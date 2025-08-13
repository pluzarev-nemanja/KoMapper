package com.nemanjap.processor.mapper.model

data class PropertyMappingData(
    val from: String,
    val to: String,
    val conditionClass: String?
)