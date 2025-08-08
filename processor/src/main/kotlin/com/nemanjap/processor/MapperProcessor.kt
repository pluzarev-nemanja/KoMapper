package com.nemanjap.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.mapper.Mapper
import com.nemanjap.annotations.PropertyMap
import com.nemanjap.annotations.mapper.SuspendMapper

class MapperProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapToSymbols = resolver.getSymbolsWithAnnotation(MapTo::class.qualifiedName!!)
        val propertyMapSymbols = resolver.getSymbolsWithAnnotation(PropertyMap::class.qualifiedName!!)
        (mapToSymbols + propertyMapSymbols)
            .filterIsInstance<KSClassDeclaration>().forEach { sourceClass ->
                generateMapper(sourceClass)
            }
        return emptyList()
    }

    private fun generateMapper(sourceClass: KSClassDeclaration) {
        val annotation = sourceClass.annotations.first {
            it.shortName.getShortName() == MapTo::class.simpleName
        }
        val targetTypeArg = annotation.arguments.first().value as KSType
        val targetDeclaration = targetTypeArg.declaration as KSClassDeclaration
        val targetType = targetDeclaration.qualifiedName?.asString() ?: return
        val targetSimpleName = targetDeclaration.simpleName.asString()

        val sourceType = sourceClass.qualifiedName?.asString() ?: return
        val sourceSimpleName = sourceClass.simpleName.asString()
        val sourceSimpleNameOnly = sourceType.substringAfterLast('.')

        val mapperName = "${sourceSimpleName}To${targetSimpleName}Mapper"
        val suspendable = annotation.arguments
            .firstOrNull { it.name?.asString() == "suspendable" }
            ?.value as? Boolean ?: true
        val suspendKeyword = if (suspendable) "suspend " else ""
        val interfaceName = if (suspendable) SuspendMapper::class.qualifiedName else Mapper::class.qualifiedName
        val interfaceSimpleName = if (suspendable) SuspendMapper::class.simpleName else Mapper::class.simpleName

        val oneLineEnabled = annotation.arguments
            .firstOrNull { it.name?.asString() == "oneLineEnabled" }
            ?.value as? Boolean ?: true

        val propertyMappings = parsePropertyMappings(annotation)
        val sourceNullable = annotation.arguments
            .firstOrNull { it.name?.asString() == "sourceNullable" }
            ?.value as? Boolean ?: false
        val targetNullable = annotation.arguments
            .firstOrNull { it.name?.asString() == "targetNullable" }
            ?.value as? Boolean ?: false
        val isSingleton = annotation.arguments
            .firstOrNull { it.name?.asString() == "isSingleton" }
            ?.value as? Boolean ?: false
        val generateReverse = annotation.arguments
            .firstOrNull { it.name?.asString() == "generateReverse" }
            ?.value as? Boolean ?: false
        val generateExtensions = annotation.arguments
            .firstOrNull { it.name?.asString() == "generateExtensions" }
            ?.value as? Boolean ?: false

        val inputType = if (sourceNullable) "$sourceSimpleNameOnly?" else sourceSimpleNameOnly
        val returnType = if (targetNullable) "$targetSimpleName?" else targetSimpleName
        val sourceProperties = sourceClass.getAllProperties().map { it.simpleName.asString() }

        writeToGeneratedFile(
            sourceClass = sourceClass,
            mapperName = mapperName,
            interfaceName = interfaceName,
            sourceType = sourceType,
            targetType = targetType,
            oneLineEnabled = oneLineEnabled,
            interfaceSimpleName = interfaceSimpleName,
            inputType = inputType,
            returnType = returnType,
            suspendKeyword = suspendKeyword,
            sourceProperties = sourceProperties,
            propertyMappings = propertyMappings,
            isSourceNullable = sourceNullable,
            isTargetNullable = targetNullable,
            isSingleton = isSingleton
        )
        if (generateReverse) {
            val reversedMappings = propertyMappings.entries.associate { (k, v) -> v to k }
            val targetProperties = targetDeclaration.getAllProperties().map { it.simpleName.asString() }

            val targetInputType = if (targetNullable) "$targetSimpleName?" else targetSimpleName
            val sourceReturnType = if (sourceNullable) "$sourceSimpleNameOnly?" else sourceSimpleNameOnly
            writeToGeneratedFile(
                sourceClass = targetDeclaration,
                mapperName = "${targetSimpleName}To${sourceSimpleName}Mapper",
                interfaceName = interfaceName,
                sourceType = targetType,
                targetType = sourceType,
                oneLineEnabled = oneLineEnabled,
                interfaceSimpleName = interfaceSimpleName,
                inputType = targetInputType,
                returnType = sourceReturnType,
                suspendKeyword = suspendKeyword,
                sourceProperties = targetProperties,
                propertyMappings = reversedMappings,
                isSourceNullable = targetNullable,
                isTargetNullable = sourceNullable,
                isSingleton = isSingleton
            )
        }
        if (generateExtensions) {
            writeExtensionFunction(
                sourceClass = sourceClass,
                sourceSimpleNameOnly = sourceSimpleNameOnly,
                targetSimpleName = targetSimpleName,
                isSourceNullable = sourceNullable,
                isTargetNullable = targetNullable,
                suspendable = suspendable
            )
            if (generateReverse) {
                writeExtensionFunction(
                    sourceClass = targetDeclaration,
                    sourceSimpleNameOnly = targetSimpleName,
                    targetSimpleName = sourceSimpleNameOnly,
                    isSourceNullable = targetNullable,
                    isTargetNullable = sourceNullable,
                    suspendable = suspendable
                )
            }
        }
    }

    private fun writeExtensionFunction(
        sourceClass: KSClassDeclaration,
        sourceSimpleNameOnly: String,
        targetSimpleName: String,
        isSourceNullable: Boolean,
        isTargetNullable: Boolean,
        suspendable: Boolean
    ) {
        val packageName = sourceClass.packageName.asString()
        val fileName = "${sourceSimpleNameOnly}To${targetSimpleName}Extensions"

        val file = codeGenerator.createNewFile(
            Dependencies(true, sourceClass.containingFile!!),
            packageName,
            fileName
        )
        val inputType = if (isSourceNullable) "$sourceSimpleNameOnly?" else sourceSimpleNameOnly
        val returnType = if (isTargetNullable) "$targetSimpleName?" else targetSimpleName

        file.bufferedWriter().use { writer ->
            writer.write("package $packageName\n\n")
            if (suspendable) {
                writer.write("import kotlinx.coroutines.runBlocking\n\n")
                writer.write("// Extension suspend function to map $inputType to $returnType\n")
                writer.write("suspend fun $inputType.to$targetSimpleName(): $returnType {\n")
                writer.write("    return ${sourceSimpleNameOnly}To${targetSimpleName}Mapper().mappingObject(this)\n")
                writer.write("}\n\n")
                writer.write("// Blocking version for non-suspend context\n")
                writer.write("fun $inputType.to${targetSimpleName}Blocking(): $returnType = runBlocking {\n")
                writer.write("    to$targetSimpleName()\n")
                writer.write("}\n")
            } else {
                writer.write("// Extension function to map $inputType to $returnType\n")
                writer.write("fun $inputType.to$targetSimpleName(): $returnType {\n")
                writer.write("    return ${sourceSimpleNameOnly}To${targetSimpleName}Mapper().mappingObject(this)\n")
                writer.write("}\n")
            }
        }
    }

    private fun writeToGeneratedFile(
        sourceClass: KSClassDeclaration,
        mapperName: String,
        interfaceName: String?,
        sourceType: String,
        targetType: String,
        oneLineEnabled: Boolean,
        interfaceSimpleName: String?,
        inputType: String,
        returnType: String,
        suspendKeyword: String,
        sourceProperties: Sequence<String>,
        propertyMappings: Map<String, String>,
        isSourceNullable: Boolean,
        isTargetNullable: Boolean,
        isSingleton: Boolean
    ) {
        val effectiveReturnType = if (!isSourceNullable && isTargetNullable) {
            returnType.trimEnd('?')
        } else {
            returnType
        }
        val prefixType = if (isSingleton) "object" else "class"

        val file = codeGenerator.createNewFile(
            Dependencies(false, sourceClass.containingFile!!),
            sourceClass.packageName.asString(),
            mapperName
        )
        file.bufferedWriter().use { writer ->
            writer.write("package ${sourceClass.packageName.asString()}\n\n")
            writer.write("import $interfaceName\n")
            writer.write("import $sourceType\n")
            writer.write("import $targetType\n\n")

            writer.write("$prefixType $mapperName : $interfaceSimpleName<$inputType, $effectiveReturnType> {\n")

            if (isSourceNullable && isTargetNullable) {
                if (oneLineEnabled) {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType = input?.let {\n")
                    writer.write("        ${effectiveReturnType.trimEnd('?')}(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("            $targetPropName = it.$sourcePropName$comma\n")
                    }
                    writer.write("        )\n    }\n")
                } else {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType {\n")
                    writer.write("        return input?.let {\n")
                    writer.write("            ${effectiveReturnType.trimEnd('?')}(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("                $targetPropName = it.$sourcePropName$comma\n")
                    }
                    writer.write("            )\n")
                    writer.write("        }\n    }\n")
                }
            } else if (isSourceNullable) {
                if (oneLineEnabled) {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType = input?.let {\n")
                    writer.write("        ${effectiveReturnType.trimEnd('?')}(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("            $targetPropName = it.$sourcePropName$comma\n")
                    }
                    writer.write("        )\n    } ?: throw IllegalArgumentException(\"Input is null but target is non-nullable\")\n")
                } else {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType {\n")
                    writer.write("        return input?.let {\n")
                    writer.write("            ${effectiveReturnType.trimEnd('?')}(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("                $targetPropName = it.$sourcePropName$comma\n")
                    }
                    writer.write("            )\n")
                    writer.write("        } ?: throw IllegalArgumentException(\"Input is null but target is non-nullable\")\n")
                    writer.write("    }\n")
                }
            } else {
                if (oneLineEnabled) {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType = $effectiveReturnType(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("        $targetPropName = input.$sourcePropName$comma\n")
                    }
                    writer.write("    )\n")
                } else {
                    writer.write("    override ${suspendKeyword}fun mappingObject(input: $inputType): $effectiveReturnType {\n")
                    writer.write("        return $effectiveReturnType(\n")
                    sourceProperties.forEachIndexed { index, sourcePropName ->
                        val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                        val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                        writer.write("            $targetPropName = input.$sourcePropName$comma\n")
                    }
                    writer.write("        )\n    }\n")
                }
            }

            writer.write("}\n")
        }
    }

    private fun parsePropertyMappings(annotation: KSAnnotation): Map<String, String> =
        buildMap {
            val propertyMapsArg = annotation.arguments.firstOrNull { it.name?.asString() == "propertyMaps" }
            val propertyMapsValue = propertyMapsArg?.value

            if (propertyMapsValue is List<*>) {
                for (pmAnnotation in propertyMapsValue) {
                    if (pmAnnotation is KSAnnotation) {
                        val from =
                            pmAnnotation.arguments.firstOrNull { it.name?.asString() == "from" }?.value as? String
                        val to = pmAnnotation.arguments.firstOrNull { it.name?.asString() == "to" }?.value as? String
                        if (from != null && to != null) {
                            put(from, to)
                        }
                    }
                }
            }
        }
}