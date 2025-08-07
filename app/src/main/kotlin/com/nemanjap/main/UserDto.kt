package com.nemanjap.main

import com.nemanjap.annotations.MapTo

@MapTo(User::class, suspendable = true, oneLineEnabled = true)
data class UserDto(
    val id: String,
    val name: String
)
