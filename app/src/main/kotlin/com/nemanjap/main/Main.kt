package com.nemanjap.main

import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() {

    startKoin {

    }

    val userDto: UserDto? = UserDto("123", "Alice")
    val mapper = UserDtoToUserMapper()
    val user: User = runBlocking {
        mapper.mappingObject(userDto)
    }
    println(user)
}