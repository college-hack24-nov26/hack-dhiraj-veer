
package com.example.pikrakshak.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Forest = Color(0xFF14532D)
private val Leaf = Color(0xFF22C55E)
private val Pale = Color(0xFFF0FDF4)

data class RoleOption(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    val roles = listOf(
        RoleOption(
            "Farmer",
            "Manage crops and sell your harvest",
            Icons.Default.Agriculture
        ),
        RoleOption(
            "Buyer",
            "Discover fresh agricultural produce",
            Icons.Default.Person
        ),
        RoleOption(
            "Seller",
            "Manage and sell available products",
            Icons.Default.Store
        ),
        RoleOption(
            "Storage Owner",
            "Manage storage facilities and capacity",
            Icons.Default.Business
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Pale)
            .padding(22.dp)
    ) {
        Spacer(Modifier.height(35.dp))

        Text(
            text = "PIKRAKSHAK",
            color = Forest,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(35.dp))

        Text(
            text = "Welcome to\nPikRakshak 🌱",
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold,
            color = Forest
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Choose your role to get started",
            color = Color.DarkGray,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(25.dp))

        roles.forEach { role ->
            RoleCard(
                role = role,
                onClick = { onRoleSelected(role.title) }
            )

            Spacer(Modifier.height(13.dp))
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Growing together • Reducing food waste",
            color = Forest,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun RoleCard(
    role: RoleOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Leaf.copy(alpha = 0.13f)
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = role.title,
                    tint = Forest,
                    modifier = Modifier.padding(13.dp).size(27.dp)
                )
            }

            Spacer(Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    role.title,
                    color = Forest,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    role.description,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Text(
                "›",
                color = Leaf,
                fontSize = 29.sp
            )
        }
    }
}
