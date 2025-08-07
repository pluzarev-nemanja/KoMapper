package com.nemanjap.main

import kotlinx.coroutines.runBlocking

fun main() {
    val userDto: UserDto? = UserDto("123", "Alice")
    val mapper = UserDtoToUserMapper()
    val user: User = runBlocking {
        mapper.mappingObject(userDto)
    }
    println(user)
}