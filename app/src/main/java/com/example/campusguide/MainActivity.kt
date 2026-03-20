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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.campusguide.ui.screens.map.MapScreen
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import kotlinx.coroutines.launch
import com.example.campusguide.ui.accessibility.AccessibilityPreferences
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.screens.map.DirectionsTopBarState
import com.example.campusguide.ui.components.DirectionsTopBar
import com.example.campusguide.ui.components.FocusClearWrapper
import com.example.campusguide.ui.components.ignoreFocusClearOnTouch
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import com.example.campusguide.ui.viewmodels.MapSearchViewModel
import com.example.campusguide.ui.viewmodels.UserLocationViewModel


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
    var showAccessibility by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var directionsTopBarState by remember { mutableStateOf(DirectionsTopBarState(active = false)) }
    var directionsGoTrigger by remember { mutableIntStateOf(0) }
    var directionsCancelTrigger by remember { mutableIntStateOf(0) }
    var originPickTrigger by remember { mutableIntStateOf(0) }
    var myLocationTrigger by remember { mutableIntStateOf(0) }
    var topBarTravelMode by remember { mutableStateOf(TravelMode.DRIVE) }
    val viewModel = viewModel<ControlsViewModel>()
    val userLocationViewModel = viewModel<UserLocationViewModel>()
    val userLatLng by userLocationViewModel.userLatLng.collectAsState()
    val nearestId by userLocationViewModel.nearestId.collectAsState()

    val mapViewmodel: MapSearchViewModel = viewModel()
    var showProfile by remember {mutableStateOf(false)}

    var currentDestination = remember { mutableStateOf(AppDestinations.MAP) }


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


    LaunchedEffect(Unit) {
        userLocationViewModel.fetchUserLocation(context)
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
            FocusClearWrapper {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavigationBar((currentDestination), { modifier ->

                    when (currentDestination.value) {
                        AppDestinations.MAP -> {
                            MapScreen(
                            viewModel = viewModel,
                            searchQuery = "$mapViewmodel.searchQuery#$mapViewmodel.searchCounter",
                            topBarSelectedSuggestion = mapViewmodel.topBarSelectedSuggestion,
                            onTopBarBuildingConsumed = {
                                mapViewmodel.topBarSelectedSuggestion = null
                            },
                            onBottomSearchClick = {
                                try {
                                    searchFocusRequester.requestFocus()
                                } catch (_: IllegalStateException) {
                                }
                            },
                            onDirectionsTopBarState = { state ->
                                directionsTopBarState = state
                            },

                            directionsGoTrigger = directionsGoTrigger,
                            directionsCancelTrigger = directionsCancelTrigger,
                            originPickTrigger = originPickTrigger,
                            myLocationTrigger = myLocationTrigger,
                            topBarTravelMode = topBarTravelMode,
                        )

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
                                    currentSteps = directionsTopBarState.currentSteps,
                                    isPickingOrigin = directionsTopBarState.isPickingOrigin,
                                    onOriginClick = { originPickTrigger++ },
                                    onMyLocationClick = { myLocationTrigger++ },
                                    onGoClick = { directionsGoTrigger++ },
                                    onCancelClick = {
                                        directionsCancelTrigger++
                                        topBarTravelMode = TravelMode.DRIVE
                                    },
                                )
                            } else {
                                val suggestionContent: @Composable (Suggestion) -> Unit = { suggestion ->
                                    mapViewmodel.BuildingRow(suggestion, nearestId, userLatLng)
                                }

                                SearchBarWithProfile(
                                    modifier = Modifier.padding(top = 35.dp).ignoreFocusClearOnTouch(),
                                    focusRequester = searchFocusRequester,
                                    suggestions = mapViewmodel.topBarSuggestions,

                                    onSearchQueryChange = mapViewmodel::onSearchQueryChange,
                                    onSearchSubmit = mapViewmodel::onSearchSubmit,
                                    onSuggestionSelected = mapViewmodel::onSuggestionSelected,
                                    onProfileClick = { showProfile = true },


                                    suggestionContent = suggestionContent,
                                    suggestionKey = mapViewmodel.suggestionKey,
                                )
                            }
                    }




                        AppDestinations.CALENDAR -> {
                            CalendarScreen()

                        }
                        AppDestinations.POI -> {
                            Greeting("POI Screen", modifier)
                        }
                    }
                }
                )
            }
        }
        }
    }
}
sealed class AppIcon {
    data class Vector(val imageVector: ImageVector) : AppIcon()
    data class Drawable(@param:DrawableRes val resId: Int) : AppIcon()
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