package com.nemanjap.annotations.mapper

fun interface Mapper<in Input, out Output> {
    fun mappingObject(input: Input): Output
}