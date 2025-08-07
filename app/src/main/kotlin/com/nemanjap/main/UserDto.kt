package com.nemanjap.main

import com.nemanjap.annotations.MapTo
import com.nemanjap.main.User

@MapTo(User::class)
data class UserDto(
    val id: String,
    val name: String
)
