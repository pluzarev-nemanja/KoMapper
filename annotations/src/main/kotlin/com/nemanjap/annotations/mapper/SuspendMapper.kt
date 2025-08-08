package com.nemanjap.annotations.mapper

fun interface SuspendMapper<in Input, out Output> {
    suspend fun mappingObject(input: Input): Output
}