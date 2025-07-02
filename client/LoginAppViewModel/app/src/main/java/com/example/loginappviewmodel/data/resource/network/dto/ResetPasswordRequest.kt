package com.example.loginappviewmodel.data.resource.network.dto

// --- Reset Password DTOs ---
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
