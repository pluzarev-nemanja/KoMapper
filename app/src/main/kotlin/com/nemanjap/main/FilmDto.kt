package com.nemanjap.main

import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.RegisterInKoin

@MapTo(
    Film::class,
    suspendable = true,
    oneLineEnabled = true,
    sourceNullable = true,
    targetNullable = false,
    isSingleton = false,
    generateExtensions = true,
    generateReverse = true
)
@RegisterInKoin(
    isSingleton = false,
    createdAtStart = false,
    named = "FILM",
    namedClass = Film::class,
    bindInterfaces = false,
    useConstructorDsl = false
)
data class FilmDto(
    val name: String,
    val rate: String
)