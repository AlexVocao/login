package com.example.loginappviewmodel.ui.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val successToken: String? = null,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val usernameOrEmailInput: String = "",
    val passwordInput: String = ""
)