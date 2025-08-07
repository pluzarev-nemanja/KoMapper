package com.nemanjap.main

import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.PropertyMap

@MapTo(
    User::class,
    suspendable = true,
    oneLineEnabled = true,
    sourceNullable = true,
    targetNullable = false,
    isSingleton = false,
    propertyMaps = [
        PropertyMap(from = "userId", to = "id"),
        PropertyMap(from = "userName", to = "name")
    ]
)
data class UserDto(
    val userId: String,
    val userName: String
)
