package com.example.user

import com.example.annotations.Min
import com.example.annotations.NotEmpty

data class User(
    @NotEmpty val name: String,
    @Min(18) val age: Int
)