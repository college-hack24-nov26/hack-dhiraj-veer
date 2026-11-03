package com.example.pikrakshak

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MarketplaceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun detectCategory(cropName: String): String {
        val crop = cropName.trim().lowercase()

        val vegetables = setOf(
            "tomato", "onion", "potato", "carrot", "cabbage",
            "cauliflower", "brinjal", "eggplant", "okra", "cucumber",
            "spinach", "chilli", "capsicum", "beans", "peas", "radish"
        )

        val fruits = setOf(
            "mango", "banana", "apple", "grapes", "orange",
            "watermelon", "papaya", "guava", "pineapple",
            "pomegranate", "lemon", "strawberry"
        )

        return when {
            crop in vegetables -> "Vegetables"
            crop in fruits -> "Fruits"
            else -> "Other"
        }
    }

    private fun convertDocument(document: DocumentSnapshot): ProduceListing {
        val cropName = document.getString("cropName") ?: ""

        return ProduceListing(
            id = document.id,
            category = document.getString("category")
                ?: detectCategory(cropName),
            cropName = cropName,
            quantityKg = (document.get("quantityKg") as? Number)?.toDouble() ?: 0.0,
            pricePerKg = (document.get("pricePerKg") as? Number)?.toDouble() ?: 0.0,
            location = document.getString("location") ?: "",
            phone = document.getString("phone") ?: "",
            farmerName = document.getString("farmerName") ?: "",
            sellerUid = document.getString("sellerUid") ?: "",
            createdAt = (document.get("createdAt") as? Number)?.toLong() ?: 0L,
            isAvailable = document.getBoolean("isAvailable") ?: true
        )
    }

    fun listenToListings(
        onResult: (List<ProduceListing>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("produceListings")
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Unable to load listings.")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents?.map {
                    convertDocument(it)
                } ?: emptyList()

                onResult(result)
            }
    }

    fun listenToMyListings(
        onResult: (List<ProduceListing>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val user = auth.currentUser

        if (user == null) {
            onError("Please log in to view your listings.")
            return null
        }

        return db.collection("produceListings")
            .whereEqualTo("sellerUid", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Unable to load your listings.")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents?.map {
                    convertDocument(it)
                } ?: emptyList()

                onResult(result)
            }
    }

    fun addListing(
        cropName: String,
        quantityKg: Double,
        pricePerKg: Double,
        location: String,
        phone: String,
        farmerName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        category: String = detectCategory(cropName)
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("Please log in before adding a listing.")
            return
        }

        if (
            cropName.isBlank() ||
            quantityKg <= 0 ||
            pricePerKg <= 0 ||
            location.isBlank() ||
            phone.isBlank() ||
            farmerName.isBlank()
        ) {
            onError("Please enter valid details in every field.")
            return
        }

        val listing = hashMapOf(
            "category" to category,
            "cropName" to cropName.trim(),
            "quantityKg" to quantityKg,
            "pricePerKg" to pricePerKg,
            "location" to location.trim(),
            "phone" to phone.trim(),
            "farmerName" to farmerName.trim(),
            "sellerUid" to user.uid,
            "createdAt" to System.currentTimeMillis(),
            "isAvailable" to true
        )

        db.collection("produceListings")
            .add(listing)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Unable to save listing.")
            }
    }

    fun updateListing(
        listingId: String,
        quantityKg: Double,
        pricePerKg: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (quantityKg <= 0 || pricePerKg <= 0) {
            onError("Quantity and price must be greater than zero.")
            return
        }

        db.collection("produceListings")
            .document(listingId)
            .update(
                mapOf(
                    "quantityKg" to quantityKg,
                    "pricePerKg" to pricePerKg
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Unable to update listing.")
            }
    }

    fun markAsSold(
        listingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("produceListings")
            .document(listingId)
            .update("isAvailable", false)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Unable to mark as sold.")
            }
    }

    fun deleteListing(
        listingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("produceListings")
            .document(listingId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Unable to delete listing.")
            }
    }
}