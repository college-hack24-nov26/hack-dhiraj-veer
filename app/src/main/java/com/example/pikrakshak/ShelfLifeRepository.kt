
package com.example.pikrakshak

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

object ShelfLifeRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveResult(
        crop: CropType,
        daysSinceHarvest: Int,
        temperatureCelsius: Double,
        refrigerated: Boolean,
        humidityPercent: Double,
        storageType: StorageType,
        maturity: MaturityLevel,
        condition: ProduceCondition,
        result: ShelfLifeResult,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val advice = SmartRecommendationEngine.generate(result)

        val record = hashMapOf(
            "cropName" to crop.displayName,
            "daysSinceHarvest" to daysSinceHarvest,
            "temperatureCelsius" to temperatureCelsius,
            "humidityPercent" to humidityPercent,
            "refrigerated" to refrigerated,
            "storageType" to storageType.name,
            "maturity" to maturity.name,
            "condition" to condition.name,
            "estimatedTotalDays" to result.estimatedTotalDays,
            "remainingDays" to result.remainingDays,
            "risk" to result.risk.name,
            "recommendation" to advice.title,
            "recommendedActions" to advice.actions,
            "savedAt" to Timestamp.now()
        )

        db.collection("shelfLifeResults")
            .add(record)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.localizedMessage
                        ?: "Could not save the result."
                )
            }
    }
}
