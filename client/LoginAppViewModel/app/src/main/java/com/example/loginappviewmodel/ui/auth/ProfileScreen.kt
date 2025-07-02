package com.example.loginappviewmodel.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.loginappviewmodel.ui.AppDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by profileViewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back" )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.errorMessage != null) {
                Text(text = "Error: ${uiState.errorMessage}", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { profileViewModel.fetchUserProfile() }) {
                    Text(text = "Retry")
                }
            } else if (uiState.userProfile != null) {
                val user = uiState.userProfile!!
                println("User Profile: $user") // Debugging line
                Text(text = "Username: ${user.username}")
                Text(text = "Email: ${user.email}")
                user.gender?.let {
                    Text(text = "Gender: $it")
                }
                user.address?.let {
                    Text(text = "Address: $it")
                }
                Text(text = "Created At: ${user.created_at}")
            } else {
                Text(text = "No user profile data available.")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                profileViewModel.logout()
                navController.navigate(AppDestinations.LOGIN_SCREEN) {
                    popUpTo(AppDestinations.PROFILE_SCREEN) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text(text = "Logout")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController)
}