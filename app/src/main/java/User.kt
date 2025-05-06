package com.example.bar

data class User(
    val id: Int? = null,
    val login: String,
    val password: String,
    val username: String? = null,
    val email: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)