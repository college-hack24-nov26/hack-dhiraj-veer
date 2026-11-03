
package com.example.pikrakshak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val RoleGreen = Color(0xFF14532D)
private val RoleLightGreen = Color(0xFFF0FDF4)

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    onBack: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F8EC),
                        Color.White,
                        Color(0xFFF5FBF6)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                color = RoleGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) +
                    slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(600)
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌱", fontSize = 48.sp)

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Join PikRakshak",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoleGreen
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose how you want to use our platform",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        UserRole.entries.forEachIndexed { index, role ->
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 150 + index * 120
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 150 + index * 120
                    )
                )
            ) {
                RoleCard(
                    role = role,
                    isSelected = selectedRole == role,
                    onClick = { selectedRole = role }
                )
            }

            Spacer(Modifier.height(13.dp))
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "You can use your selected role whenever you log in.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(14.dp))

        val buttonScale by animateFloatAsState(
            targetValue = if (selectedRole != null) 1f else 0.97f,
            animationSpec = tween(200),
            label = "continueScale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(buttonScale)
                .background(
                    color = if (selectedRole != null) RoleGreen
                    else Color(0xFF9EBAA4),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(enabled = selectedRole != null) {
                    selectedRole?.let(onRoleSelected)
                }
                .padding(vertical = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue  →",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(180),
        label = "roleCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RoleLightGreen
            else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF22C55E)
            else Color(0xFFE5E7EB)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(
                        Color(0xFFE8F5E9),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(role.emoji, fontSize = 29.sp)
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = role.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoleGreen
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = role.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = if (isSelected) "✓" else "○",
                color = if (isSelected) Color(0xFF16A34A)
                else Color.LightGray,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
