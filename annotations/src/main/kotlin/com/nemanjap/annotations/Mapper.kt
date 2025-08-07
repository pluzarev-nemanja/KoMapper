package com.nemanjap.annotations

fun interface Mapper<in Input, out Output> {
    suspend fun mappingObject(input: Input): Output
}