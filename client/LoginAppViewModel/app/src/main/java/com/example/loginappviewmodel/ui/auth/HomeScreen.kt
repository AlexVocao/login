package com.example.loginappviewmodel.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.loginappviewmodel.ui.AppDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Text("You have successfully logged in.")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                // Navigate back to login, clearing the back stack up to login
                navController.navigate(AppDestinations.LOGIN_SCREEN) {
                    popUpTo(AppDestinations.HOME_SCREEN) { inclusive = true } // Remove home from backstack
                    launchSingleTop = true // Avoid multiple copies of login if already there
                }
                // TODO: Clear any stored user session/token here
            }) {
                Text("LOGOUT")
            }
        }
    }
}