package com.example.campusguide.ui.screens.map

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.campusguide.R
import com.example.campusguide.ui.components.BuildingDetailsBottomSheet
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import com.example.campusguide.ui.map.geoJson.GeoJsonOverlay
import com.example.campusguide.ui.map.models.BuildingInfo
import com.example.campusguide.ui.directions.DirectionsStep
import com.example.campusguide.ui.directions.DirectionsUiState
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.map.utils.BuildingHit
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.Locale
import kotlin.coroutines.resume
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.IndoorOutdoorRouteRequest
import com.example.campusguide.ui.directions.isCrossCampusRoute
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.indoor.CrossFloorRouter
import com.example.campusguide.indoor.IndoorGraphRegistry
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import com.example.campusguide.indoor.IndoorPathfinder
import com.example.campusguide.ui.components.ShuttleStopInfoCard
import com.example.campusguide.ui.map.geoJson.MapMarkerFactory
import com.example.campusguide.ui.shuttle.ShuttleTracker
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import androidx.core.content.ContextCompat
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.ignoreFocusClearOnTouch
import com.example.campusguide.ui.directions.detectCampus
import com.example.campusguide.ui.screens.IndoorMapScreen
import com.example.campusguide.ui.shuttle.ShuttleSchedule
import com.example.campusguide.ui.viewmodels.UserLocationViewModel
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Polyline
import kotlin.div
import kotlin.text.get

private const val CAMERA_ANIMATION_DURATION_MS = 1500
private const val CAMPUS_ZOOM_LEVEL = 15f

@Composable
fun MapScreen(
    searchQuery: String = "",
    topBarSelectedSuggestion: Suggestion? = null,
    onTopBarBuildingConsumed: () -> Unit = {},
    topBarDirectionsDestinationBuilding: CampusBuilding? = null,
    onTopBarDirectionsDestinationConsumed: () -> Unit = {},
    hasExistingDestinationSelection: Boolean = false,
    onIndoorOverlayChanged: (String?) -> Unit = {},
    requestedIndoorBuildingCode: String? = null,
    indoorOutdoorRouteRequest: IndoorOutdoorRouteRequest? = null,
    onIndoorOutdoorRouteRequested: (IndoorOutdoorRouteRequest) -> Unit = {},
    onIndoorOutdoorRouteRequestConsumed: () -> Unit = {},
    indoorSearchFocusNodeTrigger: IndoorNode? = null,
    indoorSetStartTrigger: IndoorNode? = null,
    indoorSetDestTrigger: IndoorNode? = null,
    onIndoorTriggerConsumed: () -> Unit = {},
    onIndoorTopCardActiveChanged: (Boolean) -> Unit = {},
    onBottomSearchClick: () -> Unit = {},
    onDirectionsTopBarState: (DirectionsTopBarState) -> Unit = {},
    directionsGoTrigger: Int = 0,
    directionsCancelTrigger: Int = 0,
    topBarTravelMode: TravelMode = TravelMode.DRIVE,
    viewModel: ControlsViewModel = viewModel<ControlsViewModel>(),
    originPickTrigger: Int = 0,
    myLocationTrigger: Int = 0,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accessibilityState = LocalAccessibilityState.current

    val userLocationViewModel: UserLocationViewModel = viewModel()

    // State management
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var sgwOverlay by remember { mutableStateOf<GeoJsonOverlay?>(null) }
    var loyOverlay by remember { mutableStateOf<GeoJsonOverlay?>(null) }
    var sgwAttached by remember { mutableStateOf(false) }
    var loyAttached by remember { mutableStateOf(false) }
    var selectedCampus by rememberSaveable { mutableStateOf(getSavedCampus(context)) }
    var selectedBuildingInfo by remember { mutableStateOf<BuildingInfo?>(null) }
    var searchMarker by remember { mutableStateOf<Marker?>(null) }
    var pendingSearchQuery by remember { mutableStateOf(searchQuery) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var controlsVisible = viewModel.controlsVisible

    val snackBarHostState = remember { SnackbarHostState() }

    val repo = remember { GoogleRoutesRepository() }
    var directionsUiState by remember { mutableStateOf(DirectionsUiState()) }
    var travelMode by rememberSaveable { mutableStateOf(TravelMode.DRIVE) }
    var isPickingOrigin by remember { mutableStateOf(false) }
    val routePolylines = remember { mutableListOf<Polyline?>() }
    var routeRequestGeneration by remember { mutableIntStateOf(0) }

    val defaultOrigin by userLocationViewModel.effectiveOrigin.collectAsState()
    // Track the selected building's LatLng for directions
    var selectedBuildingLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Autocomplete state (cross-campus always enabled per US-2.5 AC4)
    var originDisplayName  by remember { mutableStateOf<String?>(null) }

    // Track origin and destination buildings for cross-campus detection
    var originBuilding by remember { mutableStateOf<CampusBuilding?>(null) }
    var destinationBuilding by remember { mutableStateOf<Suggestion?>(null) }
    var legLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var legFallbackMessage by remember { mutableStateOf<String?>(null) }
    var topBarOriginOverride by remember { mutableStateOf<String?>(null) }
    var topBarDestinationOverride by remember { mutableStateOf<String?>(null) }
    var isIndoorOutdoorFlow by remember { mutableStateOf(false) }
    var indoorOutdoorFallbackParts by remember { mutableStateOf<List<String>>(emptyList()) }
    var indoorFlowStartNode by remember { mutableStateOf<IndoorNode?>(null) }
    var indoorFlowStartAccessNode by remember { mutableStateOf<IndoorNode?>(null) }
    var indoorFlowEndAccessNode by remember { mutableStateOf<IndoorNode?>(null) }
    var indoorFlowEndNode by remember { mutableStateOf<IndoorNode?>(null) }

    var mapTapFocusNodeTrigger by remember { mutableStateOf<IndoorNode?>(null) }
    var mapTapSetStartNodeTrigger by remember { mutableStateOf<IndoorNode?>(null) }
    var mapTapSetDestNodeTrigger by remember { mutableStateOf<IndoorNode?>(null) }
    var indoorTriggerVersion by remember { mutableIntStateOf(0) }

    // Shuttle state (US-3.1)
    val shuttleTracker = remember { ShuttleTracker() }
    // Reserved for US-3.2: enables removing/updating markers when switching campuses
    val shuttleMarkerMap = remember { mutableMapOf<String, Marker>() }
    var selectedShuttleStop by remember { mutableStateOf<ShuttleStop?>(null) }


    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    var routePolylineRef by remember {
        mutableStateOf<Polyline?>(null)
    }

    val mapView = remember { MapView(context) }


    // Get user location for default origin
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return@LaunchedEffect

        userLocationViewModel.fetchUserLocation()
    }

    // Location services
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    // Indoor navigation state (US-5.1 – US-5.6)
    var indoorBuildingCode by remember { mutableStateOf<String?>(null) }
    var indoorDirectionsState by remember { mutableStateOf<DirectionsTopBarState?>(null) }
    var latestIndoorOriginNode by remember { mutableStateOf<IndoorNode?>(null) }
    var latestIndoorDestinationNode by remember { mutableStateOf<IndoorNode?>(null) }
    var suppressIndoorStateUpdates by remember { mutableStateOf(false) }

    // Sync the requested indoor building code from the top bar into the map overlay state.
    LaunchedEffect(requestedIndoorBuildingCode) {
        if (requestedIndoorBuildingCode == null) {
            suppressIndoorStateUpdates = true
            indoorBuildingCode = null
            indoorDirectionsState = null
            latestIndoorOriginNode = null
            latestIndoorDestinationNode = null
            mapTapFocusNodeTrigger = null
            mapTapSetStartNodeTrigger = null
            mapTapSetDestNodeTrigger = null
        } else if (requestedIndoorBuildingCode != indoorBuildingCode) {
            suppressIndoorStateUpdates = false
            indoorBuildingCode = requestedIndoorBuildingCode
            // Clear indoor directions state when opening indoor map fresh
            // to prevent stale state from previous attempts
            indoorDirectionsState = null
        }
    }

    // Publish whether the indoor overlay is open (and which building)
    LaunchedEffect(indoorBuildingCode) {
        if (indoorBuildingCode != null) {
            suppressIndoorStateUpdates = false
        }
        onIndoorOverlayChanged(indoorBuildingCode)
    }

    LaunchedEffect(
        indoorDirectionsState?.indoorOriginNode,
        indoorDirectionsState?.indoorDestinationNode,
    ) {
        indoorDirectionsState?.indoorOriginNode?.let { latestIndoorOriginNode = it }
        indoorDirectionsState?.indoorDestinationNode?.let { latestIndoorDestinationNode = it }
    }

    LaunchedEffect(indoorSearchFocusNodeTrigger, indoorSetStartTrigger, indoorSetDestTrigger) {
        if (
            indoorSearchFocusNodeTrigger != null ||
            indoorSetStartTrigger != null ||
            indoorSetDestTrigger != null
        ) {
            indoorTriggerVersion++
        }
    }

    fun resolveBuildingLatLng(building: CampusBuilding): LatLng {
        val overlay = when (building.campus) {
            Campus.SGW    -> sgwOverlay
            Campus.LOYOLA -> loyOverlay
        } ?: return when (building.campus) {
            Campus.SGW    -> LatLng(45.4972, -73.5789)
            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
        }

        val polygons = overlay.getBuildings()[building.buildingCode]
        if (!polygons.isNullOrEmpty()) {
            val pts = polygons.first().points
            return LatLng(
                pts.sumOf { it.latitude } / pts.size,
                pts.sumOf { it.longitude } / pts.size
            )
        }

        return when (building.campus) {
            Campus.SGW    -> LatLng(45.4972, -73.5789)
            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
        }
    }

    LaunchedEffect(indoorOutdoorRouteRequest) {
        val request = indoorOutdoorRouteRequest ?: return@LaunchedEffect
        onIndoorOutdoorRouteRequestConsumed()

        indoorDirectionsState = null
        mapTapFocusNodeTrigger = null
        mapTapSetStartNodeTrigger = null
        mapTapSetDestNodeTrigger = null
        val startNode = request.startNode
        val destinationNode = request.destinationNode
        val startBuilding = findCampusBuilding(startNode.buildingCode)
        val endBuilding = findCampusBuilding(destinationNode.buildingCode)

        if (startBuilding == null || endBuilding == null) {
            isIndoorOutdoorFlow = false
            indoorFlowStartNode = null
            indoorFlowStartAccessNode = null
            indoorFlowEndAccessNode = null
            indoorFlowEndNode = null
            legLabels = emptyList()
            legFallbackMessage = "Unable to resolve one of the selected buildings. Fallback: route to destination building only."
            topBarOriginOverride = startNode.label
            topBarDestinationOverride = destinationNode.label
            directionsUiState = directionsUiState.copy(
                step = DirectionsStep.PickDestination,
                isLoadingRoute = false,
                errorMessage = "Could not build multi-leg route for selected indoor rooms",
            )
            return@LaunchedEffect
        }

        originBuilding = startBuilding
        destinationBuilding = endBuilding
        topBarOriginOverride = startNode.label
        topBarDestinationOverride = destinationNode.label
        isIndoorOutdoorFlow = true
        indoorFlowStartNode = startNode
        indoorFlowEndNode = destinationNode

        val startAccess = findAccessNode(startNode.buildingCode)
        val endAccess = findAccessNode(destinationNode.buildingCode)
        indoorFlowStartAccessNode = startAccess
        indoorFlowEndAccessNode = endAccess

        val startIndoorOk = startAccess != null && hasIndoorLegPath(startNode, startAccess, accessibilityState)
        val endIndoorOk = endAccess != null && hasIndoorLegPath(endAccess, destinationNode, accessibilityState)

        val plannedLegs = buildList {
            add("Indoor leg 1: ${startNode.label} → ${startAccess?.label ?: "building access point"}")
            add("Outdoor leg: ${startBuilding.buildingCode} → ${endBuilding.buildingCode}")
            add("Indoor leg 2: ${endAccess?.label ?: "building access point"} → ${destinationNode.label}")
        }

        val fallbackParts = mutableListOf<String>()
        if (!startIndoorOk) {
            fallbackParts.add("Start indoor leg unavailable")
        }
        if (!endIndoorOk) {
            fallbackParts.add("Destination indoor leg unavailable")
        }

        val outdoorOrigin = resolveBuildingLatLng(startBuilding)
        val outdoorDestination = resolveBuildingLatLng(endBuilding)

        legLabels = plannedLegs
        indoorOutdoorFallbackParts = fallbackParts
        legFallbackMessage = if (fallbackParts.isEmpty()) {
            null
        } else {
            "${fallbackParts.joinToString(". ")}. Outdoor leg will fallback to destination building if needed."
        }

        val hit = BuildingHit(
            id = endBuilding.buildingCode,
            properties = JSONObject().apply {
                put("building-code", endBuilding.buildingCode)
                put("building-name", endBuilding.buildingName)
                put("address", endBuilding.address)
            }
        )
        directionsUiState = directionsUiState.copy(
            isLoadingRoute = false,
            errorMessage = null,
            step = DirectionsStep.PlanRoute(
                origin = outdoorOrigin,
                destination = outdoorDestination,
                buildingHit = hit,
            ),
        )
    }
    // Sync travel mode from top bar selection
    LaunchedEffect(topBarTravelMode) {
            travelMode = topBarTravelMode
        val step = directionsUiState.step
        if (step is DirectionsStep.ShowingRoute) {
            directionsUiState = directionsUiState.copy(
                step = DirectionsStep.PlanRoute(
                    origin = step.origin,
                    destination = step.destination,
                    buildingHit = step.buildingHit,
                )
            )
        }

    }
// Handle Go button from top bar
    LaunchedEffect(directionsGoTrigger) {
        if (directionsGoTrigger == 0) return@LaunchedEffect

        routePolylines.forEach { it?.remove() }
        routePolylines.clear()

        val requestGeneration = routeRequestGeneration
        val indoorState = indoorDirectionsState
        val indoorOrigin = indoorState?.indoorOriginNode ?: if (indoorState == null) latestIndoorOriginNode else null
        val indoorDestination = indoorState?.indoorDestinationNode ?: if (indoorState == null) latestIndoorDestinationNode else null

        directionsUiState = directionsUiState.copy(isLoadingRoute = true, errorMessage = null)

        if (
            indoorBuildingCode != null &&
            indoorOrigin != null &&
            indoorDestination != null &&
            !indoorOrigin.buildingCode.equals(indoorDestination.buildingCode, ignoreCase = true)
        ) {
            mapTapFocusNodeTrigger = null
            mapTapSetStartNodeTrigger = null
            mapTapSetDestNodeTrigger = null
            onIndoorOutdoorRouteRequested(
                IndoorOutdoorRouteRequest(
                    startNode = indoorOrigin,
                    destinationNode = indoorDestination,
                )
            )
            indoorBuildingCode = null
            return@LaunchedEffect
        }

        val step = directionsUiState.step as? DirectionsStep.PlanRoute ?: return@LaunchedEffect
        var drawRouteResult = DrawRouteResult(emptyList(), "Failed to load route")

        centerOnOrigin(googleMap, step.origin, context)

        val isCrossCampus = isCrossCampusRoute(originBuilding, destinationBuilding, step.origin)
        directionsUiState = directionsUiState.copy(isLoadingRoute = true, errorMessage = null)

        val departure = canUseShuttle(step.origin, step.destination, travelMode)
        if(departure != null){
            drawRouteResult =
                    drawRoute(
                        step,
                        step.origin,
                        step.destination,
                        "SHUTTLE",
                        googleMap,
                        getDirectionsUiState = { directionsUiState },
                        onDirectionsUiStateChange = { directionsUiState = it },
                        repo,
                        isCrossCampus,
                        requestGeneration,
                        routeRequestGeneration,
                        isIndoorOutdoorFlow,
                        destinationBuilding,
                        indoorOutdoorFallbackParts,
                        onLegFallbackMessage = { legFallbackMessage = it },
                        defaultOrigin,

                        legLabels = legLabels,
                        onLegLabels = { legLabels = it },
                        departure = departure
                    )

        }
        else {
            drawRouteResult =
                drawRoute(
                    step,
                    step.origin,
                    step.destination,
                    travelMode.name,
                    googleMap,
                    getDirectionsUiState = { directionsUiState },
                    onDirectionsUiStateChange = { directionsUiState = it },
                    repo,
                    isCrossCampus,
                    requestGeneration,
                    routeRequestGeneration,
                    isIndoorOutdoorFlow,
                    destinationBuilding,
                    indoorOutdoorFallbackParts,
                    onLegFallbackMessage = { legFallbackMessage = it },
                    defaultOrigin,
                    legLabels = legLabels,
                    onLegLabels = { legLabels = it }
                )
            
        }

        routePolylines.addAll(drawRouteResult.polylines)

        scope.launch {
            snackBarHostState.showSnackbar(
                message = drawRouteResult.snackBarMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

// Handle Cancel from top bar
    LaunchedEffect(directionsCancelTrigger) {
        if (directionsCancelTrigger == 0) return@LaunchedEffect

        routePolylines.forEach { it?.remove() }
        routePolylines.clear()



        suppressIndoorStateUpdates = true
        routeRequestGeneration++
        routePolylineRef?.remove()
        routePolylineRef = null
        onIndoorTriggerConsumed()
        onIndoorOverlayChanged(null)
        onIndoorTopCardActiveChanged(false)
        indoorBuildingCode = null
        selectedBuildingInfo = null
        indoorDirectionsState = null
        latestIndoorOriginNode = null
        latestIndoorDestinationNode = null
        isIndoorOutdoorFlow = false
        indoorOutdoorFallbackParts = emptyList()
        indoorFlowStartNode = null
        indoorFlowStartAccessNode = null
        indoorFlowEndAccessNode = null
        indoorFlowEndNode = null
        mapTapFocusNodeTrigger = null
        mapTapSetStartNodeTrigger = null
        mapTapSetDestNodeTrigger = null
        legLabels = emptyList()
        legFallbackMessage = null
        topBarOriginOverride = null
        topBarDestinationOverride = null
        directionsUiState = directionsUiState.copy(
            step = DirectionsStep.PickDestination,
            errorMessage = null,
        )
        isPickingOrigin = false

        searchMarker?.remove()
        searchMarker = null

    }

// Handle origin pick mode trigger from top bar
    LaunchedEffect(originPickTrigger) {
        if (originPickTrigger == 0) return@LaunchedEffect
        isPickingOrigin = true
    }

// Handle "My Location" trigger from top bar
    LaunchedEffect(myLocationTrigger) {
        if (myLocationTrigger == 0) return@LaunchedEffect
        val step = directionsUiState.step as? DirectionsStep.PlanRoute ?: return@LaunchedEffect
        directionsUiState = directionsUiState.copy(
            step = step.copy(origin = defaultOrigin)
        )
        originDisplayName = null
        originBuilding = null
        isPickingOrigin = false
    }

// Publish top-bar state to MainActivity whenever directions state changes
    LaunchedEffect(
        directionsUiState,
        travelMode,
        originBuilding,
        destinationBuilding,
        originDisplayName,
        legLabels,
        legFallbackMessage,
        topBarOriginOverride,
        topBarDestinationOverride,
        indoorBuildingCode,
        indoorDirectionsState,
    ) {
        val indoorOriginNode = indoorDirectionsState?.indoorOriginNode
        val indoorDestinationNode = indoorDirectionsState?.indoorDestinationNode
        val hasCrossBuildingIndoorSelection =
            indoorOriginNode != null &&
                    indoorDestinationNode != null &&
                    !indoorOriginNode.buildingCode.equals(indoorDestinationNode.buildingCode, ignoreCase = true)

        if (indoorBuildingCode != null) {
            val isRouteInProgress =
                (isIndoorOutdoorFlow && hasCrossBuildingIndoorSelection && directionsUiState.step is DirectionsStep.ShowingRoute) ||
                        (indoorDirectionsState?.routeSummary != null && hasCrossBuildingIndoorSelection)

            if (isRouteInProgress) {
                onDirectionsTopBarState(DirectionsTopBarState(active = false))
                return@LaunchedEffect
            }

            val indoorState = indoorDirectionsState
            if (indoorState != null) {
                onDirectionsTopBarState(indoorState)
            } else {
                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = false,
                        originLabel = "Tap a room to set start",
                        destinationLabel = "Tap a room to set destination",
                        showActions = true,
                        goEnabled = false,
                        showTravelModes = true,
                        goLabel = "Go",
                        cancelLabel = "Cancel",
                    )
                )
            }
            return@LaunchedEffect
        }

        if (
            indoorDirectionsState != null &&
            directionsUiState.step is DirectionsStep.PickDestination &&
            hasCrossBuildingIndoorSelection
        ) {
            onDirectionsTopBarState(indoorDirectionsState!!.copy(active = true))
            return@LaunchedEffect
        }

        when (val step = directionsUiState.step) {
            is DirectionsStep.PlanRoute -> {
                // Automatically detect cross-campus routes
                val isCrossCampus = isCrossCampusRoute(originBuilding,
                    destinationBuilding, step.origin)
                val canUseShuttle = canUseShuttle(step.origin, step.destination, travelMode) != null

                val shuttleStatus = ShuttleSchedule.nextDeparture(detectCampus(step.destination))

                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = topBarOriginOverride ?: (originDisplayName ?: "Your location"),
                        destinationLabel = topBarDestinationOverride ?: buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = isCrossCampus,
                        selectedMode = travelMode,
                        errorMessage = directionsUiState.errorMessage,
                        legLabels = legLabels,
                        legFallbackMessage = legFallbackMessage,
                        isLoadingRoute = directionsUiState.isLoadingRoute,
                        showActions = true,
                        isPickingOrigin = isPickingOrigin,
                        canUseShuttle = canUseShuttle,
                        shuttleStatus = shuttleStatus,
                        goEnabled = true,
                        showTravelModes = true,
                        goLabel = "Go",
                        cancelLabel = "Cancel",
                    )
                )
            }
            is DirectionsStep.ShowingRoute -> {
                // Automatically detect cross-campus routes
                val isCrossCampus = isCrossCampusRoute(originBuilding,
                    destinationBuilding, step.origin)
                val canUseShuttle = canUseShuttle(step.origin, step.destination, travelMode) != null
                val shuttleStatus = ShuttleSchedule.nextDeparture(detectCampus(step.destination))

                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = topBarOriginOverride ?: (originDisplayName ?: "Your location"),
                        destinationLabel = topBarDestinationOverride ?: buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = isCrossCampus,
                        selectedMode = travelMode,
                        routeSummary = buildRouteSummary(step.route.distanceMeters, step.route.durationSeconds),
                        legLabels = legLabels,
                        legFallbackMessage = legFallbackMessage,
                        showActions = false,
                        route = step.route,
                        canUseShuttle = canUseShuttle,
                        currentSteps = step.route.legs.firstOrNull(),
                        shuttleStatus = shuttleStatus,
                        goEnabled = true,
                        showTravelModes = true,
                        goLabel = "Go",
                        cancelLabel = "Cancel",
                    )
                )
            }
            else -> {
                val keepActive = legLabels.isNotEmpty() || !legFallbackMessage.isNullOrBlank()
                onDirectionsTopBarState(
                    if (keepActive) {
                        DirectionsTopBarState(
                            active = true,
                            originLabel = topBarOriginOverride ?: "Your location",
                            destinationLabel = topBarDestinationOverride ?: "Destination",
                            selectedMode = travelMode,
                            errorMessage = directionsUiState.errorMessage,
                            legLabels = legLabels,
                            legFallbackMessage = legFallbackMessage,
                            showActions = false,
                            goEnabled = true,
                            showTravelModes = true,
                            goLabel = "Go",
                            cancelLabel = "Cancel",
                        )
                    } else {
                        DirectionsTopBarState(active = false)
                    }
                )
            }
        }
    }

    LaunchedEffect(topBarSelectedSuggestion) {

        when (topBarSelectedSuggestion) {
            is CampusBuilding -> {

                isIndoorOutdoorFlow = false
                indoorOutdoorFallbackParts = emptyList()
                indoorFlowStartNode = null
                indoorFlowStartAccessNode = null
                indoorFlowEndAccessNode = null
                indoorFlowEndNode = null
                mapTapFocusNodeTrigger = null
                mapTapSetStartNodeTrigger = null
                mapTapSetDestNodeTrigger = null
                legLabels = emptyList()
                legFallbackMessage = null
                topBarOriginOverride = null
                topBarDestinationOverride = null

                val building = topBarSelectedSuggestion
                val latLng = resolveBuildingLatLng(building)

                // Drop pin on the building
                searchMarker?.remove()
                searchMarker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(building.buildingName)
                )
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                // Set as To destination and open route panel
                destinationBuilding = building  // Track for cross-campus detection
                val hit = BuildingHit(
                    id = building.buildingCode,
                    properties = JSONObject().apply {
                        put("building-code", building.buildingCode)
                        put("building-name", building.buildingName)
                        put("address", building.address)
                    }
                )
                directionsUiState = directionsUiState.copy(
                    step = DirectionsStep.PlanRoute(
                        origin = defaultOrigin,
                        destination = latLng,
                        buildingHit = hit,
                    )
                )
            }

            is ShuttleStop -> {
                val stop = topBarSelectedSuggestion
                val latLng = stop.latLng

                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                // Set as To destination and open route panel
                destinationBuilding = stop  // Track for cross-campus detection
                val hit = BuildingHit(
                    id = stop.id,
                    properties = JSONObject().apply {
                        put("building-code", stop.id)
                        put("building-name", stop.name)
                        put("address", stop.description)
                    }
                )
                directionsUiState = directionsUiState.copy(
                    step = DirectionsStep.PlanRoute(
                        origin = defaultOrigin,
                        destination = latLng,
                        buildingHit = hit,
                    )
                )
            }
            else -> {
                return@LaunchedEffect
            }
        }
        onTopBarBuildingConsumed()
    }

    LaunchedEffect(topBarDirectionsDestinationBuilding) {
        val building = topBarDirectionsDestinationBuilding ?: return@LaunchedEffect
        val latLng = resolveBuildingLatLng(building)

        destinationBuilding = building
        val hit = BuildingHit(
            id = building.buildingCode,
            properties = JSONObject().apply {
                put("building-code", building.buildingCode)
                put("building-name", building.buildingName)
                put("address", building.address)
            }
        )

        when (val step = directionsUiState.step) {
            is DirectionsStep.PlanRoute -> {
                directionsUiState = directionsUiState.copy(
                    errorMessage = null,
                    step = step.copy(
                        destination = latLng,
                        buildingHit = hit,
                    )
                )
            }
            is DirectionsStep.ShowingRoute -> {
                routePolylineRef?.remove()
                routePolylineRef = null
                directionsUiState = directionsUiState.copy(
                    errorMessage = null,
                    step = DirectionsStep.PlanRoute(
                        origin = step.origin,
                        destination = latLng,
                        buildingHit = hit,
                    ),
                )
            }
            else -> {
                directionsUiState = directionsUiState.copy(
                    errorMessage = null,
                    step = DirectionsStep.PlanRoute(
                        origin = defaultOrigin,
                        destination = latLng,
                        buildingHit = hit,
                    ),
                )
            }
        }

        onTopBarDirectionsDestinationConsumed()
    }

    val activity = LocalActivity.current
    // Permission handling
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false


        if (fineLocationGranted && coarseLocationGranted) {
            activity?.let { act ->
                checkLocationSettings(act, locationSettingsLauncher)
            }

            googleMap?.let { map ->
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true
                    startLocationTracking(
                        context,
                        fusedLocationProviderClient,
                        googleMap,
                        sgwOverlay,
                        loyOverlay,
                        userLocationViewModel,
                        ) { callback ->
                        locationCallback = callback
                    }
                }
            }
        }
    }

    // Request location permissions
    LaunchedEffect(Unit) {
        if (!isLocationEnabled(context)) {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
            // check accuracy directly
            checkLocationSettings(context, locationSettingsLauncher)

    }



    // Search function
    val scheduleSearch: (String) -> Unit = remember {
        { rawQuery: String ->
            val query = rawQuery.trim()
            pendingSearchQuery = query

            if (googleMap == null) {

                return@remember
            }


            searchJob?.cancel()

            if (query.isBlank()) {
                searchMarker?.remove()
                searchMarker = null
                return@remember
            }

            searchJob = scope.launch {
                delay(400)

                if (query != pendingSearchQuery) return@launch

                val address = withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // NO SONAR: minSdk=33, kept for explicit API clarity
                            suspendCancellableCoroutine { cont ->
                                geocoder.getFromLocationName(query, 1) { results ->
                                    cont.resume(results.firstOrNull())
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            geocoder.getFromLocationName(query, 1)?.firstOrNull()
                        }
                    } catch (_: Exception) {
                        null
                    }
                }

                if (address == null) {

                    return@launch
                }


                val latLng = LatLng(address.latitude, address.longitude)
                searchMarker?.remove()
                searchMarker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(query)
                )

                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    }

    // Trigger search when searchQuery changes from parent
    LaunchedEffect(searchQuery) {

        val actualQuery = searchQuery.substringBefore('#')
        if (actualQuery.isNotBlank()) {

            scheduleSearch(actualQuery)
        } else {

        }
    }

    // Campus switching function
    fun switchCampus(campus: Campus) {
        googleMap?.let { map ->
            scope.launch(Dispatchers.Main) {
                val targetLocation = when (campus) {
                    Campus.SGW -> LatLng(45.4972, -73.5789)
                    Campus.LOYOLA -> LatLng(45.4582, -73.6402)
                }

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(targetLocation, CAMPUS_ZOOM_LEVEL),
                    CAMERA_ANIMATION_DURATION_MS,
                    null
                )

                suspend fun ensureOverlayAttached(c: Campus) {
                    val alreadyAttached = when (c) {
                        Campus.SGW -> sgwAttached
                        Campus.LOYOLA -> loyAttached
                    }
                    if (alreadyAttached) return

                    val json = withContext(Dispatchers.IO) {
                        loadGeoJson(
                            context,
                            when (c) {
                                Campus.SGW -> R.raw.sgw_buildings
                                Campus.LOYOLA -> R.raw.loy_buildings
                            }
                        )
                    }

                    when (c) {
                        Campus.SGW -> sgwOverlay?.attachToMapAsync(map, json)
                        Campus.LOYOLA -> loyOverlay?.attachToMapAsync(map, json)
                    }

                    when (c) {
                        Campus.SGW -> {
                            sgwAttached = true
                            sgwOverlay?.setAllStyles(defaultOverlayStyle)
                            sgwOverlay?.setMarkersVisible(false)
                        }
                        Campus.LOYOLA -> {
                            loyAttached = true
                            loyOverlay?.setAllStyles(defaultOverlayStyle)
                            loyOverlay?.setMarkersVisible(false)
                        }
                    }
                }

                ensureOverlayAttached(Campus.SGW)
                ensureOverlayAttached(Campus.LOYOLA)

                sgwOverlay?.setBuildingsVisible(true)
                loyOverlay?.setBuildingsVisible(true)
            }
        }
    }

    // Dispose location tracking
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let {
                fusedLocationProviderClient.removeLocationUpdates(it)
            }
            searchJob?.cancel()
        }
    }



    Box(modifier = Modifier
        .fillMaxSize()
        .semantics {
            stateDescription =
                if (selectedCampus == Campus.LOYOLA)
                    "Loyola map shown"
                else
                    "SGW map shown"
        }) {

        DisposableEffect(Unit) {
            onDispose {
                try {
                    googleMap?.clear()
                    mapView.onStop()
                    mapView.onDestroy()
                } catch (e: Exception) {
                    // mapView was never fully initialized
                }
            }
        }

        // Map View
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    getMapAsync { map ->
                        googleMap = map

                        // Initialize overlays
                        sgwOverlay = GeoJsonOverlay(ctx, idPropertyName = "buildingCode")
                        loyOverlay = GeoJsonOverlay(ctx, idPropertyName = "buildingCode")
                        map.uiSettings.isMapToolbarEnabled = false


                        // Move camera to saved campus
                        val savedCampus = getSavedCampus(ctx)
                        val initialLocation = when (savedCampus) {
                            Campus.SGW -> LatLng(45.4972, -73.5789)
                            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
                        }
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                initialLocation,
                                CAMPUS_ZOOM_LEVEL
                            )
                        )

                        // Show location if permission granted
                        if (ActivityCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            map.isMyLocationEnabled = true
                        }

                        // Remove default controls
                        map.uiSettings.isMyLocationButtonEnabled = false
                        map.uiSettings.isZoomControlsEnabled = false

                        // Add shuttle stop markers (US-3.1)
                        if (shuttleTracker.isOperational()) {
                            val shuttleIcon = MapMarkerFactory.create(ctx, "Shuttle")
                            shuttleTracker.getShuttleStops().forEach { stop ->
                                val marker = map.addMarker(
                                    AdvancedMarkerOptions()
                                        .position(stop.latLng)
                                        .icon(shuttleIcon)
                                        .anchor(0.5f, 1.0f) // tip of pinpoints to coordinate
                                        .contentDescription(stop.id + "1")
                                )
                                if (marker != null) {
                                    marker.tag = stop
                                    shuttleMarkerMap[stop.id] = marker
                                }
                            }
                        } else {
                            scope.launch {
                                snackBarHostState.showSnackbar("Shuttle stop data unavailable")
                            }
                        }

                        // Marker click: handle shuttle stop taps (US-3.1)
                        // GeoJsonOverlay uses polygon listeners, not marker listeners — safe to set here.
                        @Suppress("PotentialBehaviorOverride")
                        map.setOnMarkerClickListener { marker -> // NOSONAR
                            val stop = marker.tag as? ShuttleStop
                            if (stop != null) {
                                selectedShuttleStop = stop
                                true
                            } else {
                                false
                            }
                        }

                        // Set up polygon click listener
                        map.setOnPolygonClickListener { polygon ->
                            val overlayAndFeature = listOfNotNull(sgwOverlay, loyOverlay)
                                .firstNotNullOfOrNull { overlay ->
                                    overlay.getPolygonId(polygon)
                                        ?.let { featureId -> overlay to featureId }
                                } ?: return@setOnPolygonClickListener

                            val activeOverlay = overlayAndFeature.first
                            val featureId = overlayAndFeature.second
                            val props = activeOverlay.getBuildingProps()[featureId]
                                ?: return@setOnPolygonClickListener
                            val buildingInfo = BuildingInfo.fromJson(props)

                            // Calculate centroid for polygon click position
                            val latLng = polygon.points.let { points ->
                                val avgLat = points.map { it.latitude }.average()
                                val avgLng = points.map { it.longitude }.average()
                                LatLng(avgLat, avgLng)
                            }

                            when (val step = directionsUiState.step) {
                                is DirectionsStep.PlanRoute -> {
                                    if (isPickingOrigin) {
                                        directionsUiState = directionsUiState.copy(
                                            step = step.copy(origin = latLng),
                                            errorMessage = null
                                        )
                                        isPickingOrigin = false
                                    } else {
                                        selectedBuildingInfo = buildingInfo
                                        selectedBuildingLatLng = latLng
                                    }
                                }

                                is DirectionsStep.ShowingRoute -> {
                                    selectedBuildingInfo = null
                                    val tappedBuildingCode = buildingInfo?.buildingCode

                                    // Clear indoor directions state BEFORE opening new building
                                    // to prevent merge logic from resurrecting stale destination nodes
                                    indoorDirectionsState = null
                                    indoorBuildingCode = tappedBuildingCode

                                    mapTapFocusNodeTrigger = null
                                    mapTapSetStartNodeTrigger = null
                                    mapTapSetDestNodeTrigger = null

                                    if (isIndoorOutdoorFlow && tappedBuildingCode != null) {
                                        val isStartBuilding = tappedBuildingCode.equals(
                                            indoorFlowStartNode?.buildingCode,
                                            ignoreCase = true
                                        )
                                        val isEndBuilding = tappedBuildingCode.equals(
                                            indoorFlowEndNode?.buildingCode,
                                            ignoreCase = true
                                        )

                                        when {
                                            isStartBuilding && indoorFlowStartNode != null && indoorFlowStartAccessNode != null -> {
                                                mapTapSetStartNodeTrigger = indoorFlowStartNode
                                                mapTapSetDestNodeTrigger = indoorFlowStartAccessNode
                                            }

                                            isEndBuilding && indoorFlowEndAccessNode != null && indoorFlowEndNode != null -> {
                                                mapTapSetStartNodeTrigger = indoorFlowEndAccessNode
                                                mapTapSetDestNodeTrigger = indoorFlowEndNode
                                            }

                                            isStartBuilding && indoorFlowStartNode != null -> {
                                                mapTapFocusNodeTrigger = indoorFlowStartNode
                                            }

                                            isEndBuilding && indoorFlowEndNode != null -> {
                                                mapTapFocusNodeTrigger = indoorFlowEndNode
                                            }
                                        }

                                        indoorTriggerVersion++
                                    }
                                }

                                else -> {
                                    // PickDestination or ConfirmDestination: show bottom sheet
                                    selectedBuildingInfo = buildingInfo
                                    selectedBuildingLatLng = latLng
                                }
                            }
                        }

                        // Load active campus
                        scope.launch(Dispatchers.IO) {
                            val activeCampus = getSavedCampus(ctx)
                            val activeJson = loadGeoJson(
                                ctx,
                                when (activeCampus) {
                                    Campus.SGW -> R.raw.sgw_buildings
                                    Campus.LOYOLA -> R.raw.loy_buildings
                                }
                            )

                            when (activeCampus) {
                                Campus.SGW -> sgwOverlay?.attachToMapAsync(map, activeJson)
                                Campus.LOYOLA -> loyOverlay?.attachToMapAsync(map, activeJson)
                            }

                            withContext(Dispatchers.Main) {
                                when (activeCampus) {
                                    Campus.SGW -> sgwAttached = true
                                    Campus.LOYOLA -> loyAttached = true
                                }
                                initializeOverlays(
                                    activeCampus,
                                    sgwOverlay!!,
                                    loyOverlay!!,
                                    ctx,
                                    fusedLocationProviderClient,
                                    map,
                                    sgwOverlay,
                                    loyOverlay,
                                    userLocationViewModel

                                ) { callback ->
                                    locationCallback = callback
                                }
                            }
                        }

                        scheduleSearch(pendingSearchQuery)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("mapView"),
            update = { mapView ->
                mapView.onResume()
            }
        )


        // Campus Toggle + round search shortcut button (same row)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 10.dp)
                .ignoreFocusClearOnTouch(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onBottomSearchClick)
                    .semantics { contentDescription = "Bottom search button" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CampusToggle(
                selectedCampus = selectedCampus,
                onCampusSelected = { campus ->
                    selectedCampus = campus
                    saveCampus(context, campus)
                    switchCampus(campus)
                },
                showIcon = true
            )
        }

        // Map Controls
        if (controlsVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("mapControls")
                    .semantics { contentDescription = "Map Controls" }
                    .padding(end = 16.dp, bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { zoomIn(googleMap) },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_in_button),
                        contentDescription = "Zoom In",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { moveLeft(googleMap) },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.left_button),
                            contentDescription = "Left",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { moveUp(googleMap) },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.up_button),
                                contentDescription = "Up",
                                tint = Color.Unspecified,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        IconButton(
                            onClick = { recenter(googleMap, fusedLocationProviderClient, context) },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.recenter_button),
                                contentDescription = "Recenter",
                                tint = Color.Unspecified,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        IconButton(
                            onClick = { moveDown(googleMap) },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.down_button),
                                contentDescription = "Down",
                                tint = Color.Unspecified,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    IconButton(
                        onClick = { moveRight(googleMap) },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.right_button),
                            contentDescription = "Right",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                IconButton(
                    onClick = { zoomOut(googleMap) },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_out_button),
                        contentDescription = "Zoom Out",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = { viewModel.controlsVisible = !controlsVisible },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.panel_button),
                        contentDescription = "Toggle Controls",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            IconButton(
                onClick = { viewModel.controlsVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 60.dp)
                    .size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.panel_button),
                    contentDescription = "Toggle Controls",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Building Details Bottom Sheet
        selectedBuildingInfo?.let { info ->
            BuildingDetailsBottomSheet(
                buildingInfo = info,
                onDismiss = { selectedBuildingInfo = null },
                onDirectionsClick = {
                    isIndoorOutdoorFlow = false
                    indoorOutdoorFallbackParts = emptyList()
                    legLabels = emptyList()
                    legFallbackMessage = null
                    topBarOriginOverride = null
                    topBarDestinationOverride = null
                    val latLng = selectedBuildingLatLng ?: LatLng(45.4972, -73.5789)

                    // Find corresponding CampusBuilding for cross-campus detection
                    destinationBuilding = ALL_SUGGESTIONS.firstOrNull {
                        (it as? CampusBuilding)?.buildingCode == info.buildingCode
                    } as? CampusBuilding

                    val buildingHit = BuildingHit(
                        id = info.buildingCode,
                        properties = JSONObject().apply {
                            put("building-code", info.buildingCode)
                            put("building-name", info.buildingName)
                            put("address", info.address)
                        }
                    )
                    directionsUiState = directionsUiState.copy(
                        step = DirectionsStep.PlanRoute(
                            origin = defaultOrigin,
                            destination = latLng,
                            buildingHit = buildingHit
                        )
                    )
                    selectedBuildingInfo = null
                },
                onExploreIndoors = {
                    // Clear indoor directions state to prevent stale nodes from previous route
                    indoorDirectionsState = null
                    indoorBuildingCode = info.buildingCode
                    selectedBuildingInfo = null
                }
            )
        }

        // Shuttle stop info card (US-3.1)
        selectedShuttleStop?.let { stop ->
            ShuttleStopInfoCard(
                stop = stop,
                isOperational = shuttleTracker.isOperational(),
                onDismiss = { selectedShuttleStop = null },
                onDirectionsClick = {
                    val hit = BuildingHit(
                        id = stop.id,
                        properties = JSONObject().apply {
                            put("building-name", stop.name)
                        }
                    )
                    directionsUiState = directionsUiState.copy(
                        step = DirectionsStep.PlanRoute(
                            origin = defaultOrigin,
                            destination = stop.latLng,
                            buildingHit = hit
                        )
                    )
                    selectedShuttleStop = null
                }
            )
        }



        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )



        // Indoor map overlay (US-5.1 – US-5.6)
        indoorBuildingCode?.let { code ->
            val hasAnyMapTapPairTrigger =
                mapTapSetStartNodeTrigger != null || mapTapSetDestNodeTrigger != null
            val hasCompleteMapTapPairTrigger =
                mapTapSetStartNodeTrigger != null && mapTapSetDestNodeTrigger != null
            val hasIncompleteMapTapPairTrigger =
                hasAnyMapTapPairTrigger && !hasCompleteMapTapPairTrigger

            val effectiveFocusNode = mapTapFocusNodeTrigger ?: indoorSearchFocusNodeTrigger
            val effectiveSetStartNode = if (hasCompleteMapTapPairTrigger) {
                mapTapSetStartNodeTrigger
            } else if (hasIncompleteMapTapPairTrigger) {
                null
            } else {
                indoorSetStartTrigger
            }
            val effectiveSetDestNode = if (hasCompleteMapTapPairTrigger) {
                mapTapSetDestNodeTrigger
            } else if (hasIncompleteMapTapPairTrigger) {
                null
            } else {
                indoorSetDestTrigger
            }

            val consumeMapTapTriggers =
                mapTapFocusNodeTrigger != null ||
                        hasCompleteMapTapPairTrigger
            val consumeTopBarTriggers =
                (mapTapFocusNodeTrigger == null && indoorSearchFocusNodeTrigger != null) ||
                        (!hasCompleteMapTapPairTrigger &&
                                !hasIncompleteMapTapPairTrigger &&
                                (indoorSetStartTrigger != null || indoorSetDestTrigger != null))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                IndoorMapScreen(
                    buildingCode = code,
                    // Respond to top search actions while indoors are open
                    focusNode = effectiveFocusNode,
                    setStartNode = effectiveSetStartNode,
                    setDestNode = effectiveSetDestNode,
                    resetVersion = directionsCancelTrigger,
                    triggerVersion = indoorTriggerVersion,
                    hasExistingDestinationSelection = hasExistingDestinationSelection,
                    goTrigger = directionsGoTrigger,
                    clearTrigger = directionsCancelTrigger,
                    onDirectionsTopBarState = { state ->
                        if (!suppressIndoorStateUpdates) {
                            if (state.active) {
                                val previous = indoorDirectionsState
                                indoorDirectionsState = if (previous != null) {
                                    val mergedOriginNode = state.indoorOriginNode ?: previous.indoorOriginNode
                                    val mergedDestinationNode = state.indoorDestinationNode ?: previous.indoorDestinationNode
                                    state.copy(
                                        originLabel = if (state.indoorOriginNode == null && previous.indoorOriginNode != null) {
                                            previous.originLabel
                                        } else {
                                            state.originLabel
                                        },
                                        destinationLabel = if (state.indoorDestinationNode == null && previous.indoorDestinationNode != null) {
                                            previous.destinationLabel
                                        } else {
                                            state.destinationLabel
                                        },
                                        indoorOriginNode = mergedOriginNode,
                                        indoorDestinationNode = mergedDestinationNode,
                                        goEnabled = state.goEnabled || (mergedOriginNode != null && mergedDestinationNode != null),
                                        showTravelModes = false,
                                    )
                                } else {
                                    state.copy(showTravelModes = false)
                                }
                            } else if (indoorDirectionsState == null) {
                                indoorDirectionsState = state
                            }
                        }
                    },
                    onTopCardActiveChanged = { active ->
                        onIndoorTopCardActiveChanged(active)
                    },
                    onCrossBuildingRouteRequested = { request ->
                        onIndoorOutdoorRouteRequested(request)
                    },
                    onTriggersConsumed = {
                        if (consumeMapTapTriggers) {
                            mapTapFocusNodeTrigger = null
                            mapTapSetStartNodeTrigger = null
                            mapTapSetDestNodeTrigger = null
                        }
                        if (consumeTopBarTriggers) {
                            onIndoorTriggerConsumed()
                        }
                    },
                    onClose = {
                        indoorBuildingCode = null
                    }
                )
            }
        }
    }
}


