package com.example.loginappviewmodel.ui.auth

data class SignupUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val addressInput: String = "",
    val genderInput: String = ""
)