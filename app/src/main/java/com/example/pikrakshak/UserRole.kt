
package com.example.pikrakshak

enum class UserRole(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    FARMER(
        "Farmer",
        "Manage crops and sell your produce",
        "🌾"
    ),
    BUYER(
        "Buyer",
        "Discover and purchase fresh produce",
        "🛒"
    ),
    SELLER(
        "Seller",
        "Manage your produce listings",
        "📦"
    ),
    STORAGE_OWNER(
        "Storage Owner",
        "Manage storage facilities and bookings",
        "🏬"
    );

    companion object {
        fun fromValue(value: String?): UserRole? {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}
