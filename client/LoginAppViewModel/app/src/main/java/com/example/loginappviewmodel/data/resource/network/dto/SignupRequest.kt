package com.example.loginappviewmodel.data.resource.network.dto
// --- Signup DTOs ---
data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val address: String? = null,
    val gender: String? = null
)
