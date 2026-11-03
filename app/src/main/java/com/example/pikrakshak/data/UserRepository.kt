
package com.example.pikrakshak.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        role: String,
        city: String,
        state: String
    ): User {

        val result = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        val uid = result.user?.uid
            ?: throw Exception("Registration failed")

        val user = User(
            uid = uid,
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role,
            city = city.trim(),
            state = state.trim(),
            createdAt = System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .set(user)
            .await()

        return user
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): User {

        val result = auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        val uid = result.user?.uid
            ?: throw Exception("Login failed")

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
            ?: throw Exception("User profile not found")
    }

    suspend fun getCurrentUser(): User? {

        val uid = auth.currentUser?.uid ?: return null

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
    }

    fun logout() {
        auth.signOut()
    }
}
