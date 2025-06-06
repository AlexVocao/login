package com.example.loginappviewmodel.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Create DataStore instance use delegation
// Name "user_auth_prefs" is used to store user authentication preferences
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth_prefs")

class UserPreferencesRepository(private val context: Context) {
    // Key to store and retrieve the authentication token
    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    // Function to save the authentication token
    suspend fun saveAuthToken(token: String) {
        context.userDataStore.edit { preferences ->
            // Store the token in preferences
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    // Function to get the authentication token. It will emit the new value whenever it changes
    suspend fun getAuthToken(): String? {
        return context.userDataStore.data
            .catch { exception ->
                // Handle any exceptions that occur while reading data
                if (exception is IOException) {
                    // Log the error or handle it as needed
                    emit(emptyPreferences()) // Emit empty preferences on error
                } else {
                    throw exception // Rethrow other exceptions
                }
            }
            .map { preferences ->
                // Retrieve the token from preferences, or return null if not found
                preferences[PreferencesKeys.AUTH_TOKEN]
            }
            .firstOrNull() // Get the first value emitted by the flow
    }

    // Function to clear the authentication token
    suspend fun clearAuthToken() {
        context.userDataStore.edit { preferences ->
            // Remove the token from preferences
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
        }
    }
}