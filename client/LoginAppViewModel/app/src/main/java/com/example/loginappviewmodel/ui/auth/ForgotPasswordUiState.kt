package com.example.loginappviewmodel.ui.auth

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val emailInt: String = ""
)
