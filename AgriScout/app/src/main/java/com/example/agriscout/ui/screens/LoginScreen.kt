package com.example.agriscout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.data.firebase.FirebaseAuthManager
import com.example.agriscout.ui.components.AnimatedButton
import com.example.agriscout.ui.components.ShinyCard
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val authManager = remember { FirebaseAuthManager() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AGRISCOUT",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Modern Farm Management",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            ShinyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Welcome Back", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name (Officer Name)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedButton(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email and password cannot be empty."
                                return@AnimatedButton
                            }
                            if (isRegisterMode && name.isBlank()) {
                                errorMessage = "Please enter your full name."
                                return@AnimatedButton
                            }
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                val result = if (isRegisterMode) {
                                    authManager.register(email.trim(), password.trim(), name.trim())
                                } else {
                                    authManager.login(email.trim(), password.trim())
                                }
                                isLoading = false
                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (isRegisterMode) "Sign Up" else "Sign In", fontSize = 18.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { isRegisterMode = !isRegisterMode; errorMessage = "" }) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign In" else "No account? Register here",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
