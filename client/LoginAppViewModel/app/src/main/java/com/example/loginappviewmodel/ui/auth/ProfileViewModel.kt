package com.example.loginappviewmodel.ui.auth

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginappviewmodel.data.resource.network.dto.ApiErrorResponse
import com.example.loginappviewmodel.data.repository.AuthRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiStateFlow = MutableStateFlow(ProfileUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        _uiStateFlow.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Check token validity before making the request
            val token = authRepository.getAuthTokenOnce()
            if (token.isNullOrEmpty()) {
                _uiStateFlow.update {
                    it.copy(
                        errorMessage = "User not authenticated",
                        isLoading = false
                    )
                }
                // Optionally, you can navigate to login screen or show a dialog
                return@launch
            }
            try {
                val response = authRepository.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    println("User profile fetched successfully: ${response.body()}")
                    val userProfile = response.body()?.user
                    _uiStateFlow.update {
                        it.copy(
                            userProfile = userProfile,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseApiError(errorBody) ?: "Failed to fetch user profile"
                    _uiStateFlow.update { it.copy(errorMessage = errorMessage, isLoading = false) }
                }
            } catch (e: IOException) {
                _uiStateFlow.update { it.copy(errorMessage = "Network error: ${e.message}", isLoading = false) }
                e.printStackTrace()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = parseApiError(errorBody) ?: "HTTP error: ${e.message()}"
                _uiStateFlow.update { it.copy(errorMessage = errorMessage, isLoading = false) }
                e.printStackTrace()
            } catch (e: CancellationException) {
                throw e // Rethrow CancellationException to allow coroutine cancellation
            } catch (e: Exception) {
                _uiStateFlow.update { it.copy(errorMessage = "Unexpected error: ${e.message}", isLoading = false) }
                e.printStackTrace()
            }
        }
    }

    private fun parseApiError(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            Gson().fromJson(errorBody, ApiErrorResponse::class.java)?.error
        } catch (e: Exception) {
            errorBody
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.clearAuthToken()
        }
    }

}