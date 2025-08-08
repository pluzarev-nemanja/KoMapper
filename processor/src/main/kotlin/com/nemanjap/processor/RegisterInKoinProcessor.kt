package com.nemanjap.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.RegisterInKoin

class RegisterInKoinProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val mappers = mutableListOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val registerInKoinSymbols = resolver
            .getSymbolsWithAnnotation(RegisterInKoin::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        registerInKoinSymbols.forEach { classDecl ->
            val hasMapTo = classDecl.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == MapTo::class.qualifiedName
            }

            if (!hasMapTo) {
                logger.error(
                    "@RegisterInKoin can be used only with classes annotated with @MapTo",
                    classDecl
                )
                return@forEach
            }

            val qualifiedName = classDecl.qualifiedName?.asString()
            if (qualifiedName != null) {
                mappers.add(qualifiedName)
            }
        }

        if (mappers.isNotEmpty()) {
            writeKoinModule(mappers.distinct())
        }

        return emptyList()
    }

    private fun writeKoinModule(mappers: List<String>) {
        val file = codeGenerator.createNewFile(
            dependencies = com.google.devtools.ksp.processing.Dependencies(false),
            packageName = "com.nemanjap.generated",
            fileName = "MappersKoinModule"
        )

        file.bufferedWriter().use { writer ->
            writer.appendLine("package com.nemanjap.generated")
            writer.appendLine()
            writer.appendLine("import org.koin.dsl.module")
            mappers.forEach {
                writer.appendLine("import $it")
            }
            writer.appendLine()
            writer.appendLine("val mappersModule = module {")
            mappers.forEach { mapper ->
                writer.appendLine("    single { $mapper() }")
            }
            writer.appendLine("}")
        }
    }
}