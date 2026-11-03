
package com.example.pikrakshak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SmartRecommendation(
    val title: String,
    val riskLabel: String,
    val summary: String,
    val actions: List<String>,
    val color: Color
)

object SmartRecommendationEngine {

    fun generate(result: ShelfLifeResult): SmartRecommendation {
        return when {
            result.remainingDays == 0 ||
                    result.risk == SpoilageRisk.HIGH -> {
                SmartRecommendation(
                    title = "Take Action Now",
                    riskLabel = "HIGH RISK",
                    summary = "The estimated remaining shelf life is " +
                            "${result.remainingDays} day(s). Inspect the produce " +
                            "promptly and prioritize suitable action.",
                    actions = listOf(
                        "Inspect freshness and visible spoilage before selling.",
                        "Contact nearby buyers and plan timely transport.",
                        "Use suitable storage if it can help preserve the produce.",
                        "Do not sell or consume produce that appears spoiled."
                    ),
                    color = Color(0xFFB91C1C)
                )
            }

            result.risk == SpoilageRisk.MEDIUM -> {
                SmartRecommendation(
                    title = "Plan to Sell Soon",
                    riskLabel = "MEDIUM RISK",
                    summary = "The produce may have limited time remaining. " +
                            "Check its condition and prepare a selling or storage plan.",
                    actions = listOf(
                        "Contact buyers and check current market demand.",
                        "Keep produce in suitable, well-ventilated storage.",
                        "Avoid unnecessary handling and transport delays.",
                        "Arrange transport before quality declines."
                    ),
                    color = Color(0xFFB45309)
                )
            }

            else -> {
                SmartRecommendation(
                    title = "Maintain Good Storage",
                    riskLabel = "LOW RISK",
                    summary = "The rule-based estimate indicates more time " +
                            "remaining, but freshness still needs regular checking.",
                    actions = listOf(
                        "Keep produce in crop-appropriate storage conditions.",
                        "Avoid direct sunlight and unnecessary handling.",
                        "Monitor freshness and temperature regularly.",
                        "Plan selling according to market demand."
                    ),
                    color = Color(0xFF15803D)
                )
            }
        }
    }
}

@Composable
fun SmartRecommendationCard(
    result: ShelfLifeResult,
    modifier: Modifier = Modifier
) {
    val advice = SmartRecommendationEngine.generate(result)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SMART RECOMMENDATIONS",
                color = Color(0xFF14532D),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = advice.title,
                color = advice.color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = advice.riskLabel,
                color = advice.color,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = advice.summary,
                fontSize = 14.sp,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Recommended Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF14532D)
            )

            advice.actions.forEachIndexed { index, action ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        fontWeight = FontWeight.Bold,
                        color = advice.color
                    )

                    Text(
                        text = action,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF334155),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
