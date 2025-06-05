package com.example.loginappviewmodel.data.network.dto

// --- Reset Password DTOs ---
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
