package com.nemanjap.annotations.condition

interface ConditionEvaluator<T> {
    fun shouldMap(source: T): Boolean
    fun defaultValue(): T
}