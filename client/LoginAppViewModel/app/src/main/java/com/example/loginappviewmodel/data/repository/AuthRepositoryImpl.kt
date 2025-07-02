package com.example.loginappviewmodel.data.repository

import com.example.loginappviewmodel.data.resource.network.api.AuthService
import com.example.loginappviewmodel.data.resource.network.dto.ForgotPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.LoginRequest
import com.example.loginappviewmodel.data.resource.network.dto.ResetPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.SignupRequest
import com.example.loginappviewmodel.data.resource.local.preferences.UserPreferencesDataSource
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userPreferencesDataSource: UserPreferencesDataSource
) : AuthRepository {

    // Auth Service
    override suspend fun signUp(request: SignupRequest) = authService.signup(request)

    override suspend fun login(request: LoginRequest) = authService.login(request)

    override suspend fun forgotPassword(request: ForgotPasswordRequest) = authService.forgotPassword(request)

    override suspend fun resetPassword(request: ResetPasswordRequest) = authService.resetPassword(request)

    override suspend fun getUserProfile() = authService.getUserProfile()

    // User Preferences
    override suspend fun saveAuthToken(token: String) {
        userPreferencesDataSource.saveAuthToken(token)
    }

    override suspend fun getAuthTokenOnce(): String? {
        return userPreferencesDataSource.getAuthTokenOnce()
    }

    override suspend fun clearAuthToken() {
        userPreferencesDataSource.clearAuthToken()
    }
}