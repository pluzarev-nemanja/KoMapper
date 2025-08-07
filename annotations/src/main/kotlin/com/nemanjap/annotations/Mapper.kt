package com.nemanjap.annotations

fun interface Mapper<in Input, out Output> {
    fun mappingObject(input: Input): Output
}