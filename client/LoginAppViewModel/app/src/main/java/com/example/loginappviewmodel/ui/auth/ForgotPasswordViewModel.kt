package com.example.loginappviewmodel.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.RetrofitInstance
import com.example.loginappviewmodel.data.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.network.dto.ForgotPasswordRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authService: AuthService = RetrofitInstance.authService
) : ViewModel()  {
    private val _uiStateFlow = MutableStateFlow(ForgotPasswordUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun onEMailChanged(email: String) {
        _uiStateFlow.update { it.copy(emailInt = email, successMessage = null, errorMessage = null) }
    }

    fun performForgotPasswordRequest() {
        println("Alex Vo performForgotPasswordRequest called")
        val email = _uiStateFlow.value.emailInt
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiStateFlow.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }
        _uiStateFlow.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(email)
                val response = authService.forgotPassword(request)
                println("Response: ${response.isSuccessful}, Body: ${response.body()}")
                if (response.isSuccessful && response.body() != null) {
                    _uiStateFlow.update { it.copy(successMessage = "Password reset link sent to $email", isLoading = false, errorMessage = null, emailInt = "") }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseApiError(errorBody)
                    _uiStateFlow.update { it.copy(errorMessage = errorMessage, isLoading = false, successMessage = null) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Network error: ${e.message}")
                _uiStateFlow.update { it.copy(errorMessage = "Network error: ${e.message}", isLoading = false) }
            }
        }

    }

    private fun parseApiError(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) {
            return "An unknown error occurred"
        }
        return try {
            Gson().fromJson(errorBody, ApiErrorResponse::class.java).error
        } catch (e: Exception) {
            "Failed to parse error response: ${e.message ?: "Unknown error"}"
        }
    }
}