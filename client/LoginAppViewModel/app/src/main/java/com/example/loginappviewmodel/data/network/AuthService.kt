package com.example.loginappviewmodel.data.network

import android.content.Context
import com.example.loginappviewmodel.data.network.dto.ForgotPasswordRequest
import com.example.loginappviewmodel.data.network.dto.GenericSuccessResponse
import com.example.loginappviewmodel.data.network.dto.LoginRequest
import com.example.loginappviewmodel.data.network.dto.LoginResponse
import com.example.loginappviewmodel.data.network.dto.ProfileResponse
import com.example.loginappviewmodel.data.network.dto.ResetPasswordRequest
import com.example.loginappviewmodel.data.network.dto.SignupRequest
import com.example.loginappviewmodel.data.network.dto.SignupResponse
import com.example.loginappviewmodel.data.network.dto.UserDto
import com.example.loginappviewmodel.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

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
class AuthInterceptor(private val userPreferencesRepository: UserPreferencesRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = runBlocking { userPreferencesRepository.getAuthTokenOnce() }
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}

object RetrofitInstance {
    // Use 10.0.2.2 for Android Emulator accessing localhost on host machine
    // Use your machine's local network IP for physical device testing on same Wi-Fi
    private const val BASE_URL = "http://10.0.2.2:3000/" // Ensure trailing slash!
    //private const val BASE_URL = "http://192.168.1.11:3000/" // Ensure trailing slash!

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add Auth Interceptor
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getRetrofitInstance(context: Context): Retrofit {
        val userPreferencesRepository = UserPreferencesRepository(context)
        val authInterceptor = AuthInterceptor(userPreferencesRepository)
        val okHttpClient = createOkHttpClient(authInterceptor)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}