
package com.example.pikrakshak

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun StorageOwnerProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        val uid = user?.uid
        if (uid == null) {
            isLoading = false
            message = "Please login again."
            return@LaunchedEffect
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                name = document.getString("name")
                    ?: document.getString("fullName")
                            ?: ""
                phone = document.getString("phone")
                    ?: document.getString("phoneNumber")
                            ?: ""
                email = user.email.orEmpty()
                isLoading = false
            }
            .addOnFailureListener {
                message = it.localizedMessage ?: "Unable to load profile."
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Text(
                "My Profile",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🏬", style = MaterialTheme.typography.displayMedium)
                Text(
                    "Storage Owner Account",
                    style = MaterialTheme.typography.titleLarge
                )

                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Name: ${name.ifBlank { "Not added" }}")
                    Text("Phone: ${phone.ifBlank { "Not added" }}")
                }

                Text("Email: ${email.ifBlank { "Not available" }}")
                Text("Account type: Storage Owner")

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isEditing) {
                    Button(
                        onClick = {
                            val uid = user?.uid
                            when {
                                uid == null -> message = "Please login again."
                                name.isBlank() -> message = "Enter your name."
                                phone.isNotBlank() &&
                                        !phone.matches(Regex("^[0-9]{10}$")) ->
                                    message = "Enter a valid 10-digit phone number."

                                else -> {
                                    isSaving = true
                                    message = ""

                                    // Update only profile fields.
                                    // The existing role field is preserved.
                                    db.collection("users").document(uid)
                                        .set(
                                            mapOf(
                                                "name" to name.trim(),
                                                "phone" to phone.trim(),
                                                "email" to email,
                                                "updatedAt" to System.currentTimeMillis()
                                            ),
                                            SetOptions.merge()
                                        )
                                        .addOnSuccessListener {
                                            isSaving = false
                                            isEditing = false
                                            message = "Profile updated successfully."
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            message = it.localizedMessage
                                                ?: "Unable to update profile."
                                        }
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }

                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Button(
                        onClick = {
                            message = ""
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }
                }

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}
