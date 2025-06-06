package com.example.loginappviewmodel.ui.auth

import com.example.loginappviewmodel.data.network.dto.UserDto

data class LoginUiState(
    val isLoading: Boolean = false,
    val receivedToken: String? = null, // To save token temporarily after login
    val loggedInUser: UserDto? = null, // To save user temporarily after login
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val usernameOrEmailInput: String = "",
    val passwordInput: String = ""
)