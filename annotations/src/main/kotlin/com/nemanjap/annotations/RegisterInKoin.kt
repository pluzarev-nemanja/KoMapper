package com.nemanjap.annotations

import kotlin.reflect.KClass

/**
 * Marks a class to be registered in the generated Koin module.
 *
 * This annotation is intended for use with a KSP processor that generates
 * dependency injection definitions for classes (e.g., mappers, services).
 * It allows fine-tuning how the class will be registered in Koin.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterInKoin(

    /**
     * If `true`, the generated definition will use `single` (singleton scope).
     * If `false`, it will use `factory` (new instance each time).
     */
    val isSingleton: Boolean = true,

    /**
     * If `true`, the Koin definition will be created eagerly at startup
     * (`createdAtStart = true` or `withOptions { createdAtStart() }`).
     */
    val createdAtStart: Boolean = false,

    /**
     * String-based qualifier for this definition.
     * If empty, no qualifier will be applied.
     *
     * Example: `named = "prod"` generates `single(named("prod")) { ... }`
     */
    val named: String = "",

    /**
     * Type-safe qualifier for this definition.
     * If set to a type other than `Unit::class`, it will generate
     * a `named<YourQualifier>()` qualifier.
     */
    val namedClass: KClass<*> = Unit::class,

    /**
     * If `true`, the generator will automatically bind all implemented interfaces
     * using `bind`.
     *
     * Example: `single { MyMapper() } bind MyInterface::class`
     */
    val bindInterfaces: Boolean = true,

    /**
     * If `true`, the generator will use Koin's constructor DSL (`singleOf` / `factoryOf`)
     * instead of the lambda form with `get()`.
     *
     * Example:
     * - Constructor DSL: `singleOf(::MyMapper) { bind<MyInterface>() }`
     * - Lambda: `single<MyInterface> { MyMapper(get()) }`
     */
    val useConstructorDsl: Boolean = true
)