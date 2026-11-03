
package com.example.pikrakshak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val ProfileGreen = Color(0xFF14532D)
private val ProfileLightGreen = Color(0xFFF0FDF4)

@Composable
fun BuyerProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val uid = user?.uid

    var buyerName by remember {
        mutableStateOf(user?.displayName ?: "Buyer")
    }

    var phone by remember { mutableStateOf("") }
    var email = user?.email ?: "Email not available"

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Load saved buyer details from Firestore
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        buyerName = document.getString("name")
                            ?: document.getString("fullName")
                                    ?: buyerName

                        phone = document.getString("phone")
                            ?: document.getString("phoneNumber")
                                    ?: ""
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF7))
            .padding(20.dp)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ProfileGreen
                )
            }

            Text(
                "My Profile",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileGreen
            )
        }

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile icon
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(ProfileLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Buyer",
                        tint = ProfileGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    buyerName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileGreen
                )

                Text(
                    "BUYER ACCOUNT",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(25.dp))
                HorizontalDivider()
                Spacer(Modifier.height(18.dp))

                // Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = ProfileGreen
                    )

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            "Email Address",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            email,
                            color = Color(0xFF163321),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = ProfileGreen
                    )

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            "Phone Number",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            phone.ifBlank { "Not added yet" },
                            color = Color(0xFF163321),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Account type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = ProfileGreen
                    )

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            "Account Type",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            "Buyer",
                            color = Color(0xFF163321),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Edit Profile button
        if (!isEditing) {
            Button(
                onClick = {
                    message = ""
                    isEditing = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileGreen
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Edit Profile",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Edit fields
            OutlinedTextField(
                value = buyerName,
                onValueChange = { buyerName = it },
                label = { Text("Full Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        phone = it
                    }
                },
                label = { Text("Phone Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Text(
                message,
                color = if (message.startsWith("Profile saved")) {
                    ProfileGreen
                } else {
                    Color.Red
                },
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            // Save button
            Button(
                onClick = {
                    if (uid == null) {
                        message = "Please login again."
                    } else if (buyerName.isBlank()) {
                        message = "Please enter your name."
                    } else if (phone.isNotBlank() && phone.length != 10) {
                        message = "Enter a valid 10-digit phone number."
                    } else {
                        isSaving = true
                        message = ""

                        val buyerData = hashMapOf(
                            "name" to buyerName.trim(),
                            "phone" to phone,
                            "email" to email,
                            "role" to "BUYER",
                            "updatedAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(uid)
                            .set(buyerData, SetOptions.merge())
                            .addOnSuccessListener {
                                isSaving = false
                                isEditing = false
                                message = "Profile saved successfully!"
                            }
                            .addOnFailureListener { error ->
                                isSaving = false
                                message = error.localizedMessage
                                    ?: "Unable to save profile."
                            }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileGreen
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    isEditing = false
                    message = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cancel")
            }
        }

        Spacer(Modifier.height(18.dp))

        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileGreen
            )
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null
            )
            Spacer(Modifier.width(10.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}
