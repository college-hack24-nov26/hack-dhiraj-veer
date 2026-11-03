
package com.example.pikrakshak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ForestGreen = Color(0xFF14532D)
private val LeafGreen = Color(0xFF22C55E)
private val LightGreen = Color(0xFFF0FDF4)

@Composable
fun PikRakshakDashboard(
    onShelfLife: () -> Unit,
    onFeatureSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGreen)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Namaste, Farmer! 👋",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreen
                )
                Text(
                    text = "PikRakshak 🌱",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestGreen
                )
            }

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen
                ),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Text("Logout")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Protect your harvest. Grow your future.",
            color = Color(0xFF52705B),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = ForestGreen
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "🌾 Smart Harvest Management",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Estimate shelf life, identify spoilage risks, " +
                            "and find better ways to protect your produce.",
                    color = Color(0xFFE0F2E5),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onShelfLife,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeafGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Check Shelf Life →")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Explore Features",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(Modifier.height(10.dp))

        val features = listOf(
            "⚠️" to "Spoilage Risk",
            "🛒" to "Produce Marketplace",
            "🏬" to "Nearby Storage",
            "🌿" to "Crop Recommendations",
            "📷" to "Disease Detection",
            "👤" to "My Profile"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { feature ->
                Card(
                    onClick = {
                        onFeatureSelected(feature.second)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = feature.first,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = feature.second,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }
    }
}
