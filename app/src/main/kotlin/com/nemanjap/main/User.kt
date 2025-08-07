package com.nemanjap.main

import com.nemanjap.annotations.Min
import com.nemanjap.annotations.NotEmpty

data class User(
    @NotEmpty val name: String,
    @Min(18) val age: Int
)