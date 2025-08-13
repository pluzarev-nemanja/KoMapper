package com.nemanjap.main

import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.PropertyMap
import com.nemanjap.annotations.RegisterInKoin

@MapTo(
    User::class,
    suspendable = true,
    oneLineEnabled = true,
    sourceNullable = true,
    targetNullable = false,
    isSingleton = false,
    generateExtensions = true,
    generateReverse = true,
    propertyMaps = [
        PropertyMap(from = "userId", to = "id"),
        PropertyMap(from = "userName", to = "name")
    ]
)
@RegisterInKoin(
    isSingleton = true,
    createdAtStart = true,
//    named = "NEMANJA",
    namedClass = User::class,
    bindInterfaces = false,
    useConstructorDsl = true
)
data class UserDto(
    val userId: String,
    val userName: String
)
