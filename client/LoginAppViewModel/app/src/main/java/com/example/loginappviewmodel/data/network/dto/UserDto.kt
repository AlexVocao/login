package com.example.loginappviewmodel.data.network.dto

data class UserDto( // Reused in SignupResponse and potentially LoginResponse
    val id: Int, // Or String if using UUIDs
    val username: String,
    val email: String,
    val address: String?, // Added
    val gender: String?,  // Added
    val created_at: String
)