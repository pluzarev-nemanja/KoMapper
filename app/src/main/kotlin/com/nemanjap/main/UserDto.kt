package com.nemanjap.main

import com.nemanjap.annotations.MapTo
import com.nemanjap.annotations.PropertyMap
import com.nemanjap.annotations.RegisterInKoin
import com.nemanjap.annotations.condition.ConditionEvaluator

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
        PropertyMap(
            from = "userId",
            to = "id",
            condition = UserDtoConditionEvaluator::class
        ),
        PropertyMap(
            from = "userName",
            to = "name",
            condition = UserNameConditionEvaluator::class
        )
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

class UserDtoConditionEvaluator : ConditionEvaluator<String> {
    override fun shouldMap(source: String): Boolean {
        return source.isNotBlank()
    }

    override fun defaultValue(): String = "9"
}

class UserNameConditionEvaluator : ConditionEvaluator<String> {
    override fun shouldMap(source: String): Boolean {
        return source.firstOrNull()?.isUpperCase() == true
    }

    override fun defaultValue(): String = "USERNAME"
}