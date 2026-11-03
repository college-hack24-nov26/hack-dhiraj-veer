
package com.example.pikrakshak

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ""
)

object UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun saveUserRole(
        role: UserRole,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("User is not logged in.")
            return
        }

        val userData = mapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "role" to role.name
        )

        db.collection("users")
            .document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Could not save user role.")
            }
    }

    fun getUserRole(
        onSuccess: (UserRole) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("User is not logged in.")
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val roleValue = document.getString("role")
                val role = UserRole.fromValue(roleValue)

                if (role != null) {
                    onSuccess(role)
                } else {
                    onError("No valid role found for this account.")
                }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Could not load user role.")
            }
    }

    fun getUserProfile(
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("User is not logged in.")
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = UserProfile(
                    uid = user.uid,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: (user.email ?: ""),
                    phone = document.getString("phone") ?: "",
                    role = document.getString("role") ?: ""
                )
                onSuccess(profile)
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Could not load profile.")
            }
    }

    fun updateUserProfile(
        name: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("User is not logged in.")
            return
        }

        if (name.isBlank()) {
            onError("Please enter your name.")
            return
        }

        val updates = mapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "name" to name.trim(),
            "phone" to phone.trim()
        )

        db.collection("users")
            .document(user.uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Could not update profile.")
            }
    }
}
