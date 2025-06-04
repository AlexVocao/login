package com.example.loginappviewmodel.ui.auth

data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val tokenInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
)
