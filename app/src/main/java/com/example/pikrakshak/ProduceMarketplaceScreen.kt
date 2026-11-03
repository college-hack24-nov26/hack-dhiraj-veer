
package com.example.pikrakshak

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private val ForestGreen = Color(0xFF14532D)
private val LeafGreen = Color(0xFF166534)
private val LightGreen = Color(0xFFF0FDF4)
private val MutedText = Color(0xFF64748B)

// ---------------------------------------------------------
// PRODUCE IMAGES
// ---------------------------------------------------------

private fun getProduceImage(cropName: String): Int? {
    return when (cropName.trim().lowercase().replace(" ", "_")) {
        "tomato" -> R.drawable.tomato
        "onion" -> R.drawable.onion
        "potato" -> R.drawable.potato
        "carrot" -> R.drawable.carrot
        "cabbage" -> R.drawable.cabbage
        "cauliflower" -> R.drawable.cauliflower
        "brinjal", "eggplant" -> R.drawable.brinjal
        "okra", "ladyfinger" -> R.drawable.okra
        "cucumber" -> R.drawable.cucumber
        "spinach" -> R.drawable.spinach
        "chilli", "green_chilli" -> R.drawable.green_chilli
        "capsicum", "bell_pepper" -> R.drawable.capsicum
        "beans" -> R.drawable.beans
        "peas" -> R.drawable.peas
        "radish" -> R.drawable.radish
        "mango" -> R.drawable.mango
        "banana" -> R.drawable.banana
        "apple" -> R.drawable.apple
        "grapes" -> R.drawable.grapes
        "orange" -> R.drawable.orange
        "watermelon" -> R.drawable.watermelon
        "papaya" -> R.drawable.papaya
        "guava" -> R.drawable.guava
        "pineapple" -> R.drawable.pineapple
        "pomegranate" -> R.drawable.pomegranate
        "lemon" -> R.drawable.lemon
        "strawberry" -> R.drawable.strawberry
        else -> null
    }
}

@Composable
private fun ProduceImage(
    cropName: String,
    imageSize: Dp = 76.dp
) {
    val imageResource = getProduceImage(cropName)

    Surface(
        modifier = Modifier.size(imageSize),
        shape = RoundedCornerShape(18.dp),
        color = LightGreen
    ) {
        if (imageResource != null) {
            Image(
                painter = painterResource(id = imageResource),
                contentDescription = cropName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 30.sp)
            }
        }
    }
}

// ---------------------------------------------------------
// REUSABLE DROPDOWN
// ---------------------------------------------------------

@Composable
private fun MarketplaceDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember(label) {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Text(
                    if (expanded) "▲" else "▼",
                    color = ForestGreen
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp)
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

// ---------------------------------------------------------
// MAIN MARKETPLACE SCREEN
// ---------------------------------------------------------

@Composable
fun ProduceMarketplaceScreen(
    onBack: () -> Unit = {}
) {
    val repository = remember { MarketplaceRepository() }
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    val currentUid = auth.currentUser?.uid

    // -----------------------------------------------------
    // USER ROLE
    // -----------------------------------------------------

    var currentUserRole by remember(currentUid) {
        mutableStateOf("")
    }

    var isRoleLoading by remember(currentUid) {
        mutableStateOf(true)
    }

    DisposableEffect(currentUid) {
        var roleListener: ListenerRegistration? = null

        if (currentUid != null) {
            roleListener = firestore
                .collection("users")
                .document(currentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        currentUserRole = snapshot
                            .getString("role")
                            ?.trim()
                            ?.uppercase()
                            ?: ""
                    }

                    isRoleLoading = false
                }
        } else {
            currentUserRole = ""
            isRoleLoading = false
        }

        onDispose {
            roleListener?.remove()
        }
    }

    val canManageListings =
        currentUserRole == "FARMER" ||
                currentUserRole == "SELLER"

    // -----------------------------------------------------
    // SCREEN STATE
    // -----------------------------------------------------

    var listings by remember {
        mutableStateOf<List<ProduceListing>>(emptyList())
    }

    var myListings by remember {
        mutableStateOf<List<ProduceListing>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isMyListingsLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var myListingsError by remember {
        mutableStateOf("")
    }

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var showMyListings by remember {
        mutableStateOf(false)
    }

    var selectedListing by remember {
        mutableStateOf<ProduceListing?>(null)
    }

    var listingToEdit by remember {
        mutableStateOf<ProduceListing?>(null)
    }

    var pendingActionListing by remember {
        mutableStateOf<ProduceListing?>(null)
    }

    var pendingActionType by remember {
        mutableStateOf("")
    }

    var isActionRunning by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var selectedSort by remember {
        mutableStateOf("Default")
    }

    var showSortMenu by remember {
        mutableStateOf(false)
    }

    // -----------------------------------------------------
    // LISTEN TO PUBLIC LISTINGS
    // -----------------------------------------------------

    DisposableEffect(repository) {
        val registration = repository.listenToListings(
            onResult = { result ->
                listings = result
                isLoading = false
                errorMessage = ""
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )

        onDispose {
            registration.remove()
        }
    }

    // -----------------------------------------------------
    // LISTEN TO MY LISTINGS
    // -----------------------------------------------------

    DisposableEffect(
        repository,
        showMyListings,
        canManageListings
    ) {
        val registration: ListenerRegistration? =
            if (showMyListings && canManageListings) {
                isMyListingsLoading = true

                repository.listenToMyListings(
                    onResult = { result ->
                        myListings = result
                        isMyListingsLoading = false
                        myListingsError = ""
                    },
                    onError = { error ->
                        isMyListingsLoading = false
                        myListingsError = error
                    }
                )
            } else {
                null
            }

        onDispose {
            registration?.remove()
        }
    }

    // -----------------------------------------------------
    // FILTER AND SORT
    // -----------------------------------------------------

    val filteredListings = listings
        .filter { listing ->
            val crop = listing.cropName.trim().lowercase()

            val matchesSearch = crop.contains(
                searchQuery.trim(),
                ignoreCase = true
            )

            val vegetableNames = listOf(
                "tomato", "onion", "potato", "carrot",
                "cabbage", "cauliflower", "brinjal",
                "eggplant", "okra", "ladyfinger",
                "cucumber", "spinach", "chilli",
                "capsicum", "beans", "peas", "radish"
            )

            val fruitNames = listOf(
                "mango", "banana", "apple", "grapes",
                "orange", "watermelon", "papaya",
                "guava", "pineapple", "pomegranate",
                "lemon", "strawberry"
            )

            val matchesCategory = when (selectedCategory) {
                "Vegetables" ->
                    listing.category == "Vegetables" ||
                            (listing.category == "Other" && crop in vegetableNames)

                "Fruits" ->
                    listing.category == "Fruits" ||
                            (listing.category == "Other" && crop in fruitNames)

                else -> true
            }

            matchesSearch && matchesCategory
        }
        .let { result ->
            when (selectedSort) {
                "Price: Low to High" ->
                    result.sortedBy { it.pricePerKg }

                "Price: High to Low" ->
                    result.sortedByDescending { it.pricePerKg }

                "Quantity: Highest First" ->
                    result.sortedByDescending { it.quantityKg }

                else -> result
            }
        }

    // -----------------------------------------------------
    // MAIN SCREEN CONTENT
    // -----------------------------------------------------

    if (isRoleLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LeafGreen)
        }
    } else if (selectedListing != null) {

        ProductDetailsScreen(
            listing = selectedListing!!,
            onBack = { selectedListing = null }
        )

    } else if (showMyListings && canManageListings) {

        // ================================================
        // MY LISTINGS SCREEN
        // ================================================

        Scaffold(
            containerColor = Color(0xFFF7FBF7),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = { showMyListings = false }
                    ) {
                        Text(
                            "← Back to Marketplace",
                            color = ForestGreen
                        )
                    }

                    Text(
                        "My Listings 🌾",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Text(
                        "Manage your own produce listings",
                        fontSize = 14.sp,
                        color = MutedText
                    )
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "${myListings.size} listing(s)",
                    color = LeafGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                when {
                    isMyListingsLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(24.dp),
                            color = LeafGreen
                        )
                    }

                    myListingsError.isNotBlank() -> {
                        Text(
                            myListingsError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    myListings.isEmpty() -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🌾", fontSize = 36.sp)

                                Text(
                                    "No listings found",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )

                                Text(
                                    "Add produce in the marketplace to see it here.",
                                    color = MutedText
                                )

                                Button(
                                    onClick = {
                                        showMyListings = false
                                        showAddDialog = true
                                    }
                                ) {
                                    Text("Add Produce")
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = myListings,
                                key = { listing -> listing.id }
                            ) { listing ->
                                MyProduceListingCard(
                                    listing = listing,
                                    onEdit = {
                                        listingToEdit = listing
                                    },
                                    onMarkSold = {
                                        pendingActionListing = listing
                                        pendingActionType = "sold"
                                    },
                                    onDelete = {
                                        pendingActionListing = listing
                                        pendingActionType = "delete"
                                    }
                                )
                            }

                            item {
                                Spacer(
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    } else {

        // ================================================
        // PUBLIC MARKETPLACE SCREEN
        // ================================================

        Scaffold(
            containerColor = Color(0xFFF7FBF7),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(onClick = onBack) {
                        Text(
                            "← Back to Home",
                            color = ForestGreen
                        )
                    }

                    Text(
                        "Fresh from the farm 🌱",
                        fontSize = 13.sp,
                        color = LeafGreen,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Produce Marketplace",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Text(
                        "Discover fresh produce. Connect directly with sellers.",
                        fontSize = 14.sp,
                        color = MutedText
                    )

                    if (canManageListings) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("+ Add Produce")
                            }

                            Button(
                                onClick = {
                                    myListings = emptyList()
                                    isMyListingsLoading = true
                                    myListingsError = ""
                                    showMyListings = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("My Listings")
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(24.dp),
                        color = LeafGreen
                    )
                } else if (errorMessage.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Unable to load produce",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Text(errorMessage)
                        }
                    }
                } else {

                    // SEARCH
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = {
                            Text("Search fruits and vegetables")
                        },
                        placeholder = {
                            Text("Try Tomato, Mango, Onion...")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    // CATEGORY FILTERS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "All",
                            "Vegetables",
                            "Fruits"
                        ).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                },
                                label = { Text(category) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    // SORTING
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        TextButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Text(
                                "↕ Sort: $selectedSort",
                                color = ForestGreen
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = {
                                showSortMenu = false
                            }
                        ) {
                            listOf(
                                "Default",
                                "Price: Low to High",
                                "Price: High to Low",
                                "Quantity: Highest First"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedSort = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        "${filteredListings.size} matching listing(s)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LeafGreen,
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 12.dp
                        )
                    )

                    if (filteredListings.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (listings.isEmpty()) "🌾" else "🔎",
                                    fontSize = 36.sp
                                )

                                Text(
                                    if (listings.isEmpty()) {
                                        "No produce listed yet"
                                    } else {
                                        "No matching produce found"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = ForestGreen
                                )

                                Text(
                                    if (listings.isEmpty()) {
                                        "Fresh produce listings will appear here."
                                    } else {
                                        "Try another crop name, or clear your search."
                                    },
                                    color = MutedText
                                )

                                if (listings.isEmpty() && canManageListings) {
                                    Button(
                                        onClick = {
                                            showAddDialog = true
                                        }
                                    ) {
                                        Text("Add Your First Listing")
                                    }
                                } else if (listings.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            searchQuery = ""
                                            selectedCategory = "All"
                                            selectedSort = "Default"
                                        }
                                    ) {
                                        Text(
                                            "Clear Search",
                                            color = LeafGreen
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = filteredListings,
                                key = { listing -> listing.id }
                            ) { listing ->
                                ProduceListingCard(
                                    listing = listing,
                                    onClick = {
                                        selectedListing = listing
                                    }
                                )
                            }

                            item {
                                Spacer(
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------
    // ADD LISTING DIALOG
    // -----------------------------------------------------

    if (showAddDialog && canManageListings) {
        AddProduceDialog(
            repository = repository,
            onDismiss = { showAddDialog = false }
        )
    }

    // -----------------------------------------------------
    // EDIT LISTING DIALOG
    // -----------------------------------------------------

    if (canManageListings) {
        listingToEdit?.let { listing ->
            EditProduceDialog(
                listing = listing,
                repository = repository,
                onDismiss = { listingToEdit = null }
            )
        }
    }

    // -----------------------------------------------------
    // DELETE / MARK SOLD CONFIRMATION
    // -----------------------------------------------------

    if (canManageListings) {
        pendingActionListing?.let { listing ->

            val isDelete = pendingActionType == "delete"

            AlertDialog(
                onDismissRequest = {
                    if (!isActionRunning) {
                        pendingActionListing = null
                        pendingActionType = ""
                    }
                },
                title = {
                    Text(
                        if (isDelete) {
                            "Delete Listing?"
                        } else {
                            "Mark as Sold?"
                        }
                    )
                },
                text = {
                    Text(
                        if (isDelete) {
                            "Are you sure you want to permanently delete ${listing.cropName}?"
                        } else {
                            "Mark ${listing.cropName} as sold? It will no longer appear in the public marketplace."
                        }
                    )
                },
                confirmButton = {
                    Button(
                        enabled = !isActionRunning,
                        onClick = {
                            isActionRunning = true
                            myListingsError = ""

                            val onSuccess: () -> Unit = {
                                isActionRunning = false
                                pendingActionListing = null
                                pendingActionType = ""
                            }

                            val onError: (String) -> Unit = { error ->
                                isActionRunning = false
                                myListingsError = error
                            }

                            if (isDelete) {
                                repository.deleteListing(
                                    listingId = listing.id,
                                    onSuccess = onSuccess,
                                    onError = onError
                                )
                            } else {
                                repository.markAsSold(
                                    listingId = listing.id,
                                    onSuccess = onSuccess,
                                    onError = onError
                                )
                            }
                        }
                    ) {
                        Text(
                            when {
                                isActionRunning -> "Please wait..."
                                isDelete -> "Delete"
                                else -> "Mark as Sold"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isActionRunning,
                        onClick = {
                            pendingActionListing = null
                            pendingActionType = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------
// PUBLIC PRODUCE LISTING CARD
// ---------------------------------------------------------

@Composable
private fun ProduceListingCard(
    listing: ProduceListing,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    var isVisible by remember(listing.id) {
        mutableStateOf(false)
    }

    LaunchedEffect(listing.id) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(450)) +
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(450)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .animateContentSize(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProduceImage(listing.cropName, 76.dp)

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            listing.cropName.replaceFirstChar {
                                it.uppercase()
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )

                        Text(
                            listing.category,
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "₹${listing.pricePerKg}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeafGreen
                        )

                        Text(
                            "per kg",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightGreen
                ) {
                    Text(
                        "📦  ${listing.quantityKg} kg available",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LeafGreen
                    )
                }

                Text(
                    "📍  ${listing.location}",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )

                Text(
                    "👨‍🌾  ${listing.farmerName}",
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:${listing.phone}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "☎  Contact Seller",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PRODUCT DETAILS SCREEN
// ---------------------------------------------------------

@Composable
private fun ProductDetailsScreen(
    listing: ProduceListing,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF7FBF7),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        "← Back to Marketplace",
                        color = ForestGreen
                    )
                }

                Text(
                    "Product Details",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )

                Text(
                    "Fresh produce directly from the seller",
                    fontSize = 14.sp,
                    color = MutedText
                )
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProduceImage(listing.cropName, 150.dp)

                        Text(
                            listing.cropName.replaceFirstChar {
                                it.uppercase()
                            },
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )

                        Text(
                            listing.category,
                            color = MutedText
                        )

                        Text(
                            "₹${listing.pricePerKg} / kg",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeafGreen
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Produce Information",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )

                        DetailRow(
                            "Available Quantity",
                            "${listing.quantityKg} kg"
                        )

                        DetailRow(
                            "Price per kg",
                            "₹${listing.pricePerKg}"
                        )

                        DetailRow(
                            "Total Value",
                            "₹${listing.quantityKg * listing.pricePerKg}"
                        )

                        DetailRow("Location", listing.location)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Seller Information",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )

                        DetailRow("Seller Name", listing.farmerName)
                        DetailRow("Location", listing.location)
                        DetailRow("Contact Number", listing.phone)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:${listing.phone}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "☎  Contact Seller",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ---------------------------------------------------------
// DETAIL ROW
// ---------------------------------------------------------

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MutedText,
            fontSize = 14.sp
        )

        Text(
            value,
            color = ForestGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------
// MY PRODUCE LISTING CARD
// ---------------------------------------------------------

@Composable
private fun MyProduceListingCard(
    listing: ProduceListing,
    onEdit: () -> Unit,
    onMarkSold: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProduceImage(listing.cropName, 68.dp)

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        listing.cropName.replaceFirstChar {
                            it.uppercase()
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Text(
                        "₹${listing.pricePerKg} per kg",
                        color = LeafGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (listing.isAvailable) {
                        LightGreen
                    } else {
                        Color(0xFFFFF1F2)
                    }
                ) {
                    Text(
                        if (listing.isAvailable) "Available" else "Sold",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        color = if (listing.isAvailable) {
                            LeafGreen
                        } else {
                            Color(0xFFBE123C)
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                "Quantity: ${listing.quantityKg} kg",
                color = MutedText
            )

            Text(
                "Location: ${listing.location}",
                color = MutedText
            )

            Text(
                "Seller: ${listing.farmerName}",
                color = MutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    enabled = listing.isAvailable
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = onMarkSold,
                    modifier = Modifier.weight(1f),
                    enabled = listing.isAvailable
                ) {
                    Text("Mark Sold")
                }
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Delete Listing",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ---------------------------------------------------------
// EDIT PRODUCE DIALOG
// ---------------------------------------------------------

@Composable
private fun EditProduceDialog(
    listing: ProduceListing,
    repository: MarketplaceRepository,
    onDismiss: () -> Unit
) {
    var quantity by remember(listing.id) {
        mutableStateOf(listing.quantityKg.toString())
    }

    var price by remember(listing.id) {
        mutableStateOf(listing.pricePerKg.toString())
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text("Edit ${listing.cropName}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity in kg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per kg (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val quantityValue = quantity.toDoubleOrNull()
                    val priceValue = price.toDoubleOrNull()

                    if (
                        quantityValue == null ||
                        priceValue == null ||
                        !quantityValue.isFinite() ||
                        !priceValue.isFinite() ||
                        quantityValue <= 0 ||
                        priceValue <= 0
                    ) {
                        message = "Enter valid quantity and price."
                    } else {
                        isSaving = true
                        message = ""

                        repository.updateListing(
                            listingId = listing.id,
                            quantityKg = quantityValue,
                            pricePerKg = priceValue,
                            onSuccess = onDismiss,
                            onError = { error ->
                                isSaving = false
                                message = error
                            }
                        )
                    }
                }
            ) {
                Text(if (isSaving) "Updating..." else "Update")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------
// ADD PRODUCE DIALOG
// ---------------------------------------------------------

@Composable
private fun AddProduceDialog(
    repository: MarketplaceRepository,
    onDismiss: () -> Unit
) {
    var category by remember {
        mutableStateOf("Vegetables")
    }

    var cropName by remember {
        mutableStateOf("Tomato")
    }

    var quantity by remember {
        mutableStateOf("")
    }

    var quantityUnit by remember {
        mutableStateOf("Kg")
    }

    var price by remember {
        mutableStateOf("")
    }

    var priceUnit by remember {
        mutableStateOf("Per Kg")
    }

    var location by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var farmerName by remember {
        mutableStateOf("")
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    val vegetables = listOf(
        "Tomato", "Onion", "Potato", "Carrot",
        "Cabbage", "Cauliflower", "Brinjal",
        "Okra", "Cucumber", "Spinach", "Chilli",
        "Capsicum", "Beans", "Peas", "Radish"
    )

    val fruits = listOf(
        "Mango", "Banana", "Apple", "Grapes",
        "Orange", "Watermelon", "Papaya", "Guava",
        "Pineapple", "Pomegranate", "Lemon", "Strawberry"
    )

    val cropOptions = when (category) {
        "Vegetables" -> vegetables
        "Fruits" -> fruits
        else -> emptyList()
    }

    val quantityValue = quantity.toDoubleOrNull() ?: 0.0
    val priceValue = price.toDoubleOrNull() ?: 0.0

    val quantityInKg = if (quantityUnit == "Tonnes") {
        quantityValue * 1000.0
    } else {
        quantityValue
    }

    val pricePerKg = if (priceUnit == "Per Tonne") {
        priceValue / 1000.0
    } else {
        priceValue
    }

    val totalValue = quantityInKg * pricePerKg

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(
                "Add Produce Listing",
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MarketplaceDropdown(
                    label = "Product Category",
                    selectedValue = category,
                    options = listOf("Vegetables", "Fruits"),
                    onSelected = { newCategory ->
                        category = newCategory
                        cropName = if (newCategory == "Vegetables") {
                            vegetables.first()
                        } else {
                            fruits.first()
                        }
                    }
                )

                MarketplaceDropdown(
                    label = "Crop Name",
                    selectedValue = cropName,
                    options = cropOptions,
                    onSelected = { cropName = it }
                )

                Text(
                    "Available Quantity",
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MarketplaceDropdown(
                            label = "Unit",
                            selectedValue = quantityUnit,
                            options = listOf("Kg", "Tonnes"),
                            onSelected = { quantityUnit = it }
                        )
                    }
                }

                Text(
                    "Selling Price",
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MarketplaceDropdown(
                            label = "Price Unit",
                            selectedValue = priceUnit,
                            options = listOf("Per Kg", "Per Tonne"),
                            onSelected = { priceUnit = it }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGreen
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            "Estimated Total Value",
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "₹${"%.2f".format(totalValue)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeafGreen
                        )

                        Text(
                            "Calculated automatically from quantity and price.",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Village / City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Farmer / Seller Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    if (
                        cropName.isBlank() ||
                        quantityValue <= 0 ||
                        priceValue <= 0 ||
                        !quantityValue.isFinite() ||
                        !priceValue.isFinite() ||
                        !quantityInKg.isFinite() ||
                        !pricePerKg.isFinite() ||
                        location.isBlank() ||
                        farmerName.isBlank() ||
                        phone.isBlank()
                    ) {
                        message = "Please enter valid details in all fields."
                    } else {
                        isSaving = true
                        message = ""

                        repository.addListing(
                            category = category,
                            cropName = cropName,
                            quantityKg = quantityInKg,
                            pricePerKg = pricePerKg,
                            location = location.trim(),
                            phone = phone.trim(),
                            farmerName = farmerName.trim(),
                            onSuccess = onDismiss,
                            onError = { error ->
                                isSaving = false
                                message = error
                            }
                        )
                    }
                }
            ) {
                Text(if (isSaving) "Saving..." else "Save Listing")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
