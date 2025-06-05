package com.example.loginappviewmodel.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.RetrofitInstance
import com.example.loginappviewmodel.data.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.network.dto.LoginRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(
    private val authService: AuthService = RetrofitInstance.authService
) : ViewModel() {
    private var _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameOrEmailChange(usernameOrEmail: String) {
        _uiState.value = _uiState.value.copy(
            usernameOrEmailInput = usernameOrEmail,
            errorMessage = null,
            successToken = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            passwordInput = password,
            errorMessage = null,
            successToken = null
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
                successToken = null
            )
            return
        }

        _uiState.value =
            currentState.copy(isLoading = true, errorMessage = null, successToken = null)

        viewModelScope.launch {
            try {
                val request = LoginRequest(usernameOrEmail, password)
                val response = authService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        successToken = loginResponse.token,
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
                        successToken = null,
                        loginSuccess = false
                    )
                }
            } catch (e: IOException) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please try again.",
                    successToken = null,
                    loginSuccess = false
                )
                e.printStackTrace()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseApiError(errorBody) ?: "HTTP error: ${e.code()}"
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    successToken = null,
                    loginSuccess = false
                )
                e.printStackTrace()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred.",
                    successToken = null,
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