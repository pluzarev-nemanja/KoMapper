package com.nemanjap.annotations

import kotlin.reflect.KClass

/**
 * Declares that the annotated class should have a generated mapper to the given target type.
 *
 * This annotation is typically processed by a code generator (e.g., KSP) to create
 * mapping functions between the annotated class (source) and the specified `target` class.
 * It also allows customizing the mapping behavior through additional parameters.
 *
 * Example:
 * ```
 * @MapTo(
 *     target = UserDomain::class,
 *     suspendable = true,
 *     propertyMaps = [
 *         PropertyMap(from = "firstName", to = "givenName")
 *     ]
 * )
 * data class UserDto(val firstName: String, val age: Int)
 * ```
 *
 * The generator could then produce:
 * ```
 * suspend fun UserDto.toUserDomain(): UserDomain { ... }
 * ```
 *
 * @property target
 * The target class to which this source class should be mapped.
 *
 * @property suspendable
 * If `true`, the generated mapper function will be marked as `suspend`.
 *
 * @property oneLineEnabled
 * If `true`, the mapping will be generated in a compact single-line style
 * (useful for short mappings without intermediate variables).
 *
 * @property sourceNullable
 * If `true`, the source type will be nullable in the generated function
 * signature (e.g., `fun MyDto?.toDomain(): MyDomain`).
 *
 * @property targetNullable
 * If `true`, the target type will be nullable in the generated function
 * return type (e.g., `fun MyDto.toDomain(): MyDomain?`).
 *
 * @property isSingleton
 * If `true`, the generated mapper will be registered as a singleton in the DI container
 * (e.g., using `single` in Koin). If `false`, it will be a factory.
 *
 * @property generateReverse
 * If `true`, the generator will also create a reverse mapping function from `target` to source.
 *
 * @property generateExtensions
 * If `true`, the generator will create extension functions for mapping
 * instead of (or in addition to) standalone mapper classes.
 *
 * @property propertyMaps
 * An array of `PropertyMap` entries that define custom name mappings
 * between source and target properties when their names differ.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapTo(
    val target: KClass<*>,
    val suspendable: Boolean = true,
    val oneLineEnabled: Boolean = false,
    val sourceNullable: Boolean = false,
    val targetNullable: Boolean = false,
    val isSingleton: Boolean = false,
    val generateReverse: Boolean = false,
    val generateExtensions: Boolean = false,
    val propertyMaps: Array<PropertyMap> = []
)