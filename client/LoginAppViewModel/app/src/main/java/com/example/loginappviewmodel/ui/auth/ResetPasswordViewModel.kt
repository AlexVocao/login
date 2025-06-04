package com.example.loginappviewmodel.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.network.AuthService
import com.example.loginappviewmodel.data.network.RetrofitInstance
import com.example.loginappviewmodel.data.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.network.dto.ResetPasswordRequest
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ResetPasswordViewModel(
    private val authService: AuthService = RetrofitInstance.authService
) : ViewModel() {
    private var _uiStateFlow = MutableStateFlow(ResetPasswordUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun onTokenInputChange(token: String) {
        _uiStateFlow.update {
            it.copy(
                tokenInput = token,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onNewPasswordInputChange(newPassword: String) {
        _uiStateFlow.update {
            it.copy(
                newPasswordInput = newPassword,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onConfirmPasswordInputChange(confirmPassword: String) {
        _uiStateFlow.update {
            it.copy(
                confirmPasswordInput = confirmPassword,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun performResetPassword() {
        val currentState = _uiStateFlow.value
        val token = currentState.tokenInput
        val newPassword = currentState.newPasswordInput
        val confirmPassword = currentState.confirmPasswordInput

        if (token.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiStateFlow.update {
                it.copy(
                    errorMessage = "All fields are required."
                )
            }
            return
        }

        if (newPassword != confirmPassword) {
            _uiStateFlow.update {
                it.copy(
                    errorMessage = "New password and confirmation do not match."
                )
            }
            return
        }
        // Call the API to reset the password here
        // Update the UI state based on the API response
        _uiStateFlow.update {
            it.copy(isLoading = true, errorMessage = null, successMessage = null)
        }

        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(token, newPassword)
                val response = authService.resetPassword(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiStateFlow.update {
                        it.copy(
                            isLoading = false,
                            successMessage = response.body()?.message ?: "Password reset successfully.",
                            errorMessage = null,
                            tokenInput = "",
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseApiError(errorBody) ?: "Reset password failed: ${response.code()}"

                    _uiStateFlow.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                            successMessage = null
                        )
                    }
                }
            } catch (ioe: IOException) {
                _uiStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${ioe.message}",
                        successMessage = null
                    )
                }
            } catch (he: HttpException) {
                val errorBody = he.response()?.errorBody()?.string()
                val errorMessage = parseApiError(errorBody) ?: "HTTP error: ${he.code()}"

                _uiStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        successMessage = null
                    )
                }
            } catch (e: CancellationException) {
               throw e // Propagate cancellation exceptions
            } catch (e: Exception) {
                _uiStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred: ${e.message}",
                        successMessage = null
                    )
                }
                e.printStackTrace()
            }
        }
    }

    private fun parseApiError(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null

        return try {
            Gson().fromJson(errorBody, ApiErrorResponse::class.java)?.message ?: "Unknown error occurred."
        } catch (e: Exception) {
            e.printStackTrace()
            "An error occurred while parsing the error response."
        }
    }
}