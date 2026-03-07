package com.example.campusguide


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.campusguide.ui.accessibility.AccessibleAppRoot
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.accessibility.rememberAccessibilityState
import androidx.compose.ui.focus.FocusRequester
import com.example.campusguide.ui.components.NavigationBar
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.screens.CalendarScreen
import com.example.campusguide.ui.screens.MapScreen
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import kotlinx.coroutines.launch
import com.example.campusguide.ui.accessibility.AccessibilityPreferences
import androidx.compose.runtime.remember
import com.example.campusguide.data.ALL_CAMPUS_BUILDINGS
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.screens.DirectionsTopBarState
import com.example.campusguide.ui.components.DirectionsTopBar
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            // Global accessibility state; on every change, persist to DataStore
            val accessibilityState = rememberAccessibilityState { state ->
                scope.launch {
                    AccessibilityPreferences.saveFromState(this@MainActivity, state)
                }
            }

            // Hydrate from persisted preferences when the app starts
            LaunchedEffect(Unit) {
                val persisted = AccessibilityPreferences.load(this@MainActivity)
                accessibilityState.setFrom(persisted)
            }

            CompositionLocalProvider(
                LocalAccessibilityState provides accessibilityState
            ) {
                ConcordiaCampusGuideTheme {
                    AccessibleAppRoot {
                        ConcordiaCampusGuideApp()
                    }
                }
            }
        }

    }
}

@PreviewScreenSizes
@Composable
fun ConcordiaCampusGuideApp() {
    var currentDestination = rememberSaveable { mutableStateOf(AppDestinations.MAP) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAccessibility by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchCounter by rememberSaveable { mutableStateOf(0) }
    var topBarSuggestions by remember { mutableStateOf<List<com.example.campusguide.data.CampusBuilding>>(emptyList()) }
    var topBarSelectedBuilding by remember { mutableStateOf<com.example.campusguide.data.CampusBuilding?>(null) }
    val searchFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var directionsTopBarState by remember { mutableStateOf(DirectionsTopBarState(active = false)) }
    var directionsGoTrigger by remember { mutableStateOf(0) }
    var directionsCancelTrigger by remember { mutableStateOf(0) }
    var topBarTravelMode by remember { mutableStateOf(TravelMode.DRIVE) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* no-op; screens will re-check permission */ }
    )

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }


    when {
        showAccessibility -> {
            AccessibilityScreen(
                onBackClick = { showAccessibility = false }
            )
        }

        showProfile -> {
            ProfileScreen(
                onBackClick = { showProfile = false },
                onProfileClick = { /* handle profile details */ },
                onAccessibilityClick = { showAccessibility = true }
            )
        }
        else -> {
            NavigationBar(currentDestination) { modifier ->
                Box(modifier = modifier.fillMaxSize()) {
                    when (currentDestination.value) {
                        AppDestinations.MAP -> MapScreen(
                            searchQuery = "$searchQuery#$searchCounter",
                            topBarSelectedBuilding = topBarSelectedBuilding,
                            onTopBarBuildingConsumed = { topBarSelectedBuilding = null },
                            onBottomSearchClick = {
                                try { searchFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
                            },
                            onDirectionsTopBarState = { state -> directionsTopBarState = state },  // ← add
                            directionsGoTrigger = directionsGoTrigger,                              // ← add
                            directionsCancelTrigger = directionsCancelTrigger,                      // ← add
                            topBarTravelMode = topBarTravelMode,
                        )
                        AppDestinations.CALENDAR -> CalendarScreen()
                        AppDestinations.POI -> PlaceholderScreen("POI Screen", modifier)
                    }

                    if (directionsTopBarState.active) {
                        DirectionsTopBar(
                            modifier = Modifier.padding(top = 35.dp, start = 8.dp, end = 8.dp),
                            originLabel = directionsTopBarState.originLabel,
                            destinationLabel = directionsTopBarState.destinationLabel,
                            isCrossCampus = directionsTopBarState.isCrossCampus,
                            selectedMode = directionsTopBarState.selectedMode,
                            onModeSelected = { mode -> topBarTravelMode = mode },
                            routeSummary = directionsTopBarState.routeSummary,
                            errorMessage = directionsTopBarState.errorMessage,
                            showActions = directionsTopBarState.showActions,
                            isLoadingRoute = directionsTopBarState.isLoadingRoute,
                            onGoClick = { directionsGoTrigger++ },
                            onCancelClick = {
                                directionsCancelTrigger++
                                topBarTravelMode = TravelMode.DRIVE
                            },
                            onBackClick = {
                                directionsCancelTrigger++
                                topBarTravelMode = TravelMode.DRIVE
                            },
                        )
                    } else {
                        SearchBarWithProfile(
                            modifier = Modifier.padding(top = 35.dp),
                            focusRequester = searchFocusRequester,
                            onSearchQueryChange = { query ->
                                topBarSuggestions = com.example.campusguide.data.buildingSuggestions(
                                    query = query,
                                    activeCampus = com.example.campusguide.ui.components.Campus.SGW,
                                    crossCampus = true,
                                )
                            },
                            onSearchSubmit = { query ->
                                // Check if query matches a campus building first
                                val matchedBuilding = com.example.campusguide.data.ALL_CAMPUS_BUILDINGS
                                    .firstOrNull { it.matches(query) }
                                if (matchedBuilding != null) {
                                    // Use building directly, skip geocoder
                                    topBarSelectedBuilding = matchedBuilding
                                    topBarSuggestions = emptyList()
                                    currentDestination.value = AppDestinations.MAP
                                } else {
                                    // Fall back to geocoder search
                                    searchQuery = query
                                    searchCounter++
                                    topBarSuggestions = emptyList()
                                    if (currentDestination.value != AppDestinations.MAP) {
                                        currentDestination.value = AppDestinations.MAP
                                    }
                                }
                            },
                            onProfileClick = { showProfile = true },
                            suggestions = topBarSuggestions,
                            onBuildingSelected = { building ->
                                topBarSelectedBuilding = building
                                topBarSuggestions = emptyList()
                                searchQuery = ""
                                currentDestination.value = AppDestinations.MAP
                            },
                        )
                    }
                }
            }
        }
    }
}
sealed class AppIcon {
    data class Vector(val imageVector: ImageVector) : AppIcon()
    data class Drawable(@DrawableRes val resId: Int) : AppIcon()
}

enum class AppDestinations(
    val label: String,
    val icon: AppIcon,
) {
    MAP("Map", AppIcon.Vector(Icons.Default.Place)),
    CALENDAR("Calendar", AppIcon.Drawable(R.drawable.ic_calendar)),
    POI("POI", AppIcon.Drawable(R.drawable.ic_poi)),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    AccessibleText(
        text = "Hello $name!",
        baseFontSizeSp = 16f,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConcordiaCampusGuideTheme {
        Greeting("Android")
    }
}

@Composable
fun PlaceholderScreen(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}