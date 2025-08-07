package com.nemanjap.main

import kotlinx.coroutines.runBlocking

fun main() {
    val userDto = UserDto("123", "Alice")
    val mapper = UserDtoToUserMapper()
    val user = runBlocking {
        mapper.mappingObject(userDto)
    }
    println(user)
}