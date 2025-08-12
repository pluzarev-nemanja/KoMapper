package com.nemanjap.processor.koin

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.RegisterInKoin
import com.nemanjap.processor.koin.util.KoinProcessorException

class RegisterInKoinProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val mappers = mutableListOf<String>()
    private val koinDslModuleImport = "org.koin.dsl.module"
    private val koinDslBindImport = "org.koin.dsl.bind"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val registerInKoinSymbols = resolver
            .getSymbolsWithAnnotation(RegisterInKoin::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        registerInKoinSymbols.forEach { classDecl ->
            val mapToAnnotation = classDecl.annotations.firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == MapTo::class.qualifiedName
            }

            if (mapToAnnotation == null) {
                KoinProcessorException.fail(
                    "@RegisterInKoin can be used only with classes annotated with @MapTo"
                )
            }

            val mapToArg = mapToAnnotation.arguments.firstOrNull()
            val targetType = (mapToArg?.value as? KSType)?.declaration as? KSClassDeclaration
                ?: KoinProcessorException.fail("@MapTo must reference a class")

            val sourcePackage = classDecl.packageName.asString()
            val sourceSimple = classDecl.simpleName.asString()

            val targetSimple = targetType.simpleName.asString()

            val generatedPackage = "$sourcePackage.mapper"

            val mapperClassName = "${sourceSimple}To${targetSimple}Mapper"

            val mapperFqName = "$generatedPackage.$mapperClassName"

            mappers.add(mapperFqName)
        }

        if (mappers.isNotEmpty() && registerInKoinSymbols.isNotEmpty()) {
            val sourceClass = registerInKoinSymbols.first()
            val annotation = sourceClass.annotations.first {
                it.shortName.getShortName() == RegisterInKoin::class.simpleName
            }
            val isSingleton = annotation.arguments
                .firstOrNull { it.name?.asString() == "isSingleton" }
                ?.value as? Boolean ?: false
            writeKoinModule(
                mappers.distinct(),
                sourceClass = sourceClass,
                isSingleton = isSingleton
            )
        }

        return emptyList()
    }

    private fun writeKoinModule(
        mappers: List<String>,
        sourceClass: KSClassDeclaration,
        isSingleton: Boolean
    ) {
        val packageName = sourceClass.packageName.asString().plus(".di")
        val file = codeGenerator.createNewFile(
            Dependencies(false, sourceClass.containingFile!!),
            packageName = packageName,
            fileName = "MappersModule"
        )
        val injectionType = if (isSingleton) "single" else "factory"

        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.appendLine()
            writer.appendLine("import $koinDslModuleImport")
            writer.appendLine("import $koinDslBindImport")
            mappers.forEach {
                writer.appendLine("import $it")
            }
            writer.appendLine()
            writer.appendLine("val mappersModule = module {")
            mappers.forEach { mapper ->
                val className = mapper.substringAfterLast('.')
                writer.appendLine("    $injectionType { $className() } bind ")
            }
            writer.appendLine("}")
        }
    }
}