package com.example.loginappviewmodel.ui.auth


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.RetrofitInstance
import com.example.loginappviewmodel.data.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.network.dto.LoginRequest
import com.example.loginappviewmodel.data.preferences.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authService: AuthService =
        RetrofitInstance.getRetrofitInstance(application).create(AuthService::class.java)
    private val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)
    private var _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameOrEmailChange(usernameOrEmail: String) {
        _uiState.value = _uiState.value.copy(
            usernameOrEmailInput = usernameOrEmail,
            errorMessage = null,
            receivedToken = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            passwordInput = password,
            errorMessage = null,
            receivedToken = null
        )
    }

    // --- Login Function using .update ---
    fun performLogin() {
        val currentState = _uiState.value
        val usernameOrEmail = currentState.usernameOrEmailInput
        val password = currentState.passwordInput

        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Username or email and password are required.",
                receivedToken = null
            )
            return
        }

        _uiState.value =
            currentState.copy(isLoading = true, errorMessage = null, receivedToken = null)

        viewModelScope.launch {
            try {
                val request = LoginRequest(usernameOrEmail, password)
                val response = authService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    println("Login successful token: ${loginResponse.token}")
                    println("Login successful user: ${loginResponse.user}")
                    userPreferencesRepository.saveAuthToken(loginResponse.token)

                    _uiState.value = currentState.copy(
                        isLoading = false,
                        receivedToken = loginResponse.token,
                        loggedInUser = loginResponse.user,
                        loginSuccess = true,
                        errorMessage = null
                    )
                    // TODO: Save token securely & Navigate to the next screen
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseApiError(errorBody) ?: "Login failed"
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        receivedToken = null,
                        loginSuccess = false
                    )
                }
            } catch (e: IOException) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please try again.",
                    receivedToken = null,
                    loginSuccess = false
                )
                e.printStackTrace()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseApiError(errorBody) ?: "HTTP error: ${e.code()}"
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    receivedToken = null,
                    loginSuccess = false
                )
                e.printStackTrace()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred.",
                    receivedToken = null,
                    loginSuccess = false
                )
                e.printStackTrace()
            }
        }
    }

    // Gọi hàm này sau khi đã điều hướng để reset cờ thành công
    fun onLoginNavigated() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    // parseApiError helper remains the same
    private fun parseApiError(errorBody: String?): String? {
        return try {
            errorBody?.let { Gson().fromJson(it, ApiErrorResponse::class.java)?.error }
        } catch (e: Exception) {
            null
        }
    }
}