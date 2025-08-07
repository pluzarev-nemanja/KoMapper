package com.nemanjap.annotations

import kotlin.reflect.KClass

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
    val propertyMaps: Array<PropertyMap> = []
)