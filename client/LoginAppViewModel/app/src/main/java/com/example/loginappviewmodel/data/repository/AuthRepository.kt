package com.example.loginappviewmodel.data.repository

import com.example.loginappviewmodel.data.resource.network.dto.ForgotPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.GenericSuccessResponse
import com.example.loginappviewmodel.data.resource.network.dto.LoginRequest
import com.example.loginappviewmodel.data.resource.network.dto.LoginResponse
import com.example.loginappviewmodel.data.resource.network.dto.ProfileResponse
import com.example.loginappviewmodel.data.resource.network.dto.ResetPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.SignupRequest
import com.example.loginappviewmodel.data.resource.network.dto.SignupResponse
import retrofit2.Response

interface AuthRepository {
    // Auth Service
    suspend fun signUp(request: SignupRequest): Response<SignupResponse>
    suspend fun login(request: LoginRequest): Response<LoginResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Response<GenericSuccessResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Response<GenericSuccessResponse>
    suspend fun getUserProfile(): Response<ProfileResponse>

    // User Preferences
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthTokenOnce(): String?
    suspend fun clearAuthToken()

}