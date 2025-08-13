package com.nemanjap.processor.koin.model

data class MapperRegistrationData(
    val fqMapperName: String,
    val mapperSimpleName: String,
    val isSingleton: Boolean,
    val isSuspendable: Boolean,
    val sourceTypeFqName: String,
    val sourceType: String,
    val targetType: String,
    val generatedPackage: String,
    val useConstructorDsl: Boolean,
    val targetTypeFqName: String
)
