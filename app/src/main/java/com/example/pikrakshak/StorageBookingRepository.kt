
package com.example.pikrakshak

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class StorageBookingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createBooking(
        facility: StorageFacility,
        quantityKg: Double,
        farmerPhone: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("Please login as a Farmer first.")
            return
        }

        if (quantityKg <= 0) {
            onError("Enter a valid quantity.")
            return
        }

        if (quantityKg > facility.availableCapacity) {
            onError("Requested quantity exceeds available capacity.")
            return
        }

        val booking = hashMapOf(
            "facilityId" to facility.id,
            "facilityName" to facility.name,
            "facilityOwnerUid" to facility.ownerUid,
            "farmerUid" to user.uid,
            "farmerEmail" to (user.email ?: ""),
            "farmerPhone" to farmerPhone,
            "quantityKg" to quantityKg,
            "pricePerKg" to facility.pricePerUnit,
            "estimatedTotalPrice" to quantityKg * facility.pricePerUnit,
            "notes" to notes,
            "bookingStatus" to "Pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("storageBookings")
            .add(booking)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.localizedMessage
                        ?: "Unable to submit booking request."
                )
            }
    }
}
