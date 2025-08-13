package com.nemanjap.processor.koin

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.RegisterInKoin
import com.nemanjap.annotations.mapper.Mapper
import com.nemanjap.annotations.mapper.SuspendMapper
import com.nemanjap.processor.koin.model.MapperRegistrationData
import com.nemanjap.processor.koin.util.KoinImports
import com.nemanjap.processor.koin.util.KoinProcessorException

class RegisterInKoinProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val registerInKoinSymbols = resolver
            .getSymbolsWithAnnotation(RegisterInKoin::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        logger.info("Found ${registerInKoinSymbols.size} classes annotated with @RegisterInKoin")

        val mappersData = registerInKoinSymbols.mapNotNull { collectMapperData(it) }

        if (mappersData.isEmpty()) {
            logger.warn("No mappers found to register in Koin")
        } else {
            writeKoinModule(mappersData)
            logger.info("Koin module generation completed successfully with ${mappersData.size} mappers")
        }

        return emptyList()
    }

    /**
     * Extracts mapper registration data from a class annotated with @RegisterInKoin and @MapTo
     */
    private fun collectMapperData(classDecl: KSClassDeclaration): MapperRegistrationData? {
        val mapToAnnotation = classDecl.annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == MapTo::class.qualifiedName
        } ?: KoinProcessorException.fail(
            "@RegisterInKoin can be used only with classes annotated with @MapTo"
        )

        val registerAnnotation = classDecl.annotations.first {
            it.shortName.getShortName() == RegisterInKoin::class.simpleName
        }

        val isSingleton = registerAnnotation.arguments
            .firstOrNull { it.name?.asString() == "isSingleton" }
            ?.value as? Boolean ?: false
        val useConstructorDsl = registerAnnotation.arguments
            .firstOrNull { it.name?.asString() == "useConstructorDsl" }
            ?.value as? Boolean ?: false

        val targetKSType = mapToAnnotation.arguments
            .firstOrNull { it.name?.asString() == "target" }
            ?.value as? KSType
            ?: KoinProcessorException.fail("@MapTo must reference a class")

        val suspendable = mapToAnnotation.arguments
            .firstOrNull { it.name?.asString() == "suspendable" }
            ?.value as? Boolean ?: false

        val sourceSimple = classDecl.simpleName.asString()
        val targetSimple = (targetKSType.declaration as KSClassDeclaration).simpleName.asString()
        val generatedPackage = "${classDecl.packageName.asString()}.mapper"
        val mapperClassName = "${sourceSimple}To${targetSimple}Mapper"
        val fqMapperName = "$generatedPackage.$mapperClassName"
        val targetKSTypeDecl = targetKSType.declaration as KSClassDeclaration

        val sourceFqName = classDecl.qualifiedName?.asString()
            ?: KoinProcessorException.fail("Cannot get FQ name of source class")
        val targetFqName = targetKSTypeDecl.qualifiedName?.asString()
            ?: KoinProcessorException.fail("Cannot get FQ name of target class")
        val createdAtStart = registerAnnotation.arguments
            .firstOrNull { it.name?.asString() == "createdAtStart" }?.value as? Boolean ?: false
        val named = registerAnnotation.arguments
            .firstOrNull { it.name?.asString() == "named" }?.value as? String
        val namedClassFqName = (registerAnnotation.arguments
            .firstOrNull { it.name?.asString() == "namedClass" }?.value as? KSType)
            ?.declaration?.qualifiedName?.asString()

        return MapperRegistrationData(
            fqMapperName = fqMapperName,
            mapperSimpleName = mapperClassName,
            isSingleton = isSingleton,
            isSuspendable = suspendable,
            sourceTypeFqName = sourceFqName,
            targetTypeFqName = targetFqName,
            sourceType = sourceSimple,
            targetType = targetSimple,
            generatedPackage = generatedPackage,
            useConstructorDsl = useConstructorDsl,
            createdAtStart = createdAtStart,
            named = named,
            namedClassFqName = namedClassFqName,
        )
    }

    /**
     * Writes a Koin module file containing all mapper registrations
     */
    private fun writeKoinModule(mappers: List<MapperRegistrationData>) {
        val packageName = extractDiPackage(mappers)
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            packageName = packageName,
            fileName = "MappersModule"
        )
        val imports = collectImports(mappers)
        file.bufferedWriter().use { writer ->
            writer.appendLine("package $packageName")
            writer.appendLine()
            imports.forEach { writer.appendLine("import $it") }
            mappers.forEach {
                writer.appendLine("import ${it.getInterfaceImport()}")
            }
            writer.appendLine()
            writer.appendLine("val mappersModule = module {")
            mappers.forEach { mapper ->
                writer.appendLine("    ${generateMapperRegistrationLine(mapper)}")
            }
            writer.appendLine("}")
        }
    }

    /**
     * Determines the DI package from the first mapper's source
     */
    private fun extractDiPackage(mappers: List<MapperRegistrationData>): String {
        val fqSource = mappers.first().fqMapperName
        val basePackage = fqSource.substringBeforeLast(".mapper.")
        return "$basePackage.di"
    }

    /**
     * Generates a single mapper registration line for the Koin module
     */
    private fun generateMapperRegistrationLine(mapper: MapperRegistrationData): String {
        val namedOption = when {
            !mapper.named.isNullOrEmpty() -> "named(\"${mapper.named}\")"
            !mapper.namedClassFqName.isNullOrEmpty() && mapper.namedClassFqName != "kotlin.Unit" -> "named<${mapper.namedClassFqName}>()"
            else -> null
        }

        val createdAtStartOption = if (mapper.createdAtStart) "createdAtStart = true" else null

        val params = listOfNotNull(namedOption, createdAtStartOption).joinToString(", ")

        return if (mapper.useConstructorDsl) {
            val constructorDsl = if (mapper.isSingleton) "singleOf" else "factoryOf"
            val bindLine = if (mapper.isSuspendable)
                "bind<SuspendMapper<${mapper.sourceType}, ${mapper.targetType}>>()"
            else
                "bind<Mapper<${mapper.sourceType}, ${mapper.targetType}>>()"

            if (params.isNotEmpty())
                "$constructorDsl($params, ::${mapper.mapperSimpleName}) { $bindLine }"
            else
                "$constructorDsl(::${mapper.mapperSimpleName}) { $bindLine }"
        } else {
            val bindClass = if (mapper.isSuspendable)
                "SuspendMapper<${mapper.sourceType}, ${mapper.targetType}>::class"
            else
                "Mapper<${mapper.sourceType}, ${mapper.targetType}>::class"

            val injectionType = if (mapper.isSingleton) "single" else "factory"

            if (params.isNotEmpty())
                "$injectionType($params) { ${mapper.mapperSimpleName}() } bind $bindClass"
            else
                "$injectionType { ${mapper.mapperSimpleName}() } bind $bindClass"
        }
    }

    private fun collectImports(mappers: List<MapperRegistrationData>): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add(KoinImports.KOIN_DSL_MODULE_IMPORT)
        mappers.forEach { mapper ->
            imports.add(mapper.fqMapperName)
            imports.add(mapper.sourceTypeFqName)
            imports.add(mapper.targetTypeFqName)
            if (mapper.useConstructorDsl) {
                imports.add(
                    if (mapper.isSingleton)
                        KoinImports.KOIN_DSL_SINGLE_OF_IMPORT
                    else
                        KoinImports.KOIN_DSL_FACTORY_OF_IMPORT
                )
                imports.add(KoinImports.KOIN_DSL_EXTENSION_BIND_IMPORT)
            } else {
                imports.add(KoinImports.KOIN_DSL_BIND_IMPORT)
            }
            if ((!mapper.named.isNullOrEmpty()) ||
                (!mapper.namedClassFqName.isNullOrEmpty() && mapper.namedClassFqName != "kotlin.Unit")
            ) {
                imports.add(KoinImports.KOIN_QUALIFIER_NAMED_IMPORT)
            }
            mapper.namedClassFqName?.let {
                if (it != "kotlin.Unit") imports.add(it)
            }
        }
        return imports
    }

    private fun MapperRegistrationData.getInterfaceImport(): String? =
        if (this.isSuspendable) SuspendMapper::class.qualifiedName else Mapper::class.qualifiedName

}