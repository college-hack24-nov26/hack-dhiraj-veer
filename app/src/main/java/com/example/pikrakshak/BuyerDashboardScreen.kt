
package com.example.pikrakshak

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private val BuyerGreen = Color(0xFF14532D)
private val BuyerLightGreen = Color(0xFFF0FDF4)
private val BuyerAccent = Color(0xFF22C55E)

private data class BuyerRecentListing(
    val id: String,
    val cropName: String,
    val category: String,
    val quantityKg: Double,
    val pricePerKg: Double,
    val location: String,
    val farmerName: String,
    val phone: String
)

@Composable
fun BuyerDashboardScreen(
    onMarketplaceClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContent by remember { mutableStateOf(false) }

    var recentListings by remember {
        mutableStateOf<List<BuyerRecentListing>>(emptyList())
    }

    var isLoadingListings by remember { mutableStateOf(true) }
    var listingError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Listen to the latest marketplace listings from Firestore
    DisposableEffect(Unit) {
        val registration = FirebaseFirestore.getInstance()
            .collection("produceListings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    listingError = error.localizedMessage
                        ?: "Unable to load produce listings."
                    isLoadingListings = false
                } else if (snapshot != null) {
                    recentListings = snapshot.documents.mapNotNull { document ->

                        val available =
                            document.getBoolean("isAvailable") ?: true

                        if (!available) {
                            null
                        } else {
                            BuyerRecentListing(
                                id = document.id,
                                cropName = document.getString("cropName") ?: "Produce",
                                category = document.getString("category") ?: "Other",
                                quantityKg = document.getDouble("quantityKg") ?: 0.0,
                                pricePerKg = document.getDouble("pricePerKg") ?: 0.0,
                                location = document.getString("location") ?: "Location not added",
                                farmerName = document.getString("farmerName") ?: "Farmer",
                                phone = document.getString("phone") ?: ""
                            )
                        }
                    }

                    listingError = ""
                    isLoadingListings = false
                }
            }

        onDispose {
            registration.remove()
        }
    }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Scaffold(
        containerColor = Color(0xFFF7FAF7),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, 0),
                    Triple("Market", Icons.Default.Storefront, 1),
                    Triple("Storage", Icons.Default.Warehouse, 2),
                    Triple("Profile", Icons.Default.Person, 3)
                )

                tabs.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                1 -> onMarketplaceClick()
                                2 -> onStorageClick()
                                3 -> onProfileClick()
                            }
                        },
                        icon = {
                            Icon(icon, contentDescription = label)
                        },
                        label = {
                            Text(label)
                        }
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Welcome header
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600)) +
                            slideInVertically(
                                initialOffsetY = { -it / 3 },
                                animationSpec = tween(600)
                            )
                ) {
                    BuyerWelcomeHeader()
                }
            }

            // Statistics
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(700)
                            )
                ) {
                    BuyerStatsSection()
                }
            }

            // Explore heading
            item {
                Text(
                    "Explore PikRakshak",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF163321)
                )
            }

            // Marketplace card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(800))
                ) {
                    BuyerFeatureCard(
                        title = "Fresh Produce Marketplace",
                        subtitle = "Discover fresh fruits and vegetables directly from farmers.",
                        icon = Icons.Default.ShoppingBasket,
                        onClick = onMarketplaceClick
                    )
                }
            }

            // Nearby storage card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(900))
                ) {
                    BuyerFeatureCard(
                        title = "Nearby Storage",
                        subtitle = "Explore available storage facilities.",
                        icon = Icons.Default.Warehouse,
                        onClick = onStorageClick
                    )
                }
            }

            // Recent produce heading
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Recent Produce Listings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF163321)
                        )
                        Text(
                            "Fresh listings from farmers",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    TextButton(onClick = onMarketplaceClick) {
                        Text(
                            "View All",
                            color = BuyerGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Loading state
            if (isLoadingListings) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BuyerGreen)
                    }
                }
            }

            // Error state
            else if (listingError.isNotBlank()) {
                item {
                    Text(
                        listingError,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            // Empty state
            else if (recentListings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = BuyerAccent,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "No produce listings yet",
                                fontWeight = FontWeight.Bold,
                                color = BuyerGreen
                            )

                            Text(
                                "New farmer listings will appear here.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Recent listings
            else {
                items(
                    items = recentListings,
                    key = { it.id }
                ) { listing ->

                    BuyerRecentListingCard(
                        listing = listing,
                        onContactClick = {
                            if (listing.phone.isNotBlank()) {
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${listing.phone}")
                                )
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            // Footer
            item {
                Text(
                    "Growing Together with Farmers 🌱",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = BuyerGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BuyerWelcomeHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BuyerGreen)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF14532D),
                            Color(0xFF166534),
                            Color(0xFF22A447)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "PIKRAKSHAK",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    "Welcome, Buyer! 👋",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Discover fresh produce. Connect directly with farmers.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )

                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        "Fresh • Local • Direct",
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BuyerStatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BuyerStatCard(
            modifier = Modifier.weight(1f),
            title = "Fresh Choices",
            value = "Explore",
            icon = Icons.Default.Eco
        )

        BuyerStatCard(
            modifier = Modifier.weight(1f),
            title = "Direct Access",
            value = "Farmers",
            icon = Icons.Default.Handshake
        )
    }
}

@Composable
private fun BuyerStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BuyerAccent,
                modifier = Modifier.size(27.dp)
            )

            Text(
                value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = BuyerGreen
            )

            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun BuyerFeatureCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(140),
        label = "buyerCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                pressed = true
                onClick()
                pressed = false
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Surface(
                modifier = Modifier.size(55.dp),
                shape = RoundedCornerShape(17.dp),
                color = BuyerLightGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = BuyerGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF163321)
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }

            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "Open",
                tint = BuyerAccent,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun BuyerRecentListingCard(
    listing: BuyerRecentListing,
    onContactClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        listing.cropName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BuyerGreen
                    )

                    Text(
                        listing.category,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = BuyerLightGreen,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        "Available",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = BuyerGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE8F0E8))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Price",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        "₹${"%.2f".format(listing.pricePerKg)}/kg",
                        color = BuyerGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        "Quantity",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        "${"%.2f".format(listing.quantityKg)} kg",
                        color = Color(0xFF163321),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BuyerAccent,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    listing.location,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = BuyerGreen,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    "Farmer: ${listing.farmerName}",
                    fontSize = 12.sp,
                    color = Color(0xFF163321)
                )
            }

            Button(
                onClick = onContactClick,
                enabled = listing.phone.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BuyerGreen
                )
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    if (listing.phone.isBlank()) {
                        "Phone Number Unavailable"
                    } else {
                        "Contact Farmer"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
