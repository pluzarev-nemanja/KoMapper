package com.nemanjap.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapTo(
    val target: KClass<*>,
    val suspendable: Boolean = true,
    val oneLineEnabled: Boolean = true
)