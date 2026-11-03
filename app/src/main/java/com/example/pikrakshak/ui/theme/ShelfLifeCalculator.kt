
package com.example.pikrakshak

// --------------------------------------------------
// 1. CROP TYPES
// --------------------------------------------------

enum class CropType(
    val displayName: String,
    val baseShelfLifeDays: Int
) {
    TOMATO("Tomato", 10),
    MANGO("Mango", 7),
    BANANA("Banana", 7),
    APPLE("Apple", 30),
    ORANGE("Orange", 25),
    PAPAYA("Papaya", 7),
    GUAVA("Guava", 6),
    PINEAPPLE("Pineapple", 12),
    WATERMELON("Watermelon", 10),
    GRAPES("Grapes", 10),
    STRAWBERRY("Strawberry", 5),
    LEMON("Lemon", 25),

    ONION("Onion", 60),
    POTATO("Potato", 30),
    CUCUMBER("Cucumber", 7),
    CARROT("Carrot", 21),
    CABBAGE("Cabbage", 21),
    CAULIFLOWER("Cauliflower", 7),
    BRINJAL("Brinjal", 7),
    OKRA("Okra", 5),
    CAPSICUM("Capsicum", 12),
    BROCCOLI("Broccoli", 7),
    LEAFY_VEGETABLES("Leafy vegetables", 4)
}

// --------------------------------------------------
// 2. SPOILAGE RISK
// --------------------------------------------------

enum class SpoilageRisk {
    LOW,
    MEDIUM,
    HIGH
}

// --------------------------------------------------
// 3. STORAGE TYPE
// --------------------------------------------------

enum class StorageType(val label: String) {
    ROOM_TEMPERATURE("Room Temperature"),
    REFRIGERATOR("Refrigerator"),
    COLD_STORAGE("Cold Storage")
}

// --------------------------------------------------
// 4. MATURITY LEVEL
// --------------------------------------------------

enum class MaturityLevel(val label: String) {
    UNRIPE("Unripe"),
    MATURE("Mature"),
    RIPE("Ripe"),
    OVER_RIPE("Overripe")
}

// --------------------------------------------------
// 5. PRODUCE PHYSICAL CONDITION
// --------------------------------------------------

enum class ProduceCondition(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    DAMAGED("Damaged"),
    SPOILED("Spoiled")
}

// --------------------------------------------------
// 6. SHELF LIFE RESULT
// --------------------------------------------------

data class ShelfLifeResult(
    val estimatedTotalDays: Int,
    val remainingDays: Int,
    val risk: SpoilageRisk,
    val recommendation: String
)

// --------------------------------------------------
// 7. ADVANCED SHELF LIFE CALCULATOR
// --------------------------------------------------

object ShelfLifeCalculator {

    fun calculate(
        crop: CropType,
        daysSinceHarvest: Int,
        temperatureCelsius: Double,
        refrigerated: Boolean,
        humidityPercent: Double = 65.0,
        storageType: StorageType = if (refrigerated) {
            StorageType.REFRIGERATOR
        } else {
            StorageType.ROOM_TEMPERATURE
        },
        maturity: MaturityLevel = MaturityLevel.MATURE,
        condition: ProduceCondition = ProduceCondition.GOOD
    ): ShelfLifeResult {

        require(daysSinceHarvest >= 0) {
            "Days since harvest cannot be negative."
        }

        require(temperatureCelsius in -10.0..60.0) {
            "Temperature must be between -10 and 60°C."
        }

        require(humidityPercent in 0.0..100.0) {
            "Humidity must be between 0 and 100%."
        }

        // Starting crop-specific shelf-life estimate
        var estimatedDays = crop.baseShelfLifeDays.toDouble()

        // --------------------------------------------------
        // TEMPERATURE ADJUSTMENT
        // --------------------------------------------------

        when {
            temperatureCelsius > 35 -> estimatedDays *= 0.45
            temperatureCelsius > 30 -> estimatedDays *= 0.60
            temperatureCelsius > 25 -> estimatedDays *= 0.75
            temperatureCelsius < 10 -> estimatedDays *= 1.10
        }

        // --------------------------------------------------
        // STORAGE ADJUSTMENT
        // --------------------------------------------------

        when (storageType) {
            StorageType.ROOM_TEMPERATURE -> Unit

            StorageType.REFRIGERATOR ->
                estimatedDays *= 1.20

            StorageType.COLD_STORAGE ->
                estimatedDays *= 1.40
        }

        // --------------------------------------------------
        // HUMIDITY ADJUSTMENT
        // --------------------------------------------------

        when {
            humidityPercent > 90 ->
                estimatedDays *= 0.85

            humidityPercent < 40 ->
                estimatedDays *= 0.85

            humidityPercent in 60.0..85.0 ->
                estimatedDays *= 1.05
        }

        // --------------------------------------------------
        // MATURITY ADJUSTMENT
        // --------------------------------------------------

        when (maturity) {
            MaturityLevel.UNRIPE ->
                estimatedDays *= 1.10

            MaturityLevel.MATURE ->
                Unit

            MaturityLevel.RIPE ->
                estimatedDays *= 0.80

            MaturityLevel.OVER_RIPE ->
                estimatedDays *= 0.50
        }

        // --------------------------------------------------
        // PHYSICAL CONDITION ADJUSTMENT
        // --------------------------------------------------

        when (condition) {
            ProduceCondition.EXCELLENT ->
                estimatedDays *= 1.10

            ProduceCondition.GOOD ->
                Unit

            ProduceCondition.DAMAGED ->
                estimatedDays *= 0.50

            ProduceCondition.SPOILED ->
                estimatedDays *= 0.10
        }

        // --------------------------------------------------
        // TOTAL AND REMAINING SHELF LIFE
        // --------------------------------------------------

        val totalDays = estimatedDays
            .toInt()
            .coerceAtLeast(1)

        val remainingDays = (totalDays - daysSinceHarvest)
            .coerceAtLeast(0)

        // --------------------------------------------------
        // SPOILAGE RISK CALCULATION
        // --------------------------------------------------

        val risk = when {
            condition == ProduceCondition.SPOILED ->
                SpoilageRisk.HIGH

            remainingDays == 0 ->
                SpoilageRisk.HIGH

            remainingDays <= 2 ->
                SpoilageRisk.HIGH

            temperatureCelsius > 35 ->
                SpoilageRisk.HIGH

            condition == ProduceCondition.DAMAGED ->
                SpoilageRisk.HIGH

            remainingDays <= totalDays * 0.35 ->
                SpoilageRisk.MEDIUM

            else ->
                SpoilageRisk.LOW
        }

        // --------------------------------------------------
        // SMART RECOMMENDATION
        // --------------------------------------------------

        val recommendation = when (risk) {

            SpoilageRisk.HIGH ->
                "Inspect the produce immediately. " +
                        "Prioritize selling or arrange suitable storage and transport."

            SpoilageRisk.MEDIUM ->
                "Check produce condition regularly and plan selling soon. " +
                        "Maintain suitable storage conditions."

            SpoilageRisk.LOW ->
                "Continue monitoring freshness and maintain " +
                        "appropriate storage conditions."
        }

        // --------------------------------------------------
        // RETURN RESULT
        // --------------------------------------------------

        return ShelfLifeResult(
            estimatedTotalDays = totalDays,
            remainingDays = remainingDays,
            risk = risk,
            recommendation = recommendation
        )
    }
}
