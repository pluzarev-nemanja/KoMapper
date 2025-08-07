package com.nemanjap.annotations

fun interface SuspendMapper<in Input, out Output> {
    suspend fun mappingObject(input: Input): Output
}