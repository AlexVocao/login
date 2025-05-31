package com.example.loginappviewmodel.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginappviewmodel.ui.auth.LoginScreen
import com.example.loginappviewmodel.ui.auth.SignupScreen

object AppDestinations {
    const val LOGIN_SCREEN = "login"
    const val SIGNUP_SCREEN = "signup"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    // Navigation logic will be implemented here
    // This is where you will set up your NavHost and NavController
    // For now, we can just leave it empty or add a placeholder
    // to indicate that this is where navigation will occur.
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_SCREEN
    ) {
        composable(AppDestinations.LOGIN_SCREEN) {
            LoginScreen(navController)
        }
        composable(AppDestinations.SIGNUP_SCREEN) {
            SignupScreen(navController)
        }
    }
}