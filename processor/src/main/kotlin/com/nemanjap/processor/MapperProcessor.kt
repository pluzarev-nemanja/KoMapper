package com.nemanjap.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.Mapper
import com.nemanjap.annotations.PropertyMap
import com.nemanjap.annotations.SuspendMapper

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

        val file = codeGenerator.createNewFile(
            Dependencies(false, sourceClass.containingFile!!),
            sourceClass.packageName.asString(),
            mapperName
        )

        val sourceProperties = sourceClass.getAllProperties().map { it.simpleName.asString() }

        file.bufferedWriter().use { writer ->
            writer.write("package ${sourceClass.packageName.asString()}\n\n")
            writer.write("import $interfaceName\n")
            writer.write("import $sourceType\n")
            writer.write("import $targetType\n\n")

            if (oneLineEnabled) {
                writer.write("class $mapperName : $interfaceSimpleName<$sourceSimpleNameOnly, $targetSimpleName> {\n")
                writer.write("    override ${suspendKeyword}fun mappingObject(input: $sourceSimpleNameOnly): $targetSimpleName = $targetSimpleName(\n")

                sourceProperties.forEachIndexed { index, sourcePropName ->
                    val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                    val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                    writer.write("        $targetPropName = input.$sourcePropName$comma\n")
                }

                writer.write("    )\n}")
            } else {
                writer.write("class $mapperName : $interfaceSimpleName<$sourceSimpleNameOnly, $targetSimpleName> {\n")
                writer.write("    override ${suspendKeyword}fun mappingObject(input: $sourceSimpleNameOnly): $targetSimpleName {\n")
                writer.write("        return $targetSimpleName(\n")

                sourceProperties.forEachIndexed { index, sourcePropName ->
                    val targetPropName = propertyMappings[sourcePropName] ?: sourcePropName
                    val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                    writer.write("        $targetPropName = input.$sourcePropName$comma\n")
                }

                writer.write("        )\n    }\n}")
            }
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