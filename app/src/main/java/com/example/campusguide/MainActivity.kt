package com.example.campusguide


import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.campusguide.ui.screens.map.hasLocationPermission
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
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.screens.map.DirectionsTopBarState
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.POIFilterValues
import com.example.campusguide.ui.components.DirectionsTopBar
import com.example.campusguide.ui.components.FocusClearWrapper
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import com.example.campusguide.ui.viewmodels.MapSearchViewModel
import com.example.campusguide.ui.viewmodels.UserLocationViewModel
import com.example.campusguide.indoor.IndoorGraphRegistry
import com.example.campusguide.indoor.IndoorRoomSearchService
import com.example.campusguide.ui.components.POIFilterTags
import com.example.campusguide.ui.components.ignoreFocusClearOnTouch
import com.example.campusguide.ui.directions.IndoorOutdoorRouteRequest
import com.example.campusguide.ui.screens.POIScreen
import com.example.campusguide.ui.viewmodels.BuildingRow

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
    var showAccessibility by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var directionsTopBarState by remember { mutableStateOf(DirectionsTopBarState(active = false)) }
    var directionsGoTrigger by remember { mutableIntStateOf(0) }
    var directionsCancelTrigger by remember { mutableIntStateOf(0) }
    var originPickTrigger by remember { mutableIntStateOf(0) }
    var myLocationTrigger by remember { mutableIntStateOf(0) }
    var topBarTravelMode by remember { mutableStateOf(TravelMode.DRIVE) }
    var directionsEditMode by rememberSaveable { mutableStateOf<DirectionsEditMode?>(null) }
    var directionsDestinationSuggestions by remember { mutableStateOf<List<CampusBuilding>>(emptyList()) }
    var indoorDirectionsQuery by rememberSaveable { mutableStateOf("") }
    var indoorDirectionsSuggestions by remember { mutableStateOf<List<IndoorRoomSearchService.Result>>(emptyList()) }
    var topBarDirectionsDestinationBuilding by remember { mutableStateOf<CampusBuilding?>(null) }
    var poiFilterValues by remember { mutableStateOf(POIFilterValues()) }
    var isSearchFocused by remember { mutableStateOf(false) }



    // Indoor overlay + search triggers
    val viewModel = viewModel<ControlsViewModel>()
    val userLocationViewModel = viewModel<UserLocationViewModel>()
    val userLatLng by userLocationViewModel.userLatLng.collectAsState()
    val nearestId by userLocationViewModel.nearestId.collectAsState()
    val nearestPOIName by userLocationViewModel.nearestPOIName.collectAsState()

    val mapViewmodel: MapSearchViewModel = viewModel()
    var showProfile by remember {mutableStateOf(false)}


    var currentDestination = remember { mutableStateOf(AppDestinations.MAP) }



    val clearDirectionsAndIndoorState = {
        directionsTopBarState = DirectionsTopBarState(active = false)
        mapViewmodel.openIndoorBuildingCode = null
        mapViewmodel.indoorTopCardActive = false
        mapViewmodel.indoorFocusNodeTrigger = null
        mapViewmodel.indoorSetStartTrigger = null
        mapViewmodel.indoorSetDestTrigger = null
        mapViewmodel.pendingIndoorStart = null
        mapViewmodel.pendingIndoorDestination = null
        mapViewmodel.indoorOutdoorRouteRequest = null
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
        if (!hasLocationPermission(context)) {
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
            mapViewmodel.pendingIndoorStart = directionsTopBarState.indoorOriginNode
        }
        if (directionsTopBarState.indoorDestinationNode != null) {
            mapViewmodel.pendingIndoorDestination = directionsTopBarState.indoorDestinationNode
        }
    }


    LaunchedEffect(Unit) {
        userLocationViewModel.fetchUserLocation()
    }


    val suggestionContent: @Composable (Suggestion) -> Unit = { suggestion ->
        BuildingRow(
            suggestion = suggestion,
            nearestId = nearestId,
            nearestPOIName = nearestPOIName,
            userLatLng = userLatLng,
            onSuggestionSelected = { mapViewmodel.onSuggestionSelected(it) },
            onIndoorSetAsStart = { mapViewmodel.onIndoorSetAsStart(it) },
            onIndoorSetAsDestination = { mapViewmodel.onIndoorSetAsDestination(it) }
        )
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
                    NavigationBar(
                        currentDestination = currentDestination,
                        onDestinationSelected = { destination ->
                            val wasOnMap = currentDestination.value == AppDestinations.MAP
                            if (destination == AppDestinations.MAP && wasOnMap) {
                                mapViewmodel.openIndoorBuildingCode = null
                                mapViewmodel.indoorTopCardActive = false
                                mapViewmodel.indoorFocusNodeTrigger = null
                                mapViewmodel.indoorSetStartTrigger = null
                                mapViewmodel.indoorSetDestTrigger = null
                            }
                            currentDestination.value = destination
                        },
                        content = { modifier ->
                            Box(modifier = modifier.fillMaxSize()) {
                                val preservedIndoorDestination =
                                    directionsTopBarState.indoorDestinationNode
                                        ?: mapViewmodel.pendingIndoorDestination

                                when (currentDestination.value) {
                                    AppDestinations.MAP -> {
                                        MapScreen(
                                            viewModel = viewModel,
                                            searchQuery = "$mapViewmodel.searchQuery#$mapViewmodel.searchCounter",
                                            topBarSelectedSuggestion = mapViewmodel.topBarSelectedSuggestion,
                                            onTopBarBuildingConsumed = {
                                                mapViewmodel.topBarSelectedSuggestion = null
                                            },

                                            originPickTrigger = originPickTrigger,
                                            myLocationTrigger = myLocationTrigger,
                                            topBarTravelMode = topBarTravelMode,
                                            topBarDirectionsDestinationBuilding = topBarDirectionsDestinationBuilding,
                                            onTopBarDirectionsDestinationConsumed = {
                                                topBarDirectionsDestinationBuilding = null
                                            },
                                            onBottomSearchClick = {
                                                try {
                                                    searchFocusRequester.requestFocus()
                                                } catch (_: IllegalStateException) {
                                                }
                                            },
                                            onDirectionsTopBarState = { state ->
                                                directionsTopBarState = state
                                            },  // ← add
                                            directionsGoTrigger = directionsGoTrigger,                              // ← add
                                            directionsCancelTrigger = directionsCancelTrigger,                      // ← add
                                            onIndoorOverlayChanged = { mapViewmodel.openIndoorBuildingCode = it },
                                            requestedIndoorBuildingCode = mapViewmodel.openIndoorBuildingCode,
                                            indoorOutdoorRouteRequest = mapViewmodel.indoorOutdoorRouteRequest,
                                            onIndoorOutdoorRouteRequested = { request ->
                                                mapViewmodel.indoorOutdoorRouteRequest = request
                                                mapViewmodel.openIndoorBuildingCode = null
                                            },
                                            onIndoorOutdoorRouteRequestConsumed = {
                                                mapViewmodel.indoorOutdoorRouteRequest = null
                                            },
                                            indoorSearchFocusNodeTrigger = mapViewmodel.indoorFocusNodeTrigger,
                                            indoorSetStartTrigger = mapViewmodel.indoorSetStartTrigger,
                                            indoorSetDestTrigger = mapViewmodel.indoorSetDestTrigger,
                                            onIndoorTriggerConsumed = {
                                                mapViewmodel.indoorFocusNodeTrigger = null
                                                mapViewmodel.indoorSetStartTrigger = null
                                                mapViewmodel.indoorSetDestTrigger = null
                                            },
                                            onIndoorTopCardActiveChanged = { active ->
                                                mapViewmodel.indoorTopCardActive = active
                                            },
                                            hasExistingDestinationSelection = preservedIndoorDestination != null,
                                        )

                                        if (directionsTopBarState.active) {
                                            DirectionsTopBar(
                                                modifier = Modifier.padding(top = 35.dp, start = 8.dp, end = 8.dp)
                                                    .ignoreFocusClearOnTouch(),
                                                originLabel = directionsTopBarState.originLabel,
                                                destinationLabel = directionsTopBarState.destinationLabel,
                                                isCrossCampus = directionsTopBarState.isCrossCampus,
                                                selectedMode = directionsTopBarState.selectedMode,
                                                onModeSelected = { mode -> topBarTravelMode = mode },
                                                routeSummary = directionsTopBarState.routeSummary,
                                                errorMessage = directionsTopBarState.errorMessage,
                                                showActions = directionsTopBarState.showActions,
                                                isLoadingRoute = directionsTopBarState.isLoadingRoute,
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
                                                    directionsCancelTrigger++
                                                    topBarTravelMode = TravelMode.DRIVE
                                                    clearDirectionsAndIndoorState()
                                                },
                                                shuttleStatus = directionsTopBarState.shuttleStatus,
                                                canUseShuttle = directionsTopBarState.canUseShuttle,
                                                onOriginClick = if (mapViewmodel.openIndoorBuildingCode != null) {
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
                                                    directionsEditMode =

                                                        when {
                                                        mapViewmodel.openIndoorBuildingCode != null && directionsEditMode == DirectionsEditMode.INDOOR_DESTINATION -> null
                                                        mapViewmodel.openIndoorBuildingCode != null -> DirectionsEditMode.INDOOR_DESTINATION
                                                        directionsEditMode == DirectionsEditMode.OUTDOOR_DESTINATION  && !mapViewmodel.searchVanish -> null
                                                        else -> DirectionsEditMode.OUTDOOR_DESTINATION
                                                    }
                                                    directionsDestinationSuggestions = emptyList()
                                                    indoorDirectionsQuery = ""
                                                    indoorDirectionsSuggestions = emptyList()
                                                    mapViewmodel.searchVanish = false
                                                },
                                                extraContent = {


                                                    if (directionsEditMode == DirectionsEditMode.OUTDOOR_DESTINATION) {

                                                        if(!mapViewmodel.searchVanish){

                                                            SearchBarWithProfile(
                                                                modifier = Modifier.padding(top = 20.dp).ignoreFocusClearOnTouch(),
                                                                focusRequester = searchFocusRequester,
                                                                onSearchQueryChange = mapViewmodel::onSearchQueryChange,

                                                                onSearchSubmit = mapViewmodel::onSearchSubmit,
                                                                suggestions = mapViewmodel.topBarSuggestions,
                                                                suggestionContent = suggestionContent,
                                                                suggestionKey = mapViewmodel.suggestionKey,
                                                                showProfile = false
                                                            )
                                                        }
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
                                                                                        mapViewmodel.pendingIndoorStart = pickedNode
                                                                                        val existingDest = mapViewmodel.pendingIndoorDestination
                                                                                        if (existingDest != null && !existingDest.buildingCode.equals(pickedNode.buildingCode, ignoreCase = true)) {
                                                                                            mapViewmodel.indoorOutdoorRouteRequest =
                                                                                                IndoorOutdoorRouteRequest(
                                                                                                    startNode = pickedNode,
                                                                                                    destinationNode = existingDest,
                                                                                                )
                                                                                            mapViewmodel.openIndoorBuildingCode = null
                                                                                        } else {
                                                                                            mapViewmodel.openIndoorBuildingCode = pickedBuildingCode
                                                                                            mapViewmodel.indoorSetStartTrigger = pickedNode
                                                                                        }
                                                                                    } else {
                                                                                        mapViewmodel.pendingIndoorDestination = pickedNode
                                                                                        val existingStart = mapViewmodel.pendingIndoorStart
                                                                                        if (existingStart != null && !existingStart.buildingCode.equals(pickedNode.buildingCode, ignoreCase = true)) {
                                                                                            mapViewmodel.indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                                                                                                startNode = existingStart,
                                                                                                destinationNode = pickedNode,
                                                                                            )
                                                                                            mapViewmodel.openIndoorBuildingCode = null
                                                                                        } else {
                                                                                            mapViewmodel.openIndoorBuildingCode = pickedBuildingCode
                                                                                            mapViewmodel.indoorSetDestTrigger = pickedNode
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
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.semantics {
                                                                contentDescription = fallback
                                                            }
                                                        )
                                                    }
                                                },
                                                route = directionsTopBarState.route
                                            )
                                        } else{

                                            SearchBarWithProfile(
                                                modifier = Modifier.padding(top = 35.dp).ignoreFocusClearOnTouch(),
                                                focusRequester = searchFocusRequester,
                                                onSearchQueryChange = mapViewmodel::onSearchQueryChange,
                                                onSearchSubmit = mapViewmodel::onSearchSubmit,
                                                onProfileClick = { showProfile = true },
                                                suggestions = mapViewmodel.topBarSuggestions,

                                                suggestionContent = suggestionContent,
                                                suggestionKey = mapViewmodel.suggestionKey,
                                            )
                                        }
                                    }


                                    AppDestinations.CALENDAR -> {
                                        CalendarScreen()
                                    }
                                    AppDestinations.POI -> {
                                        POIScreen(
                                            viewModel = viewModel,
                                            onDirectionsTopBarState = { state ->
                                                directionsTopBarState = state
                                            },
                                            directionsGoTrigger = directionsGoTrigger,                              // ← add
                                            directionsCancelTrigger = directionsCancelTrigger,
                                            topBarTravelMode = topBarTravelMode,
                                            onBottomSearchClick = {
                                                try {
                                                    searchFocusRequester.requestFocus()
                                                } catch (_: IllegalStateException) {
                                                }
                                            },
                                            topBarSelectedPOISuggestion = mapViewmodel.topBarSelectedSuggestion,
                                            onTopBarBuildingConsumed = {
                                                mapViewmodel.topBarSelectedSuggestion = null
                                            },
                                            poiFilters = poiFilterValues
                                            )



                                        if (directionsTopBarState.active) {
                                            DirectionsTopBar(
                                                modifier = Modifier.padding(top = 35.dp, start = 8.dp, end = 8.dp)
                                                    .ignoreFocusClearOnTouch(),
                                                originLabel = directionsTopBarState.originLabel,
                                                destinationLabel = directionsTopBarState.destinationLabel,
                                                isCrossCampus = directionsTopBarState.isCrossCampus,
                                                selectedMode = directionsTopBarState.selectedMode,
                                                onModeSelected = { mode -> topBarTravelMode = mode },
                                                routeSummary = directionsTopBarState.routeSummary,
                                                errorMessage = directionsTopBarState.errorMessage,
                                                showActions = directionsTopBarState.showActions,
                                                isLoadingRoute = directionsTopBarState.isLoadingRoute,
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
                                                    directionsCancelTrigger++
                                                    topBarTravelMode = TravelMode.DRIVE
                                                    clearDirectionsAndIndoorState()
                                                },
                                                shuttleStatus = directionsTopBarState.shuttleStatus,
                                                canUseShuttle = directionsTopBarState.canUseShuttle,
                                                route = directionsTopBarState.route
                                            )
                                        }else{

                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                                                SearchBarWithProfile(
                                                    modifier = Modifier.padding(top = 35.dp).ignoreFocusClearOnTouch(),
                                                    focusRequester = searchFocusRequester,
                                                    onSearchQueryChange = mapViewmodel::onPOISearchQueryChange,
                                                    onSearchSubmit = mapViewmodel::onPOISearchSubmit,
                                                    onProfileClick = { showProfile = true },
                                                    suggestions = mapViewmodel.topBarSuggestions,

                                                    suggestionContent = suggestionContent,
                                                    suggestionKey = mapViewmodel.suggestionKey,
                                                    onFocusChange = { isSearchFocused = it }

                                                )
                                                if (!(isSearchFocused && mapViewmodel.topBarSuggestions.isNotEmpty())) {
                                                    POIFilterTags(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .wrapContentWidth(
                                                                Alignment.CenterHorizontally
                                                            ).padding(horizontal = 40.dp),
                                                        poiFilters = poiFilterValues,
                                                        onPOITagSelect = {
                                                            poiFilterValues = poiFilterValues.copy(
                                                                categoriesIncluded = poiFilterValues.categoriesIncluded + it
                                                            )
                                                        },
                                                        onPOITagDismiss = {
                                                            poiFilterValues = poiFilterValues.copy(
                                                                categoriesIncluded = poiFilterValues.categoriesIncluded - it
                                                            )
                                                        },
                                                        onPOIRatingClick = {
                                                            poiFilterValues =
                                                                poiFilterValues.copy(rating = (poiFilterValues.rating + 1.0) % 5)
                                                        },
                                                        onPOIDistanceClick = {
                                                            poiFilterValues =
                                                                poiFilterValues.copy(distanceLimit = (poiFilterValues.distanceLimit + 1000.0f) % 10000f)
                                                        }
                                                    )
                                                }
                                            }


                                        }


                                    }

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
    POI("POI", AppIcon.Drawable(R.drawable.poi_icon)),
}


private enum class DirectionsEditMode {
    OUTDOOR_DESTINATION,
    INDOOR_ORIGIN,
    INDOOR_DESTINATION,
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