package com.example.campusguide.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.R
import com.example.campusguide.UsabilityTrackerIRLUsers
import com.example.campusguide.data.ALL_POI
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.OutsidePOI
import com.example.campusguide.data.POIFilterValues
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.MapBottomSearchBar
import com.example.campusguide.ui.components.MapControlsPanel
import com.example.campusguide.ui.components.POICard
import com.example.campusguide.ui.directions.DirectionsStep
import com.example.campusguide.ui.directions.DirectionsUiState
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.detectCampus
import com.example.campusguide.ui.directions.isCrossCampusRoute
import com.example.campusguide.ui.map.geoJson.MapMarkerFactory
import com.example.campusguide.ui.map.utils.BuildingHit
import com.example.campusguide.ui.screens.map.DirectionsTopBarState
import com.example.campusguide.ui.screens.map.DrawRouteResult
import com.example.campusguide.ui.screens.map.buildRouteSummary
import com.example.campusguide.ui.screens.map.buildingTitle
import com.example.campusguide.ui.screens.map.canUseShuttle
import com.example.campusguide.ui.screens.map.centerOnOrigin
import com.example.campusguide.ui.screens.map.drawRoute
import com.example.campusguide.ui.screens.map.getSavedCampus
import com.example.campusguide.ui.screens.map.hasLocationPermission
import com.example.campusguide.ui.screens.map.saveCampus
import com.example.campusguide.ui.shuttle.ShuttleSchedule
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import com.example.campusguide.ui.viewmodels.UserLocationViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.microsoft.clarity.Clarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.collections.mutableListOf

@Composable
fun POIScreen(
    topBarSelectedPOISuggestion: Suggestion? = null,
    onTopBarBuildingConsumed: () -> Unit = {},
    onBottomSearchClick: () -> Unit = {},
    onDirectionsTopBarState: (DirectionsTopBarState) -> Unit = {},
    directionsGoTrigger: Int = 0,
    directionsCancelTrigger: Int = 0,
    topBarTravelMode: TravelMode = TravelMode.DRIVE,
    viewModel: ControlsViewModel = viewModel<ControlsViewModel>(),
    poiFilters: POIFilterValues = POIFilterValues()
) {

    val firebaseAnalytics = Firebase.analytics

    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, "POIScreen")
        param(FirebaseAnalytics.Param.SCREEN_CLASS, "ScreenPOIActivity")
    }
    UsabilityTrackerIRLUsers.userInteractionRecord("POIScreen")
    LaunchedEffect(Unit) {
        Clarity.setCurrentScreenName("POIScreen")
    }

    val cameraAnimationDuration = 1500
    val campusLevelZoom = 15f



    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accessibilityState = LocalAccessibilityState.current

    val userLocationViewModel: UserLocationViewModel = viewModel()

    // State management
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    var selectedCampus by rememberSaveable { mutableStateOf(getSavedCampus(context)) }
    var searchMarker by remember { mutableStateOf<Marker?>(null) }
    var controlsVisible = viewModel.controlsVisible

    val snackBarHostState = remember { SnackbarHostState() }

    val repo = remember { GoogleRoutesRepository() }
    var directionsUiState by remember { mutableStateOf(DirectionsUiState()) }
    var travelMode by rememberSaveable { mutableStateOf(TravelMode.DRIVE) }
    val routePolylines = remember { mutableListOf<Polyline?>() }
    var routeRequestGeneration by remember { mutableIntStateOf(0) }

    val defaultOrigin by userLocationViewModel.effectiveOrigin.collectAsState()

    // Autocomplete state (cross-campus always enabled per US-2.5 AC4)
    var originDisplayName by remember { mutableStateOf<String?>(null) }

    // Track origin and destination buildings for cross-campus detection
    var originBuilding by remember { mutableStateOf<CampusBuilding?>(null) }
    var destinationBuilding by remember { mutableStateOf<Suggestion?>(null) }
    var legLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var legFallbackMessage by remember { mutableStateOf<String?>(null) }
    var topBarOriginOverride by remember { mutableStateOf<String?>(null) }
    var topBarDestinationOverride by remember { mutableStateOf<String?>(null) }
    var isIndoorOutdoorFlow by remember { mutableStateOf(false) }
    var indoorOutdoorFallbackParts by remember { mutableStateOf<List<String>>(emptyList()) }

    // Reserved for US-3.2: enables removing/updating markers when switching campuses
    val poiMarkerMap = remember { mutableMapOf<String, Marker>() }
    var selectedPOI by remember { mutableStateOf<OutsidePOI?>(null) }


    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    var routePolylineRef by remember {
        mutableStateOf<Polyline?>(null)
    }

    val poiMapView = remember { MapView(context) }


    // Get user location for default origin
    LaunchedEffect(Unit) {
        if (!hasLocationPermission(context)) return@LaunchedEffect
        userLocationViewModel.fetchUserLocation()
    }


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

        val step = directionsUiState.step as? DirectionsStep.PlanRoute ?: return@LaunchedEffect

        routePolylines.forEach { it?.remove() }
        routePolylines.clear()

        val requestGeneration = routeRequestGeneration

        directionsUiState = directionsUiState.copy(isLoadingRoute = true, errorMessage = null)
        var drawRouteResult = DrawRouteResult(emptyList(), "Failed to load route")

        centerOnOrigin(googleMap, step.origin, context)

        val isCrossCampus = isCrossCampusRoute(originBuilding, destinationBuilding, step.origin)

        val departure = canUseShuttle(step.origin, step.destination, travelMode)

        drawRouteResult = drawRoute(
            step,
            step.origin,
            step.destination,
            if (departure != null) "SHUTTLE" else travelMode.name,
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


        routeRequestGeneration++
        routePolylineRef?.remove()
        routePolylineRef = null


        legLabels = emptyList()

        directionsUiState = directionsUiState.copy(
            step = DirectionsStep.PickDestination,
            isLoadingRoute = false,
            errorMessage = null,
        )

        searchMarker?.remove()
        searchMarker = null

    }

    LaunchedEffect(topBarSelectedPOISuggestion) {

        val stop = topBarSelectedPOISuggestion
        if(stop != null && stop is OutsidePOI) {
            val latLng = stop.latLng

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

            // Set as To destination and open route panel
            destinationBuilding = stop  // Track for cross-campus detection
            val hit = BuildingHit(
                id = "poi" + stop.name,
                properties = JSONObject().apply {
                    put("poi-name", stop.name)
                    put("poi-type", stop.category)
                    put("address", stop.address)
                }
            )
            directionsUiState = directionsUiState.copy(
                step = DirectionsStep.PlanRoute(
                    origin = defaultOrigin,
                    destination = latLng,
                    buildingHit = hit,
                )
            )

            onTopBarBuildingConsumed()
            topBarDestinationOverride = stop.name
        }
    }

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
    ) {

        when (val step = directionsUiState.step) {
            is DirectionsStep.PlanRoute, is DirectionsStep.ShowingRoute -> {
                val origin = if (step is DirectionsStep.PlanRoute) step.origin else (step as DirectionsStep.ShowingRoute).origin
                val destination = if (step is DirectionsStep.PlanRoute) step.destination else (step as DirectionsStep.ShowingRoute).destination
                val buildingHit = if (step is DirectionsStep.PlanRoute) step.buildingHit else (step as DirectionsStep.ShowingRoute).buildingHit

                val isCrossCampus = isCrossCampusRoute(originBuilding, destinationBuilding, origin)
                val canUseShuttle = canUseShuttle(origin, destination, travelMode) != null
                val shuttleStatus = ShuttleSchedule.nextDeparture(detectCampus(destination))

                val baseState = DirectionsTopBarState(
                    active = true,
                    originLabel = topBarOriginOverride ?: (originDisplayName ?: "Your location"),
                    destinationLabel = topBarDestinationOverride ?: buildingTitle(buildingHit, destination),
                    isCrossCampus = isCrossCampus,
                    selectedMode = travelMode,
                    legLabels = legLabels,
                    legFallbackMessage = legFallbackMessage,
                    canUseShuttle = canUseShuttle,
                    shuttleStatus = shuttleStatus,
                    goEnabled = true,
                    showTravelModes = true,
                    goLabel = "Go",
                    cancelLabel = "Cancel",
                )

                onDirectionsTopBarState(
                    when (step) {
                        is DirectionsStep.PlanRoute -> baseState.copy(
                            showActions = true,
                            errorMessage = directionsUiState.errorMessage,
                            isLoadingRoute = directionsUiState.isLoadingRoute,
                            )
                        is DirectionsStep.ShowingRoute -> baseState.copy(
                            showActions = false,
                            routeSummary = buildRouteSummary(step.route.distanceMeters, step.route.durationSeconds),
                            route = step.route,
                            currentSteps = step.route.legs.firstOrNull(),
                        )
                    }
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


    // Location services
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }


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
                        poiMapView.onStop()
                        poiMapView.onDestroy()
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

                            map.uiSettings.isMapToolbarEnabled = false
                            map.uiSettings.isMyLocationButtonEnabled = false
                            map.uiSettings.isZoomControlsEnabled = false

                            val savedCampus = getSavedCampus(ctx)
                            val initialLocation = when (savedCampus) {
                                Campus.SGW -> LatLng(45.4972, -73.5789)
                                Campus.LOYOLA -> LatLng(45.4582, -73.6402)
                            }


                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    initialLocation,
                                    campusLevelZoom
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


                            // Add POI markers and filter (EPIC 6)
                            addPOIMarkersToMap(map, ctx, defaultOrigin, poiFilters, poiMarkerMap)

                            // Marker click: handle shuttle stop taps (US-3.1)
                            // GeoJsonOverlay uses polygon listeners, not marker listeners — safe to set here.
                            @Suppress("PotentialBehaviorOverride")
                            map.setOnMarkerClickListener { marker -> // NOSONAR
                                val poi = marker.tag as? OutsidePOI
                                if (poi != null) {
                                    selectedPOI = poi
                                    true
                                } else {
                                    false
                                }
                            }


                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("POIMapView"),
                update = { mapView ->
                    mapView.onResume()
                }
            )


            LaunchedEffect(poiFilters) {
                val map = googleMap ?: return@LaunchedEffect
                map.clear()
                // Add POI markers and filter (EPIC 6)
                addPOIMarkersToMap(map, context, defaultOrigin, poiFilters, poiMarkerMap)
            }

            selectedPOI?.let { poi ->
                POICard(
                    poi = poi,
                    onDismiss = { selectedPOI = null },
                    onDirectionsClick = {
                        val hit = BuildingHit(
                            id = poi.name,
                            properties = JSONObject().apply {
                                put("target-name", poi.name)
                            }
                        )
                        directionsUiState = directionsUiState.copy(
                            step = DirectionsStep.PlanRoute(
                                origin = defaultOrigin,
                                destination = poi.latLng,
                                buildingHit = hit
                            )
                        )
                        selectedPOI = null
                        topBarDestinationOverride = poi.name
                    }
                )
            }



        fun switchCampus(campus: Campus) {
            googleMap?.let { map ->
                scope.launch(Dispatchers.Main) {
                    val targetLocation = when (campus) {
                        Campus.SGW -> LatLng(45.4972, -73.5789)
                        Campus.LOYOLA -> LatLng(45.4582, -73.6402)
                    }

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(targetLocation, campusLevelZoom),
                        cameraAnimationDuration,
                        null
                    )
                }
            }
        }



        // Campus Toggle + round search shortcut button (same row)
        MapBottomSearchBar(
            selectedCampus = selectedCampus,
            onCampusSelected = { campus ->
                selectedCampus = campus
                saveCampus(context, campus)
                switchCampus(campus)
            },
            onBottomSearchClick = onBottomSearchClick,
        )

        MapControlsPanel(
            googleMap = googleMap,
            fusedLocationProviderClient = fusedLocationProviderClient,
            controlsVisible = controlsVisible,
            onToggleControls = { viewModel.controlsVisible = !controlsVisible },
        )
    }

}

private fun addPOIMarkersToMap(
    map: GoogleMap,
    context: android.content.Context,
    defaultOrigin: com.google.android.gms.maps.model.LatLng,
    poiFilters: POIFilterValues,
    poiMarkerMap: MutableMap<String, com.google.android.gms.maps.model.Marker>,
) {
    ALL_POI.forEach { poi ->
        if (poi.filterPOI(defaultOrigin, poiFilters)) {
            val poiIcon = MapMarkerFactory.create(context, poi.category.toString())
            val marker = map.addMarker(
                AdvancedMarkerOptions()
                    .position(poi.latLng)
                    .icon(poiIcon)
                    .anchor(0.5f, 1.0f) // tip of pinpoints to coordinate
                    .contentDescription(poi.name + " POI")
            )
            if (marker != null) {
                marker.tag = poi
                poiMarkerMap[poi.name] = marker
            }
        }
    }
}