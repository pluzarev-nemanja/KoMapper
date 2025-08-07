package com.nemanjap.annotations

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyMap(
    val from: String,
    val to: String
)