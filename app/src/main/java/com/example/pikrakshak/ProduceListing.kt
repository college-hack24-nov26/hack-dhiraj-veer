package com.example.pikrakshak

data class ProduceListing(
    val id: String = "",
    val category: String = "Other",
    val cropName: String = "",
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val location: String = "",
    val phone: String = "",
    val farmerName: String = "",
    val sellerUid: String = "",
    val createdAt: Long = 0L,
    val isAvailable: Boolean = true
)