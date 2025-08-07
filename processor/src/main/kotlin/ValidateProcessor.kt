package com.example.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter

class ValidateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn(">>> KSP Processor started")

        val notEmptySymbols = resolver.getSymbolsWithAnnotation("com.example.annotations.NotEmpty")
        val minSymbols = resolver.getSymbolsWithAnnotation("com.example.annotations.Min")

        logger.warn("Found @NotEmpty symbols count: ${notEmptySymbols.count()}")
        logger.warn("Found @Min symbols count: ${minSymbols.count()}")

        val symbols = notEmptySymbols + minSymbols

        if (symbols.none()) {
            logger.warn("No symbols found with target annotations, finishing processing early.")
            return emptyList()
        }

        val classes = symbols
            .mapNotNull {
                logger.warn("Inspecting symbol: ${it::class.simpleName} with name: ${it.toString()}")
                val parent = it.parent
                logger.warn("Parent node class: ${parent?.let { p -> p::class.simpleName }} with value: $parent")
                val classDecl = parent as? KSClassDeclaration
                if (classDecl == null) {
                    logger.warn("Parent is not KSClassDeclaration, attempting to find parentDeclaration chain.")
                    val foundClass = generateSequence(parent) { it.parent }
                        .filterIsInstance<KSClassDeclaration>()
                        .firstOrNull()
                    if (foundClass != null) {
                        logger.warn("Found KSClassDeclaration via parent chain: ${foundClass.simpleName.asString()}")
                    } else {
                        logger.warn("No KSClassDeclaration found in parent chain.")
                    }
                    foundClass
                } else {
                    classDecl
                }
            }
            .distinct()

        if (classes.toList().isEmpty()) {
            logger.warn("No classes found to process after filtering symbols.")
            return emptyList()
        }

        for (klass in classes) {
            val className = klass.simpleName.asString()
            val packageName = klass.packageName.asString()
            logger.warn("Generating validator for class: $packageName.$className")

            val validateFunc = buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("fun $className.validate(): List<String> {")
                appendLine("    val errors = mutableListOf<String>()")
                klass.getAllProperties().forEach { property ->
                    val propName = property.simpleName.asString()
                    logger.warn("  Inspecting property: $propName with annotations: ${property.annotations.map { it.shortName.asString() }}")

                    property.annotations.forEach { annotation ->
                        val annotationName = annotation.shortName.asString()
                        logger.warn("    Processing annotation: $annotationName")

                        when (annotationName) {
                            "NotEmpty" -> {
                                appendLine("    if (this.$propName.isEmpty()) errors.add(\"$propName must not be empty\")")
                            }
                            "Min" -> {
                                val minVal = annotation.arguments.firstOrNull()?.value
                                logger.warn("    Min annotation value: $minVal")
                                appendLine("    if (this.$propName < $minVal) errors.add(\"$propName must be at least $minVal\")")
                            }
                        }
                    }
                }
                appendLine("    return errors")
                appendLine("}")
            }

            logger.warn("Generated validate function code:\n$validateFunc")

            val file = codeGenerator.createNewFile(
                Dependencies(false, klass.containingFile!!),
                packageName,
                "${className}Validator"
            )
            OutputStreamWriter(file, Charsets.UTF_8).use {
                it.write(validateFunc)
            }

            logger.warn("Validator file created for $className")
        }

        logger.warn("<<< KSP Processor finished")
        return emptyList()
    }
}