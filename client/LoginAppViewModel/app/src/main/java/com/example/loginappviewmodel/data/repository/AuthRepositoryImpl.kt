package com.example.loginappviewmodel.data.repository

import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.dto.ForgotPasswordRequest
import com.example.loginappviewmodel.data.network.dto.LoginRequest
import com.example.loginappviewmodel.data.network.dto.ResetPasswordRequest
import com.example.loginappviewmodel.data.network.dto.SignupRequest
import com.example.loginappviewmodel.data.preferences.UserPreferencesRepository

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthRepository {

    // Auth Service
    override suspend fun signUp(request: SignupRequest) = authService.signup(request)

    override suspend fun login(request: LoginRequest) = authService.login(request)

    override suspend fun forgotPassword(request: ForgotPasswordRequest) = authService.forgotPassword(request)

    override suspend fun resetPassword(request: ResetPasswordRequest) = authService.resetPassword(request)

    override suspend fun getUserProfile() = authService.getUserProfile()

    // User Preferences
    override suspend fun saveAuthToken(token: String) {
        userPreferencesRepository.saveAuthToken(token)
    }

    override suspend fun getAuthTokenOnce(): String? {
        return userPreferencesRepository.getAuthTokenOnce()
    }

    override suspend fun clearAuthToken() {
        userPreferencesRepository.clearAuthToken()
    }
}