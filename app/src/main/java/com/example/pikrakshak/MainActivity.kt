package com.example.pikrakshak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource

import com.google.firebase.auth.FirebaseAuth

// Compose state
import androidx.compose.runtime.*

private val AppGreen = androidx.compose.ui.graphics.Color(0xFF14532D)
private val AppLime = androidx.compose.ui.graphics.Color(0xFF22C55E)
private val AppBackground = androidx.compose.ui.graphics.Color(0xFFF0FDF4)



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(
            "pikrakshak_settings",
            MODE_PRIVATE
        )

        val savedTheme = preferences.getString(
            "app_theme",
            "SYSTEM"
        ) ?: "SYSTEM"

        val initialTheme = try {
            AppThemeMode.valueOf(savedTheme)
        } catch (_: IllegalArgumentException) {
            AppThemeMode.SYSTEM
        }

        val savedLanguage = preferences.getString(
            "app_language",
            "en"
        ) ?: "en"

        applyAppLanguage(savedLanguage)

        setContent {

            var themeMode by remember {
                mutableStateOf(initialTheme)
            }

            var languageCode by remember {
                mutableStateOf(savedLanguage)
            }

            PikRakshakTheme(themeMode = themeMode) {

                PikRakshakApp(
                    themeMode = themeMode,

                    onThemeChange = { selectedTheme ->
                        themeMode = selectedTheme

                        preferences.edit()
                            .putString("app_theme", selectedTheme.name)
                            .apply()
                    },

                    languageCode = languageCode,

                    onLanguageChange = { selectedLanguage ->

                        if (selectedLanguage != languageCode) {

                            preferences.edit()
                                .putString("app_language", selectedLanguage)
                                .apply()

                            languageCode = selectedLanguage

                            applyAppLanguage(selectedLanguage)

                            recreate()
                        }
                    }
                )
            }
        }
    }

    private fun applyAppLanguage(languageCode: String) {
        val locale = java.util.Locale.forLanguageTag(languageCode)

        java.util.Locale.setDefault(locale)

        val configuration = android.content.res.Configuration(
            resources.configuration
        )

        configuration.setLocale(locale)

        @Suppress("DEPRECATION")
        resources.updateConfiguration(
            configuration,
            resources.displayMetrics
        )
    }
}


@Composable
fun PikRakshakApp(
    themeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    languageCode: String,
    onLanguageChange: (String) -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var isLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }

    var currentRole by remember {
        mutableStateOf<UserRole?>(null)
    }

    var isLoadingRole by remember {
        mutableStateOf(auth.currentUser != null)
    }

    var needsRoleSelection by remember {
        mutableStateOf(false)
    }

    // Navigation states
    var showShelfLife by remember { mutableStateOf(false) }
    var showNearbyStorage by remember { mutableStateOf(false) }
    var showMarketplace by remember { mutableStateOf(false) }

    var showBuyerProfile by remember { mutableStateOf(false) }
    var showAddStorage by remember { mutableStateOf(false) }
    var showMyStorage by remember { mutableStateOf(false) }
    var showStorageOwnerProfile by remember { mutableStateOf(false) }

    var showCropRecommendations by remember { mutableStateOf(false) }
    var showDiseaseDetection by remember { mutableStateOf(false) }
    var showFarmerProfile by remember { mutableStateOf(false) }

    var showThemeSettings by remember { mutableStateOf(false) }

    fun resetNavigation() {
        showShelfLife = false
        showNearbyStorage = false
        showMarketplace = false
        showBuyerProfile = false
        showAddStorage = false
        showMyStorage = false
        showStorageOwnerProfile = false
        showCropRecommendations = false
        showDiseaseDetection = false
        showFarmerProfile = false
        showThemeSettings = false
    }

    fun logout() {
        auth.signOut()
        currentRole = null
        isLoggedIn = false
        needsRoleSelection = false
        isLoadingRole = false
        resetNavigation()
    }

    LaunchedEffect(isLoggedIn, auth.currentUser?.uid) {

        if (
            isLoggedIn &&
            auth.currentUser != null &&
            !needsRoleSelection
        ) {

            isLoadingRole = true

            UserRepository.getUserRole(

                onSuccess = { role ->
                    currentRole = role
                    isLoadingRole = false
                },

                onError = {
                    currentRole = null
                    isLoadingRole = false
                    needsRoleSelection = true
                }
            )

        } else {
            isLoadingRole = false
        }
    }

    // Screen navigation
    val screenKey = when {

        showThemeSettings -> "theme_settings"

        !isLoggedIn -> "login"

        isLoadingRole -> "loading"

        needsRoleSelection || currentRole == null ->
            "role_selection"

        showCropRecommendations -> "crop_recommendations"
        showDiseaseDetection -> "disease_detection"
        showFarmerProfile -> "farmer_profile"

        showBuyerProfile -> "buyer_profile"
        showStorageOwnerProfile -> "storage_owner_profile"
        showMyStorage -> "my_storage"
        showAddStorage -> "add_storage"

        showShelfLife -> "shelf_life"
        showNearbyStorage -> "nearby_storage"
        showMarketplace -> "marketplace"

        currentRole == UserRole.FARMER -> "farmer_dashboard"
        currentRole == UserRole.BUYER -> "buyer_dashboard"
        currentRole == UserRole.SELLER -> "seller_dashboard"
        currentRole == UserRole.STORAGE_OWNER -> "storage_dashboard"

        else -> "loading"
    }

    BackHandler(enabled = screenKey != "login") {

        when {

            showThemeSettings ->
                showThemeSettings = false

            showCropRecommendations ->
                showCropRecommendations = false

            showDiseaseDetection ->
                showDiseaseDetection = false

            showFarmerProfile ->
                showFarmerProfile = false

            showBuyerProfile ->
                showBuyerProfile = false

            showStorageOwnerProfile ->
                showStorageOwnerProfile = false

            showAddStorage ->
                showAddStorage = false

            showMyStorage ->
                showMyStorage = false

            showShelfLife ->
                showShelfLife = false

            showNearbyStorage ->
                showNearbyStorage = false

            showMarketplace ->
                showMarketplace = false

            needsRoleSelection ->
                logout()

            currentRole != null -> {
                resetNavigation()
                currentRole = null
                needsRoleSelection = true
            }

            isLoggedIn ->
                logout()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedContent(
            targetState = screenKey,
            label = "PikRakshakScreenTransition",

            transitionSpec = {

                (
                        fadeIn(tween(400)) +
                                slideInVertically(
                                    animationSpec = tween(
                                        400,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { it / 14 }
                        ).togetherWith(

                        fadeOut(tween(250)) +
                                slideOutVertically(
                                    animationSpec = tween(250)
                                ) { -it / 14 }
                    )
            }

        ) { targetScreen ->

            when (targetScreen) {

                "login" -> {

                    AuthScreen(

                        onLoginSuccess = {
                            resetNavigation()
                            needsRoleSelection = false
                            isLoggedIn = true
                        },

                        onNewAccountCreated = {
                            resetNavigation()
                            needsRoleSelection = true
                            isLoggedIn = true
                        }
                    )
                }

                "loading" -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            CircularProgressIndicator()

                            Spacer(Modifier.height(12.dp))

                            Text("Preparing your dashboard...")
                        }
                    }
                }

                "role_selection" -> {

                    RoleSelectionScreen(

                        onRoleSelected = { selectedRole ->

                            UserRepository.saveUserRole(

                                role = selectedRole,

                                onSuccess = {
                                    currentRole = selectedRole
                                    needsRoleSelection = false
                                    resetNavigation()
                                },

                                onError = {
                                    needsRoleSelection = true
                                }
                            )
                        },

                        onBack = {
                            logout()
                        }
                    )
                }

                "theme_settings" -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background
                            )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            TextButton(
                                onClick = {
                                    showThemeSettings = false
                                }
                            ) {
                                Text("← Back")
                            }

                            Text(
                                "Appearance Settings",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        ThemeSettingsScreen(
                            themeMode = themeMode,
                            onThemeChange = onThemeChange
                        )
                    }
                }

                "shelf_life" -> {

                    ShelfLifeScreen(
                        onBack = {
                            showShelfLife = false
                        }
                    )
                }

                "nearby_storage" -> {

                    NearbyStorageScreen(
                        onBack = {
                            showNearbyStorage = false
                        }
                    )
                }

                "marketplace" -> {

                    ProduceMarketplaceScreen(
                        onBack = {
                            showMarketplace = false
                        }
                    )
                }

                "crop_recommendations" -> {

                    CropRecommendationScreen(
                        onBack = {
                            showCropRecommendations = false
                        }
                    )
                }

                "disease_detection" -> {

                    DiseaseDetectionScreen(
                        onBack = {
                            showDiseaseDetection = false
                        }
                    )
                }

                "farmer_profile" -> {

                    FarmerProfileScreen(
                        onBack = {
                            showFarmerProfile = false
                        },
                        onLogout = {
                            logout()
                        }
                    )
                }

                "farmer_dashboard" -> {

                    PikRakshakDashboard(

                        onShelfLife = {
                            showShelfLife = true
                        },

                        onFeatureSelected = { feature ->

                            when (feature) {

                                "Produce Marketplace" ->
                                    showMarketplace = true

                                "Spoilage Risk" ->
                                    showShelfLife = true

                                "Nearby Storage" ->
                                    showNearbyStorage = true

                                "Crop Recommendations" ->
                                    showCropRecommendations = true

                                "Disease Detection" ->
                                    showDiseaseDetection = true

                                "My Profile" ->
                                    showFarmerProfile = true
                            }
                        },

                        onLogout = {
                            logout()
                        }
                    )
                }

                "buyer_dashboard" -> {

                    BuyerDashboardScreen(

                        onMarketplaceClick = {
                            showMarketplace = true
                        },

                        onStorageClick = {
                            showNearbyStorage = true
                        },

                        onProfileClick = {
                            showBuyerProfile = true
                        }
                    )
                }

                "buyer_profile" -> {

                    BuyerProfileScreen(
                        onBack = {
                            showBuyerProfile = false
                        },
                        onLogout = {
                            logout()
                        }
                    )
                }

                "add_storage" -> {

                    AddStorageScreen(
                        onBack = {
                            showAddStorage = false
                        },
                        onFacilityAdded = {
                            showAddStorage = false
                        }
                    )
                }

                "my_storage" -> {

                    MyStorageFacilitiesScreen(
                        onBack = {
                            showMyStorage = false
                        }
                    )
                }

                "storage_owner_profile" -> {

                    StorageOwnerProfileScreen(
                        onBack = {
                            showStorageOwnerProfile = false
                        },
                        onLogout = {
                            logout()
                        }
                    )
                }

                "seller_dashboard" -> {

                    RoleHomeScreen(
                        role = UserRole.SELLER,
                        onMarketplace = {
                            showMarketplace = true
                        },
                        onShelfLife = {
                            showShelfLife = true
                        },
                        onNearbyStorage = {
                            showNearbyStorage = true
                        },
                        onLogout = {
                            logout()
                        }
                    )
                }

                "storage_dashboard" -> {

                    RoleHomeScreen(
                        role = UserRole.STORAGE_OWNER,
                        onMarketplace = {
                            showMarketplace = true
                        },
                        onShelfLife = {
                            showShelfLife = true
                        },
                        onNearbyStorage = {
                            showNearbyStorage = true
                        },
                        onLogout = {
                            logout()
                        },
                        onAddStorage = {
                            showAddStorage = true
                        },
                        onMyStorage = {
                            showMyStorage = true
                        },
                        onProfile = {
                            showStorageOwnerProfile = true
                        }
                    )
                }
            }
        }

        // Language and Appearance buttons
        if (
            screenKey == "login" ||
            screenKey == "role_selection"
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {

                        val newLanguage =
                            if (languageCode == "en") "mr" else "en"

                        onLanguageChange(newLanguage)
                    },

                    shape = RoundedCornerShape(50.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {

                    Text(
                        text = if (languageCode == "en") {
                            "🌐 मराठी"
                        } else {
                            "🌐 English"
                        },

                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        showThemeSettings = true
                    },

                    shape = RoundedCornerShape(50.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {

                    Text(
                        "⚙ Appearance",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onNewAccountCreated: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val emailBorderColor by animateColorAsState(
        targetValue = if (emailFocused) AppLime else Color(0xFFD1D5DB),
        animationSpec = tween(250),
        label = "emailBorderAnimation"
    )

    val passwordBorderColor by animateColorAsState(
        targetValue = if (passwordFocused) AppLime else Color(0xFFD1D5DB),
        animationSpec = tween(250),
        label = "passwordBorderAnimation"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(200),
        label = "authButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(
        label = "logoMotion"
    )

    val logoOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingLogo"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(700)) +
                    slideInVertically(tween(700)) { -40 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = logoOffset.dp)
                        .size(78.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppLime, AppGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 42.sp)
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "PikRakshak",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppGreen
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    stringResource(R.string.tagline),
                    color = Color(0xFF52705B),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                AnimatedContent(

                    targetState = isSignUp,
                    label = "authHeadingAnimation"
                ) { signUp ->
                    Column {
                        Text(
                            text = if (signUp) {
                                stringResource(R.string.create_account)

                            } else {
                                stringResource(R.string.welcome_back)

                            },
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )



                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = if (signUp) {
                                stringResource(R.string.signup_subtitle)

                            } else {
                                stringResource(R.string.login_subtitle)

                            },
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }



                Spacer(Modifier.height(22.dp))

                Text(
                    stringResource(R.string.email_address),
                            color = AppGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            emailFocused = it.isFocused
                        },
                    placeholder = { Text("Enter your email") },
                    leadingIcon = { Text("✉️") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emailBorderColor,
                        unfocusedBorderColor = emailBorderColor,
                        focusedContainerColor = Color(0xFFF7FFF8),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.password),
                    color = AppGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        message = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            passwordFocused = it.isFocused
                        },
                    placeholder = { Text("Enter your password") },
                    leadingIcon = { Text("🔒") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = passwordBorderColor,
                        unfocusedBorderColor = passwordBorderColor,
                        focusedContainerColor = Color(0xFFF7FFF8),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = {
                        when {
                            email.isBlank() -> {
                                message = "Please enter your email."
                            }

                            password.length < 6 -> {
                                message = "Password must contain at least 6 characters."
                            }

                            else -> {
                                isLoading = true
                                message = ""

                                val task = if (isSignUp) {
                                    auth.createUserWithEmailAndPassword(
                                        email.trim(),
                                        password
                                    )
                                } else {
                                    auth.signInWithEmailAndPassword(
                                        email.trim(),
                                        password
                                    )
                                }

                                task.addOnCompleteListener { result ->
                                    isLoading = false

                                    if (result.isSuccessful) {
                                        if (isSignUp) {
                                            onNewAccountCreated()
                                        } else {
                                            onLoginSuccess()
                                        }
                                    } else {
                                        message =
                                            result.exception?.localizedMessage
                                                ?: "Authentication failed."
                                    }
                                }
                            }
                        }
                    },


                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(23.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUp) {
                                stringResource(R.string.create_account_button)

                            } else {
                                stringResource(R.string.login_button)

                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(
                    visible = message.isNotBlank(),
                    enter = fadeIn(tween(250)) +
                            slideInVertically(tween(250)) { it / 2 },
                    exit = fadeOut(tween(200))
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                AnimatedContent(

                    targetState = isSignUp,
                    label = "authSwitchAnimation"

                )
                { signUp ->
                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            message = ""
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (signUp) {
                                stringResource(R.string.already_registered)

                            } else {
                                stringResource(R.string.new_to_app)
                            },
                            color = AppGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            stringResource(R.string.farming_footer),
            color = Color(0xFF52705B),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RoleHomeScreen(
    role: UserRole,
    onMarketplace: () -> Unit,
    onShelfLife: () -> Unit,
    onNearbyStorage: () -> Unit,
    onLogout: () -> Unit,
    onAddStorage: () -> Unit = {},
    onMyStorage: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(role) {
        showContent = true
    }

    val title = when (role) {
        UserRole.BUYER -> "Buyer Dashboard"
        UserRole.SELLER -> "Seller Dashboard"
        UserRole.STORAGE_OWNER -> "Storage Owner Dashboard"
        UserRole.FARMER -> "Farmer Dashboard"
    }

    val subtitle = when (role) {
        UserRole.BUYER -> "Discover fresh produce from farmers."
        UserRole.SELLER -> "Manage your produce listings."
        UserRole.STORAGE_OWNER -> "Manage your storage business in one place."
        UserRole.FARMER -> "Manage your farm."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F8EC),
                        Color.White,
                        AppBackground
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500)) +
                    slideInVertically(tween(500)) { -it / 4 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PikRakshak 🌱",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppGreen
                    )

                    Text(
                        "Welcome to your workspace",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    "Logout",
                    color = AppGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onLogout()
                    }
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(550, delayMillis = 100)) +
                    slideInVertically(tween(550, delayMillis = 100)) {
                        it / 4
                    }
        ) {
            Column {
                Text(
                    title,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppGreen
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Your services",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF163321)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        when (role) {
            UserRole.BUYER -> {
                RoleFeatureCard(
                    emoji = "🛒",
                    title = "Browse Fresh Produce",
                    description = "Explore fruits and vegetables listed by farmers.",
                    onClick = onMarketplace
                )
            }

            UserRole.SELLER -> {
                RoleFeatureCard(
                    emoji = "📦",
                    title = "Manage My Listings",
                    description = "Add, edit, and manage your produce listings.",
                    onClick = onMarketplace
                )
            }

            UserRole.STORAGE_OWNER -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RoleFeatureCard(
                        emoji = "➕",
                        title = "Add New Storage",
                        description = "Register your warehouse or cold storage facility.",
                        onClick = onAddStorage
                    )

                    RoleFeatureCard(
                        emoji = "🏬",
                        title = "My Storage Facilities",
                        description = "View, edit and delete your registered facilities.",
                        onClick = onMyStorage
                    )

                    RoleFeatureCard(
                        emoji = "📍",
                        title = "Nearby Storage",
                        description = "Explore available storage facilities.",
                        onClick = onNearbyStorage
                    )

                    RoleFeatureCard(
                        emoji = "🛒",
                        title = "Marketplace",
                        description = "Explore produce listed by farmers and sellers.",
                        onClick = onMarketplace
                    )

                    RoleFeatureCard(
                        emoji = "👤",
                        title = "My Profile",
                        description = "Update your name and contact information.",
                        onClick = onProfile
                    )
                }
            }

            UserRole.FARMER -> {
                // The existing Farmer Dashboard is used separately.
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "SMART FARMING • LESS WASTE • BETTER FUTURE",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF52705B),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RoleFeatureCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(200),
        label = "featureCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    emoji,
                    fontSize = 27.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = AppGreen,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    description,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }

            Text(
                "›",
                fontSize = 28.sp,
                color = AppGreen
            )
        }
    }
}
