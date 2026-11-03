
package com.example.pikrakshak

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

data class StorageFacility(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val ownerName: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val storageType: String = "",
    val totalCapacity: Double = 0.0,
    val availableCapacity: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true
)

class StorageRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val facilities = db.collection("storageFacilities")

    fun addFacility(
        facility: StorageFacility,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Please login again.")
            return
        }

        facilities.add(facility.toMap(uid))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Unable to add storage facility.")
            }
    }

    fun listenToMyFacilities(
        onResult: (List<StorageFacility>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Please login again.")
            return null
        }

        return facilities
            .whereEqualTo("ownerUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Unable to load your facilities.")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(StorageFacility::class.java)
                        ?.copy(id = document.id)
                }

                onResult(result)
            }
    }

    fun listenToActiveFacilities(
        onResult: (List<StorageFacility>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return facilities
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Unable to load storage facilities.")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(StorageFacility::class.java)
                        ?.copy(id = document.id)
                }

                onResult(result)
            }
    }

    fun updateFacility(
        facility: StorageFacility,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Please login again.")
            return
        }

        if (facility.id.isBlank() || facility.ownerUid != uid) {
            onError("You can only update your own storage facility.")
            return
        }

        facilities.document(facility.id)
            .set(facility.toMap(uid), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Unable to update facility.")
            }
    }

    fun deleteFacility(
        facilityId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Please login again.")
            return
        }

        if (facilityId.isBlank()) {
            onError("Invalid storage facility.")
            return
        }

        facilities.document(facilityId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Unable to delete facility.")
            }
    }

    private fun StorageFacility.toMap(uid: String): Map<String, Any> {
        return hashMapOf(
            "ownerUid" to uid,
            "name" to name,
            "ownerName" to ownerName,
            "address" to address,
            "city" to city,
            "state" to state,
            "storageType" to storageType,
            "totalCapacity" to totalCapacity,
            "availableCapacity" to availableCapacity,
            "pricePerUnit" to pricePerUnit,
            "phone" to phone,
            "latitude" to latitude,
            "longitude" to longitude,
            "isActive" to isActive
        )
    }
}
