package com.example.loginappviewmodel.ui.auth

import com.example.loginappviewmodel.data.resource.network.dto.UserDto

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserDto? = null,
    val errorMessage: String? = null,
)
