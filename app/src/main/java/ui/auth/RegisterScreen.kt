
package com.example.pikrakshak.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.pikrakshak.data.User
import com.example.pikrakshak.data.UserRepository

private val RegisterGreen = Color(0xFF14532D)
private val RegisterBackground = Color(0xFFF0FDF4)

@Composable
fun RegisterScreen(
    selectedRole: String,
    onRegistered: (User) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Maharashtra") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterBackground)
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = RegisterGreen)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Create Account 🌱",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = RegisterGreen
        )

        Text(
            "Register as $selectedRole",
            color = Color.DarkGray,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(22.dp))

        RegisterField("Full Name", fullName) { fullName = it }
        RegisterField(
            "Email Address",
            email,
            keyboardType = KeyboardType.Email
        ) { email = it }

        RegisterField(
            "Phone Number",
            phone,
            keyboardType = KeyboardType.Phone
        ) { phone = it }

        RegisterField("City", city) { city = it }
        RegisterField("State", state) { state = it }

        RegisterField(
            "Password",
            password,
            isPassword = true
        ) { password = it }

        RegisterField(
            "Confirm Password",
            confirmPassword,
            isPassword = true
        ) { confirmPassword = it }

        Spacer(Modifier.height(14.dp))

        if (errorMessage.isNotBlank()) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(10.dp))
        }

        Button(
            onClick = {
                errorMessage = ""

                when {
                    fullName.isBlank() ||
                            email.isBlank() ||
                            phone.isBlank() ||
                            city.isBlank() ||
                            state.isBlank() ||
                            password.isBlank() ||
                            confirmPassword.isBlank() -> {
                        errorMessage = "Please fill all fields."
                    }

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() -> {
                        errorMessage = "Enter a valid email address."
                    }

                    phone.filter { it.isDigit() }.length != 10 -> {
                        errorMessage = "Enter a valid 10-digit phone number."
                    }

                    password.length < 6 -> {
                        errorMessage = "Password must contain at least 6 characters."
                    }

                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match."
                    }

                    else -> {
                        scope.launch {
                            isLoading = true

                            try {
                                val user = UserRepository.registerUser(
                                    fullName = fullName,
                                    email = email,
                                    password = password,
                                    phone = phone,
                                    role = selectedRole,
                                    city = city,
                                    state = state
                                )

                                onRegistered(user)

                            } catch (e: Exception) {
                                errorMessage = e.message
                                    ?: "Registration failed. Please try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = RegisterGreen
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(23.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Account", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            label,
            color = RegisterGreen,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(5.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            }
        )
    }

    Spacer(Modifier.height(12.dp))
}
