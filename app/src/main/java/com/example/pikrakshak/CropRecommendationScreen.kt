
package com.example.pikrakshak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CropRecommendationScreen(
    onBack: () -> Unit
) {
    var season by remember { mutableStateOf("Kharif") }
    var soil by remember { mutableStateOf("Black Soil") }
    var water by remember { mutableStateOf("Moderate") }
    var showResults by remember { mutableStateOf(false) }

    val recommendations = when {
        season == "Kharif" && soil == "Black Soil" ->
            listOf(
                "Cotton" to "Suitable for black soil during Kharif.",
                "Soybean" to "A common Kharif crop for suitable soil and rainfall.",
                "Pigeon Pea (Tur)" to "Can be considered as a pulse crop."
            )

        season == "Rabi" && soil == "Black Soil" ->
            listOf(
                "Wheat" to "Can be grown in suitable Rabi conditions.",
                "Chickpea (Harbhara)" to "A suitable pulse option for many black-soil areas.",
                "Jowar" to "A traditional dryland crop option."
            )

        season == "Summer" && water == "High" ->
            listOf(
                "Watermelon" to "Consider where irrigation and market access are available.",
                "Muskmelon" to "Requires suitable warm weather and irrigation.",
                "Cucumber" to "Can be considered with regular water supply."
            )

        water == "Low" ->
            listOf(
                "Pearl Millet (Bajra)" to "A drought-tolerant crop option.",
                "Sorghum (Jowar)" to "A common option for lower-water conditions.",
                "Pigeon Pea (Tur)" to "Can be considered in suitable rainfed conditions."
            )

        else ->
            listOf(
                "Maize" to "Consider based on local rainfall and soil conditions.",
                "Groundnut" to "Can be considered where soil and water are suitable.",
                "Green Gram (Moong)" to "A pulse option for suitable growing conditions."
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            "Crop Recommendations 🌱",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF14532D)
        )

        Text(
            "Choose your farm conditions to view suggested crops.",
            color = Color.DarkGray
        )

        RecommendationDropdown(
            label = "Growing Season",
            selected = season,
            options = listOf("Kharif", "Rabi", "Summer"),
            onSelected = {
                season = it
                showResults = false
            }
        )

        RecommendationDropdown(
            label = "Soil Type",
            selected = soil,
            options = listOf(
                "Black Soil",
                "Red Soil",
                "Sandy Soil",
                "Alluvial Soil"
            ),
            onSelected = {
                soil = it
                showResults = false
            }
        )

        RecommendationDropdown(
            label = "Water Availability",
            selected = water,
            options = listOf("Low", "Moderate", "High"),
            onSelected = {
                water = it
                showResults = false
            }
        )

        Button(
            onClick = { showResults = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Crop Recommendations")
        }

        if (showResults) {
            Text(
                "Suggested Crops",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF14532D)
            )

            recommendations.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "${index + 1}. ${item.first}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF14532D)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(item.second)
                    }
                }
            }

            Text(
                "Note: These are general rule-based suggestions, not guaranteed " +
                        "crop suitability predictions. Check local weather, soil test " +
                        "results, irrigation, and advice from your agriculture officer " +
                        "before planting.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun RecommendationDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(Modifier.height(6.dp))

        androidx.compose.foundation.layout.Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) }
            )

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 8.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    TextButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("")
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
