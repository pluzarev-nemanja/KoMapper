package com.nemanjap.processor.koin.util

class KoinProcessorException(message: String) : RuntimeException(message) {
    companion object {
        fun fail(message: String): Nothing {
            throw KoinProcessorException(message)
        }
    }
}