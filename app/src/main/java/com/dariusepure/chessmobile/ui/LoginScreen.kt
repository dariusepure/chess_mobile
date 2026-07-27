package com.dariusepure.chessmobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dariusepure.chessmobile.logic.UserManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFF302E2B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy, // Chess AI/Robot vibe
                contentDescription = null,
                tint = Color(0xFF769656),
                modifier = Modifier.size(72.dp)
            )
            
            Text(
                text = if (isRegistering) "Create Account" else "Chess Master",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = if (isRegistering) "Join our chess community today." else "Login to track your progress and points.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (errorMessage.isNotEmpty()) {
                ErrorBanner(
                    message = errorMessage,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = "" 
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF769656),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF769656),
                    unfocusedLabelColor = Color.Gray,
                    focusedLeadingIconColor = Color(0xFF769656),
                    unfocusedLeadingIconColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                enabled = !isSubmitting,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF769656),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF769656),
                    unfocusedLabelColor = Color.Gray,
                    focusedLeadingIconColor = Color(0xFF769656),
                    unfocusedLeadingIconColor = Color.Gray
                )
            )

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    
                    isSubmitting = true
                    scope.launch {
                        delay(800) // Simulate network/processing
                        val success = if (isRegistering) {
                            UserManager.register(context, email, password)
                        } else {
                            UserManager.login(context, email, password)
                        }
                        
                        if (success) {
                            onLoginSuccess()
                        } else {
                            errorMessage = if (isRegistering) "Email already exists" else "Invalid email or password"
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF769656)),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isRegistering) "Sign Up" else "Login", fontSize = 18.sp)
                }
            }

            TextButton(
                onClick = { 
                    isRegistering = !isRegistering
                    errorMessage = ""
                },
                enabled = !isSubmitting,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = if (isRegistering) "Already have an account? Login" else "New here? Register now",
                    color = Color(0xFF769656)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = Color(0xFF3E3C39), thickness = 1.dp)
            
            TextButton(
                onClick = { /* Could auto-login as guest */ onLoginSuccess() },
                enabled = !isSubmitting,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Continue as Guest (Offline)", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFB00020).copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB00020))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color(0xFFCF6679),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
