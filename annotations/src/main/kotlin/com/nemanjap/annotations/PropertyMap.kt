package com.nemanjap.annotations

import com.nemanjap.annotations.condition.ConditionEvaluator
import kotlin.reflect.KClass

/**
 * Specifies a mapping between two property names when generating code.
 *
 * This annotation is typically used on mapping classes or other annotations
 * to indicate that a source property (`from`) should be mapped to a target
 * property (`to`), even if their names differ.
 *
 * Can be applied:
 * - Directly on a class that will be processed by a mapper generator.
 * - On another annotation that will be used to decorate mapping classes.
 *
 * Example:
 * ```
 * @PropertyMap(from = "firstName", to = "givenName")
 * data class UserDto(val firstName: String)
 * ```
 *
 * The generator would then produce mapping code:
 * ```
 * target.givenName = source.firstName
 * ```
 *
 * @property from The name of the property in the source class.
 * @property to   The name of the property in the target class.
 * @property condition The [ConditionEvaluator] implementation used to check
 * whether this property should be mapped. Defaults to the base [ConditionEvaluator] (always maps).
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyMap(
    val from: String,
    val to: String,
    val condition: KClass<out ConditionEvaluator<*>> = ConditionEvaluator::class
)