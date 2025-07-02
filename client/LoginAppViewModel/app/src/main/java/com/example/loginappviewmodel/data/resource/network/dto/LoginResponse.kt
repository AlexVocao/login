package com.example.loginappviewmodel.data.resource.network.dto

data class LoginResponse(
    val message: String,
    val token: String, // Assuming backend returns a JWT token on successful login
    // Optionally include user details here too if needed immediately after login
    val user: UserDto? = null
)