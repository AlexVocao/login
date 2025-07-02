package com.example.loginappviewmodel.data.resource.network.dto

// --- Login DTOs ---
data class LoginRequest(
    val usernameOrEmail: String, // Backend should handle either
    val password: String
)