package com.nemanjap.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.Mapper

class MapperProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(MapTo::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>().forEach { sourceClass ->
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

        val file = codeGenerator.createNewFile(
            Dependencies(false, sourceClass.containingFile!!),
            sourceClass.packageName.asString(),
            mapperName
        )

        val sourceProperties = sourceClass.getAllProperties().map { it.simpleName.asString() }

        file.bufferedWriter().use { writer ->
            writer.write("package ${sourceClass.packageName.asString()}\n\n")

            writer.write("import ${Mapper::class.qualifiedName}\n")
            writer.write("import $sourceType\n")
            writer.write("import $targetType\n\n")

            writer.write("class $mapperName : ${Mapper::class.simpleName}<$sourceSimpleNameOnly, $targetSimpleName> {\n")
            writer.write("    override suspend fun mappingObject(input: $sourceSimpleNameOnly): $targetSimpleName {\n")
            writer.write("        return $targetSimpleName(\n")

            sourceProperties.forEachIndexed { index, name ->
                val comma = if (index < sourceProperties.toList().size - 1) "," else ""
                writer.write("            $name = input.$name$comma\n")
            }

            writer.write("        )\n    }\n}")
        }
    }

}