package com.example.pushapp.models

data class CreateUserRequest(
    val userId: String,
    val email: String,
    val firstName: String
)
