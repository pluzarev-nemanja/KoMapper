package com.nemanjap.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterInKoin(
    val isSingleton: Boolean = true
)