package com.nemanjap.main

import com.nemanjap.main.di.mappersModule
import com.nemanjap.main.mapper.UserDtoToUserMapper
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() {

    startKoin {
        modules(mappersModule)
    }

    val userDto: UserDto? = UserDto("123", "Alice")
    val mapper = UserDtoToUserMapper()
    val user: User = runBlocking {
        mapper.mappingObject(userDto)
    }
    println(user)
}