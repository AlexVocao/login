package com.example.loginappviewmodel.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.RetrofitInstance
import com.example.loginappviewmodel.data.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.network.dto.SignupRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignupViewModel(private val authService: AuthService = RetrofitInstance.authService) :
    ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    // --- Event Handlers using .update() ---
    fun onUsernameChange(username: String) {
        _uiState.update {
            it.copy(
                usernameInput = username,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                passwordInput = password,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPasswordInput = confirmPassword,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onAddressChange(address: String) {
        _uiState.update {
            it.copy(
                addressInput = address,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onGenderChange(gender: String) {
        _uiState.update {
            it.copy(
                genderInput = gender,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // --- Signup Function using .update ---
    fun performSignup() {
        // Get current state value directly for validation
        val currentState = _uiState.value
        val username = currentState.usernameInput
        val email = currentState.emailInput
        val password = currentState.passwordInput
        val confirmPassword = currentState.confirmPasswordInput
        val address = currentState.addressInput
        val gender = currentState.genderInput

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username, email, and password are required.") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format.") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                val request = SignupRequest(username, email, password, address, gender)
                val response = authService.signup(request)
                if (response.isSuccessful && response.body() != null) {
                    val signupResponse = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = signupResponse.message,
                            errorMessage = null,
                            usernameInput = "",
                            emailInput = "",
                            passwordInput = "",
                            confirmPasswordInput = "",
                            addressInput = "",
                            genderInput = ""
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage =
                        parseApiError(errorBody) ?: "Signup failed: ${response.code()}"
                    println("Error Body: $errorMessage")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                            successMessage = null
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${e.message}",
                        successMessage = null
                    )
                }
                e.printStackTrace()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseApiError(errorBody) ?: "HTTP error: ${e.code()}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        successMessage = null
                    )
                }
                e.printStackTrace()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An unexpected error occurred: ${e.message}",
                        successMessage = null
                    )
                }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- Error Parsing ---
    private fun parseApiError(errorBody: String?): String? {
        return try {
            errorBody?.let { Gson().fromJson(it, ApiErrorResponse::class.java)?.message }
        } catch (e: Exception) {
            null
        }
    }
}