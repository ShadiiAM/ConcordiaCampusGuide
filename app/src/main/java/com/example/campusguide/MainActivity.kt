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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.ALL_CAMPUS_BUILDINGS
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.buildingSuggestions
import com.example.campusguide.ui.components.BuildingAutocompleteField
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.IndoorOutdoorRouteRequest
import com.example.campusguide.ui.screens.DirectionsTopBarState
import com.example.campusguide.ui.components.DirectionsTopBar
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import com.example.campusguide.ui.viewmodels.ShuttleViewModel
import com.example.campusguide.indoor.IndoorGraphRegistry
import com.example.campusguide.indoor.IndoorRoomSearchService
import com.example.campusguide.ui.components.TopSearchSuggestion

private enum class DirectionsEditMode {
    OUTDOOR_DESTINATION,
    INDOOR_ORIGIN,
    INDOOR_DESTINATION,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IndoorGraphRegistry.init(this)
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
    val currentDestination = rememberSaveable { mutableStateOf(AppDestinations.MAP) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAccessibility by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchCounter by rememberSaveable { mutableStateOf(0) }
    var topBarSuggestions by remember { mutableStateOf<List<com.example.campusguide.ui.components.TopSearchSuggestion>>(emptyList()) }
    var topBarSelectedBuilding by remember { mutableStateOf<com.example.campusguide.data.CampusBuilding?>(null) }
    val searchFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var directionsTopBarState by remember { mutableStateOf(DirectionsTopBarState(active = false)) }
    var directionsGoTrigger by remember { mutableStateOf(0) }
    var directionsCancelTrigger by remember { mutableStateOf(0) }
    var topBarTravelMode by remember { mutableStateOf(TravelMode.DRIVE) }
    var directionsEditMode by rememberSaveable { mutableStateOf<DirectionsEditMode?>(null) }
    var directionsDestinationSuggestions by remember { mutableStateOf<List<CampusBuilding>>(emptyList()) }
    var indoorDirectionsQuery by rememberSaveable { mutableStateOf("") }
    var indoorDirectionsSuggestions by remember { mutableStateOf<List<IndoorRoomSearchService.Result>>(emptyList()) }
    var topBarDirectionsDestinationBuilding by remember { mutableStateOf<CampusBuilding?>(null) }

    // Indoor overlay + search triggers
    var openIndoorBuildingCode by remember { mutableStateOf<String?>(null) }
    var indoorTopCardActive by remember { mutableStateOf(false) }
    var indoorFocusNodeTrigger by remember { mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null) }
    var indoorSetStartTrigger by remember { mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null) }
    var indoorSetDestTrigger by remember { mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null) }
    var pendingIndoorStart by remember { mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null) }
    var pendingIndoorDestination by remember { mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null) }
    var indoorOutdoorRouteRequest by remember { mutableStateOf<IndoorOutdoorRouteRequest?>(null) }
    val viewModel = viewModel<ControlsViewModel>()
    val shuttleViewModel = viewModel<ShuttleViewModel>()

    val clearDirectionsAndIndoorState = {
        directionsTopBarState = DirectionsTopBarState(active = false)
        openIndoorBuildingCode = null
        indoorTopCardActive = false
        indoorFocusNodeTrigger = null
        indoorSetStartTrigger = null
        indoorSetDestTrigger = null
        pendingIndoorStart = null
        pendingIndoorDestination = null
        indoorOutdoorRouteRequest = null
        directionsEditMode = null
        directionsDestinationSuggestions = emptyList()
        indoorDirectionsQuery = ""
        indoorDirectionsSuggestions = emptyList()
    }

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

    LaunchedEffect(directionsTopBarState.active) {
        if (!directionsTopBarState.active) {
            directionsEditMode = null
            directionsDestinationSuggestions = emptyList()
            indoorDirectionsQuery = ""
            indoorDirectionsSuggestions = emptyList()
        }
    }

    LaunchedEffect(directionsCancelTrigger) {
        if (directionsCancelTrigger == 0) return@LaunchedEffect
        clearDirectionsAndIndoorState()
    }

    LaunchedEffect(directionsTopBarState.indoorOriginNode, directionsTopBarState.indoorDestinationNode) {
        if (directionsTopBarState.indoorOriginNode != null) {
            pendingIndoorStart = directionsTopBarState.indoorOriginNode
        }
        if (directionsTopBarState.indoorDestinationNode != null) {
            pendingIndoorDestination = directionsTopBarState.indoorDestinationNode
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
            NavigationBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    val wasOnMap = currentDestination.value == AppDestinations.MAP
                    if (destination == AppDestinations.MAP && wasOnMap) {
                        openIndoorBuildingCode = null
                        indoorTopCardActive = false
                        indoorFocusNodeTrigger = null
                        indoorSetStartTrigger = null
                        indoorSetDestTrigger = null
                    }
                    currentDestination.value = destination
                }
            ) { modifier ->
                Box(modifier = modifier.fillMaxSize()) {
                    val preservedIndoorDestination = directionsTopBarState.indoorDestinationNode ?: pendingIndoorDestination

                    when (currentDestination.value) {
                        AppDestinations.MAP -> MapScreen(
                            viewModel= viewModel,
                            searchQuery = "$searchQuery#$searchCounter",
                            topBarSelectedBuilding = topBarSelectedBuilding,
                            onTopBarBuildingConsumed = { topBarSelectedBuilding = null },
                            topBarDirectionsDestinationBuilding = topBarDirectionsDestinationBuilding,
                            onTopBarDirectionsDestinationConsumed = { topBarDirectionsDestinationBuilding = null },
                            onBottomSearchClick = {
                                try { searchFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
                            },
                            onDirectionsTopBarState = { state -> directionsTopBarState = state },  // ← add
                            directionsGoTrigger = directionsGoTrigger,                              // ← add
                            directionsCancelTrigger = directionsCancelTrigger,                      // ← add
                            topBarTravelMode = topBarTravelMode,
                            onIndoorOverlayChanged = { openIndoorBuildingCode = it },
                            requestedIndoorBuildingCode = openIndoorBuildingCode,
                            indoorOutdoorRouteRequest = indoorOutdoorRouteRequest,
                            onIndoorOutdoorRouteRequested = { request ->
                                indoorOutdoorRouteRequest = request
                                openIndoorBuildingCode = null
                            },
                            onIndoorOutdoorRouteRequestConsumed = { indoorOutdoorRouteRequest = null },
                            indoorSearchFocusNodeTrigger = indoorFocusNodeTrigger,
                            indoorSetStartTrigger = indoorSetStartTrigger,
                            indoorSetDestTrigger = indoorSetDestTrigger,
                            onIndoorTriggerConsumed = {
                                indoorFocusNodeTrigger = null
                                indoorSetStartTrigger = null
                                indoorSetDestTrigger = null
                            },
                            onIndoorTopCardActiveChanged = { active -> indoorTopCardActive = active },
                            hasExistingDestinationSelection = preservedIndoorDestination != null,
                            shuttleShowBothStops = shuttleViewModel.shuttleShowBothStops,
                            onShuttleShowBothStopsConsumed = { shuttleViewModel.consumeShowBothStops() },
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
                            currentSteps = directionsTopBarState.currentSteps,
                            showTravelModes = directionsTopBarState.showTravelModes,
                            goEnabled = directionsTopBarState.goEnabled,
                            goLabel = directionsTopBarState.goLabel,
                            cancelLabel = directionsTopBarState.cancelLabel,
                            onGoClick = { directionsGoTrigger++ },
                            onCancelClick = {
                                directionsCancelTrigger++
                                topBarTravelMode = TravelMode.DRIVE
                                clearDirectionsAndIndoorState()
                            },
                            onBackClick = {
                                // X only dismisses the bar — Cancel button is the only way to cancel the route
                                directionsTopBarState = directionsTopBarState.copy(active = false)
                                directionsEditMode = null
                                directionsDestinationSuggestions = emptyList()
                                indoorDirectionsQuery = ""
                                indoorDirectionsSuggestions = emptyList()
                            },
                            onOriginClick = if (openIndoorBuildingCode != null) {
                                {
                                    directionsEditMode = if (directionsEditMode == DirectionsEditMode.INDOOR_ORIGIN) null else DirectionsEditMode.INDOOR_ORIGIN
                                    directionsDestinationSuggestions = emptyList()
                                    indoorDirectionsQuery = ""
                                    indoorDirectionsSuggestions = emptyList()
                                }
                            } else {
                                null
                            },
                            onDestinationClick = {
                                directionsEditMode = when {
                                    openIndoorBuildingCode != null && directionsEditMode == DirectionsEditMode.INDOOR_DESTINATION -> null
                                    openIndoorBuildingCode != null -> DirectionsEditMode.INDOOR_DESTINATION
                                    directionsEditMode == DirectionsEditMode.OUTDOOR_DESTINATION -> null
                                    else -> DirectionsEditMode.OUTDOOR_DESTINATION
                                }
                                directionsDestinationSuggestions = emptyList()
                                indoorDirectionsQuery = ""
                                indoorDirectionsSuggestions = emptyList()
                            },
                            extraContent = {
                                if (directionsEditMode == DirectionsEditMode.OUTDOOR_DESTINATION) {
                                    Spacer(Modifier.height(8.dp))
                                    BuildingAutocompleteField(
                                        label = "To:",
                                        value = directionsTopBarState.destinationLabel,
                                        suggestions = directionsDestinationSuggestions,
                                        placeholder = "Building name or code…",
                                        enabled = !directionsTopBarState.isLoadingRoute,
                                        testTag = "top_directions_destination_field",
                                        onQueryChange = { query ->
                                            directionsDestinationSuggestions = buildingSuggestions(
                                                query = query,
                                                activeCampus = Campus.SGW,
                                                crossCampus = true,
                                            )
                                        },
                                        onSelected = { building ->
                                            topBarDirectionsDestinationBuilding = building
                                            directionsEditMode = null
                                            directionsDestinationSuggestions = emptyList()
                                        },
                                    )
                                }

                                if (directionsEditMode == DirectionsEditMode.INDOOR_ORIGIN || directionsEditMode == DirectionsEditMode.INDOOR_DESTINATION) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = indoorDirectionsQuery,
                                        onValueChange = { query ->
                                            indoorDirectionsQuery = query
                                            indoorDirectionsSuggestions = if (query.isBlank()) {
                                                emptyList()
                                            } else {
                                                IndoorRoomSearchService.search(
                                                    query = query,
                                                    scope = IndoorRoomSearchService.Scope.Global,
                                                    limit = 8,
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            Text(
                                                if (directionsEditMode == DirectionsEditMode.INDOOR_ORIGIN)
                                                    "Search start classroom"
                                                else
                                                    "Search destination classroom"
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                if (directionsEditMode == DirectionsEditMode.INDOOR_ORIGIN)
                                                    "e.g. H.937"
                                                else
                                                    "e.g. H.831"
                                            )
                                        }
                                    )

                                    if (indoorDirectionsSuggestions.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            tonalElevation = 2.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.heightIn(max = 220.dp)
                                            ) {
                                                items(indoorDirectionsSuggestions) { suggestion ->
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                val pickedNode = suggestion.node
                                                                val pickedBuildingCode = pickedNode.buildingCode.uppercase()

                                                                if (directionsEditMode == DirectionsEditMode.INDOOR_ORIGIN) {
                                                                    pendingIndoorStart = pickedNode
                                                                    val existingDest = pendingIndoorDestination
                                                                    if (existingDest != null && !existingDest.buildingCode.equals(pickedNode.buildingCode, ignoreCase = true)) {
                                                                        indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                                                                            startNode = pickedNode,
                                                                            destinationNode = existingDest,
                                                                        )
                                                                        openIndoorBuildingCode = null
                                                                    } else {
                                                                        openIndoorBuildingCode = pickedBuildingCode
                                                                        indoorSetStartTrigger = pickedNode
                                                                    }
                                                                } else {
                                                                    pendingIndoorDestination = pickedNode
                                                                    val existingStart = pendingIndoorStart
                                                                    if (existingStart != null && !existingStart.buildingCode.equals(pickedNode.buildingCode, ignoreCase = true)) {
                                                                        indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                                                                            startNode = existingStart,
                                                                            destinationNode = pickedNode,
                                                                        )
                                                                        openIndoorBuildingCode = null
                                                                    } else {
                                                                        openIndoorBuildingCode = pickedBuildingCode
                                                                        indoorSetDestTrigger = pickedNode
                                                                    }
                                                                }

                                                                currentDestination.value = AppDestinations.MAP
                                                                directionsEditMode = null
                                                                indoorDirectionsQuery = ""
                                                                indoorDirectionsSuggestions = emptyList()
                                                                directionsDestinationSuggestions = emptyList()
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    ) {
                                                        Text(
                                                            text = suggestion.primaryLabel,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                        )
                                                        Text(
                                                            text = "${suggestion.typeLabel} · ${suggestion.locationLabel}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                directionsTopBarState.legFallbackMessage?.let { fallback ->
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = fallback,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                        modifier = Modifier.semantics {
                                            contentDescription = fallback
                                        }
                                    )
                                }
                            }
                        )
                    } else if (openIndoorBuildingCode == null) {
                        SearchBarWithProfile(
                            modifier = Modifier.padding(top = 35.dp),
                            focusRequester = searchFocusRequester,
                            onSearchQueryChange = { query ->
                                val indoorCode = openIndoorBuildingCode
                                if (indoorCode != null) {
                                    topBarSuggestions = IndoorRoomSearchService.search(
                                        query = query,
                                        scope = IndoorRoomSearchService.Scope.Building,
                                        buildingCode = indoorCode,
                                    ).map {
                                        TopSearchSuggestion.Indoor(
                                            node = it.node,
                                            buildingCode = it.buildingCode,
                                            primaryLabel = it.primaryLabel,
                                            secondaryLabel = it.typeLabel,
                                            tertiaryLabel = it.locationLabel,
                                        )
                                    }
                                } else {
                                    val building = com.example.campusguide.data.buildingSuggestions(
                                        query = query,
                                        activeCampus = com.example.campusguide.ui.components.Campus.SGW,
                                        crossCampus = true,
                                    ).map { com.example.campusguide.ui.components.TopSearchSuggestion.Building(it) }

                                    val indoor = IndoorRoomSearchService.search(
                                        query = query,
                                        scope = IndoorRoomSearchService.Scope.Global,
                                    ).map {
                                        TopSearchSuggestion.Indoor(
                                            node = it.node,
                                            buildingCode = it.buildingCode,
                                            primaryLabel = it.primaryLabel,
                                            secondaryLabel = it.typeLabel,
                                            tertiaryLabel = it.locationLabel,
                                        )
                                    }

                                    topBarSuggestions = indoor + building
                                }
                                shuttleViewModel.handleSearchQuery(query, context)
                            },
                            onSearchSubmit = { query ->
                                val indoorCode = openIndoorBuildingCode
                                if (indoorCode != null) {
                                    // Submit behaves like query-change for indoors: show suggestions; selection drives focusing.
                                    topBarSuggestions = IndoorRoomSearchService.search(
                                        query = query,
                                        scope = IndoorRoomSearchService.Scope.Building,
                                        buildingCode = indoorCode,
                                    ).map {
                                        TopSearchSuggestion.Indoor(
                                            node = it.node,
                                            buildingCode = it.buildingCode,
                                            primaryLabel = it.primaryLabel,
                                            secondaryLabel = it.typeLabel,
                                            tertiaryLabel = it.locationLabel,
                                        )
                                    }
                                } else {
                                    // Prefer indoor matches if any exist
                                    val indoor = IndoorRoomSearchService.search(
                                        query = query,
                                        scope = IndoorRoomSearchService.Scope.Global,
                                    )
                                    if (indoor.isNotEmpty()) {
                                        topBarSuggestions = indoor.map {
                                            TopSearchSuggestion.Indoor(
                                                node = it.node,
                                                buildingCode = it.buildingCode,
                                                primaryLabel = it.primaryLabel,
                                                secondaryLabel = it.typeLabel,
                                                tertiaryLabel = it.locationLabel,
                                            )
                                        }
                                        currentDestination.value = AppDestinations.MAP
                                    } else {
                                        // Check if query matches a campus building first
                                        val matchedBuilding = ALL_CAMPUS_BUILDINGS
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
                            onIndoorResultSelected = { indoor ->
                                // Default click on an indoor suggestion sets destination.
                                pendingIndoorDestination = indoor.node
                                if (openIndoorBuildingCode == null) {
                                    openIndoorBuildingCode = indoor.buildingCode
                                }
                                indoorSetDestTrigger = indoor.node
                                topBarSuggestions = emptyList()
                                searchQuery = ""
                                currentDestination.value = AppDestinations.MAP
                            },
                            onIndoorSetAsStart = { indoor ->
                                pendingIndoorStart = indoor.node
                                val existingDest = pendingIndoorDestination
                                if (existingDest != null && existingDest.buildingCode != indoor.node.buildingCode) {
                                    indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                                        startNode = indoor.node,
                                        destinationNode = existingDest,
                                    )
                                    openIndoorBuildingCode = null
                                } else {
                                    if (openIndoorBuildingCode == null) {
                                        openIndoorBuildingCode = indoor.buildingCode
                                    }
                                    indoorSetStartTrigger = indoor.node
                                }
                                topBarSuggestions = emptyList()
                                currentDestination.value = AppDestinations.MAP
                            },
                            onIndoorSetAsDestination = { indoor ->
                                pendingIndoorDestination = indoor.node
                                val existingStart = pendingIndoorStart
                                if (existingStart != null && existingStart.buildingCode != indoor.node.buildingCode) {
                                    indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                                        startNode = existingStart,
                                        destinationNode = indoor.node,
                                    )
                                    openIndoorBuildingCode = null
                                } else {
                                    if (openIndoorBuildingCode == null) {
                                        openIndoorBuildingCode = indoor.buildingCode
                                    }
                                    indoorSetDestTrigger = indoor.node
                                }
                                topBarSuggestions = emptyList()
                                currentDestination.value = AppDestinations.MAP
                            },
                            shuttleStops = shuttleViewModel.shuttleStops,
                            shuttleUserLatLng = shuttleViewModel.shuttleUserLatLng,
                            onShuttleStopSelected = { _ ->
                                shuttleViewModel.onShuttleStopSelected()
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