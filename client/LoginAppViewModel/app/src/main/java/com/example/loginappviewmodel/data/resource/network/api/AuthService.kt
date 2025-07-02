package com.example.loginappviewmodel.data.resource.network.api

import com.example.loginappviewmodel.data.resource.network.dto.ForgotPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.GenericSuccessResponse
import com.example.loginappviewmodel.data.resource.network.dto.LoginRequest
import com.example.loginappviewmodel.data.resource.network.dto.LoginResponse
import com.example.loginappviewmodel.data.resource.network.dto.ProfileResponse
import com.example.loginappviewmodel.data.resource.network.dto.ResetPasswordRequest
import com.example.loginappviewmodel.data.resource.network.dto.SignupRequest
import com.example.loginappviewmodel.data.resource.network.dto.SignupResponse
import com.example.loginappviewmodel.data.resource.local.preferences.UserPreferencesDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/signup")
    suspend fun signup(
        @Body requestBody: SignupRequest
    ): Response<SignupResponse>

    @POST("api/auth/login") // Added Login endpoint
    suspend fun login(
        @Body requestBody: LoginRequest
    ): Response<LoginResponse> // Use new LoginResponse DTO

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body requestBody: ForgotPasswordRequest
    ): Response<GenericSuccessResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body requestBody: ResetPasswordRequest
    ): Response<GenericSuccessResponse>

    // New endpoint to get user profile (requires jwt token)
    @GET("api/profile/me")
    suspend fun getUserProfile(): Response<ProfileResponse>
}

// --- Auth Interceptor (add token to header) ---
class AuthInterceptor(private val userPreferencesDataSource: UserPreferencesDataSource) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = runBlocking { userPreferencesDataSource.getAuthTokenOnce() }
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}
