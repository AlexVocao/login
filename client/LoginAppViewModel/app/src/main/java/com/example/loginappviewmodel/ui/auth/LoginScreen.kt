package com.example.loginappviewmodel.ui.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.loginappviewmodel.ui.AppDestinations

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.successToken) {
        if (uiState.successToken != null) {
            // Navigate to the main screen or home screen
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(BorderStroke(1.dp, Color.Gray), CircleShape)
                .padding(8.dp), contentAlignment = Alignment.Center
        ) {
            // Logo or Image can be placed here
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = "Logo",
                tint = Color.Gray,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Android",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black
        )
        Spacer(modifier = Modifier.size(24.dp))

        if (uiState.isLoading) {
            // Show loading indicator
            CircularProgressIndicator()
            Spacer(modifier = Modifier.size(16.dp))
        }
        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        OutlinedTextField(
            value = uiState.usernameOrEmailInput,
            onValueChange = loginViewModel::onUsernameOrEmailChange,
            label = { Text("Username or Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = uiState.errorMessage != null
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = loginViewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide(); focusManager.clearFocus(); loginViewModel.performLogin() }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = uiState.errorMessage != null
        )
        TextButton(
            onClick = { /* TODO: Forgot Password */ },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Forgot Password") }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { keyboardController?.hide(); focusManager.clearFocus(); loginViewModel.performLogin() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            enabled = !uiState.isLoading
        ) { Text("LOGIN", modifier = Modifier.padding(vertical = 8.dp)) }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { navController.navigate(AppDestinations.SIGNUP_SCREEN) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) { Text("SIGNUP", modifier = Modifier.padding(vertical = 8.dp)) }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Or", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { /* TODO: Google Login */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                Icons.Filled.AccountCircle,
                "Google Logo",
                Modifier.size(ButtonDefaults.IconSize),
                tint = Color.Gray
            ); Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(
            "Login with Google",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        }
        Spacer(modifier = Modifier.height(16.dp))


    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    // YourAppTheme {
    LoginScreen(navController = navController)
    // }
}