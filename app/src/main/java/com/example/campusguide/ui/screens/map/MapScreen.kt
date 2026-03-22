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
import com.example.campusguide.ui.directions.RouteRequest
import com.example.campusguide.ui.map.utils.BuildingHit
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.Locale
import kotlin.coroutines.resume
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.isCrossCampusRoute
import com.example.campusguide.ui.directions.getCrossCampusMessage
import com.example.campusguide.ui.directions.getCrossCampusErrorMessage
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.ShuttleStopInfoCard
import com.example.campusguide.ui.map.geoJson.ShuttleMarkerFactory
import com.example.campusguide.ui.shuttle.ShuttleTracker
import com.example.campusguide.ui.viewmodels.ControlsViewModel
import androidx.core.content.ContextCompat
import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.components.ignoreFocusClearOnTouch
import com.example.campusguide.ui.directions.RouteResult
import com.example.campusguide.ui.viewmodels.UserLocationViewModel
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Polyline

private const val CAMERA_ANIMATION_DURATION_MS = 1500
private const val CAMPUS_ZOOM_LEVEL = 15f

data class DirectionsTopBarState(
    val active: Boolean,
    val originLabel: String = "Your location",
    val destinationLabel: String = "",
    val isCrossCampus: Boolean = false,
    val selectedMode: TravelMode = TravelMode.DRIVE,
    val routeSummary: String? = null,
    val errorMessage: String? = null,
    val isLoadingRoute: Boolean = false,
    val showActions: Boolean = false,
    val route: RouteResult = RouteResult(points = emptyList()),
    val isPickingOrigin: Boolean = false,
    val canUseShuttle: Boolean = false
)
@Composable
fun MapScreen(
    searchQuery: String = "",
    topBarSelectedSuggestion: Suggestion? = null,
    onTopBarBuildingConsumed: () -> Unit = {},
    onBottomSearchClick: () -> Unit = {},
    onDirectionsTopBarState: (DirectionsTopBarState) -> Unit = {},
    directionsGoTrigger: Int = 0,
    directionsCancelTrigger: Int = 0,
    originPickTrigger: Int = 0,
    myLocationTrigger: Int = 0,
    topBarTravelMode: TravelMode = TravelMode.DRIVE,
    viewModel: ControlsViewModel = viewModel<ControlsViewModel>()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
    lateinit var polylineFormatting: PolylineOptions

    val snackBarHostState = remember { SnackbarHostState() }

    val repo = remember { GoogleRoutesRepository() }
    var directionsUiState by remember { mutableStateOf(DirectionsUiState()) }
    var travelMode by rememberSaveable { mutableStateOf(TravelMode.DRIVE) }
    var isPickingOrigin by remember { mutableStateOf(false) }
    val routePolylines = remember { mutableListOf<Polyline?>() }

    val defaultOrigin by userLocationViewModel.effectiveOrigin.collectAsState()
    // Track the selected building's LatLng for directions
    var selectedBuildingLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Autocomplete state (cross-campus always enabled per US-2.5 AC4)
    var originDisplayName  by remember { mutableStateOf<String?>(null) }

    val mapView = remember { MapView(context) }
    // Track origin and destination buildings for cross-campus detection
    var originBuilding by remember { mutableStateOf<CampusBuilding?>(null) }
    var destinationBuilding by remember { mutableStateOf<Suggestion?>(null) }

    // Shuttle state (US-3.1)
    val shuttleTracker = remember { ShuttleTracker() }
    // Reserved for US-3.2: enables removing/updating markers when switching campuses
    val shuttleMarkerMap = remember { mutableMapOf<String, Marker>() }
    var selectedShuttleStop by remember { mutableStateOf<ShuttleStop?>(null) }

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
    fun buildRouteSummary(distanceMeters: Int?, durationSeconds: Int?): String {
        val dist = distanceMeters?.let {
            if (it >= 1000) "${"%.1f".format(it / 1000.0)} km" else "$it m"
        }
        val dur = durationSeconds?.let {
            val mins = it / 60
            if (mins < 60) "$mins min" else "${mins / 60} h ${mins % 60} min"
        }
        return listOfNotNull(dur, dist).joinToString(" · ")
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

        val step = directionsUiState.step as? DirectionsStep.PlanRoute ?: return@LaunchedEffect
        directionsUiState = directionsUiState.copy(isLoadingRoute = true, errorMessage = null)

        routePolylines.forEach { it?.remove() }
        routePolylines.clear()
        if(canUseShuttle(step.origin, step.destination, travelMode)){
            val route = getShuttleRoute(step.origin, step.destination)

            route.legs.forEach { leg ->
                leg.steps.forEach { step ->
                    when (step.travelMode) {
                        TravelMode.TRANSIT -> {
                            polylineFormatting = PolylineOptions()
                                .addAll(step.polyline)
                                .color(0xFFE53935.toInt())
                                .width(18f)
                        }

                        else -> {
                            polylineFormatting = PolylineOptions()
                                .addAll(step.polyline)
                                .color(0xFF1565C0.toInt())
                                .width(14f)
                                .pattern(listOf(Dash(20f), Gap(10f)))
                        }
                    }
                    val polyline = googleMap?.addPolyline(polylineFormatting)
                    routePolylines.add(polyline)
                }
            }
            directionsUiState = directionsUiState.copy(
                isLoadingRoute = false,
                step = DirectionsStep.ShowingRoute(
                    origin = step.origin,
                    destination = step.destination,
                    buildingHit = step.buildingHit,
                    route = route,
                ),
            )
        }
        else {
            runCatching {
                repo.getRoute(
                    RouteRequest(
                        origin = step.origin,
                        destination = step.destination,
                        mode = travelMode,
                    )
                )
            }.onSuccess { route ->

                android.util.Log.d("Route", route.toString())
                recenter(googleMap, fusedLocationProviderClient, context)
                when (travelMode) {
                    TravelMode.DRIVE -> {
                        polylineFormatting = PolylineOptions()
                            .addAll(route.points)
                            .color(0xFF1565C0.toInt())
                            .width(18f)

                        val polyline = googleMap?.addPolyline(polylineFormatting)
                        routePolylines.add(polyline)
                    }

                    TravelMode.WALK -> {
                        polylineFormatting = PolylineOptions()
                            .addAll(route.points)
                            .color(0xFF1565C0.toInt())
                            .width(14f)
                            .pattern(listOf(Dash(20f), Gap(10f)))

                        val polyline = googleMap?.addPolyline(polylineFormatting)
                        routePolylines.add(polyline)
                    }

                    TravelMode.TRANSIT -> {
                        route.legs.forEach { leg ->
                            leg.steps.forEach { step ->
                                when (step.travelMode) {
                                    TravelMode.TRANSIT -> {
                                        polylineFormatting = PolylineOptions()
                                            .addAll(step.polyline)
                                            .color(0xFF7B1FA2.toInt())
                                            .width(18f)
                                    }

                                    else -> {
                                        polylineFormatting = PolylineOptions()
                                            .addAll(step.polyline)
                                            .color(0xFF1565C0.toInt())
                                            .width(14f)
                                            .pattern(listOf(Dash(20f), Gap(10f)))
                                    }
                                }
                                val polyline = googleMap?.addPolyline(polylineFormatting)
                                routePolylines.add(polyline)
                            }
                        }
                    }
                }


                // Show helpful message for cross-campus routes
                val isCrossCampus = isCrossCampusRoute(
                    originBuilding,
                    destinationBuilding, step.origin
                )
                if (isCrossCampus) {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = getCrossCampusMessage(travelMode),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                directionsUiState = directionsUiState.copy(
                    isLoadingRoute = false,
                    step = DirectionsStep.ShowingRoute(
                        origin = step.origin,
                        destination = step.destination,
                        buildingHit = step.buildingHit,
                        route = route,
                    ),
                )
            }.onFailure { e ->
                // Check if this is a cross-campus route and provide helpful error message
                val isCrossCampus = isCrossCampusRoute(
                    originBuilding,
                    destinationBuilding, step.origin
                )
                val errorMsg = if (isCrossCampus) {
                    getCrossCampusErrorMessage(travelMode)
                } else {
                    e.message ?: "Failed to get route"
                }

                directionsUiState = directionsUiState.copy(
                    isLoadingRoute = false,
                    errorMessage = errorMsg,
                )
            }
        }
    }

// Handle Cancel from top bar
    LaunchedEffect(directionsCancelTrigger) {
        if (directionsCancelTrigger == 0) return@LaunchedEffect
        routePolylines.forEach { it?.remove() }
        routePolylines.clear()
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
    LaunchedEffect(directionsUiState, travelMode, originBuilding, destinationBuilding, originDisplayName, isPickingOrigin) {
        when (val step = directionsUiState.step) {
            is DirectionsStep.PlanRoute -> {
                // Automatically detect cross-campus routes
                val isCrossCampus = isCrossCampusRoute(originBuilding,
                    destinationBuilding, step.origin)

                val canUseShuttle = canUseShuttle(step.origin, step.destination, travelMode)
                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = originDisplayName ?: "Your location",
                        destinationLabel = buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = isCrossCampus,
                        selectedMode = travelMode,
                        errorMessage = directionsUiState.errorMessage,
                        isLoadingRoute = directionsUiState.isLoadingRoute,
                        showActions = true,
                        isPickingOrigin = isPickingOrigin,
                        canUseShuttle = canUseShuttle
                    )
                )
            }
            is DirectionsStep.ShowingRoute -> {
                // Automatically detect cross-campus routes
                val isCrossCampus = isCrossCampusRoute(originBuilding,
                    destinationBuilding, step.origin)
                val canUseShuttle = canUseShuttle(step.origin, step.destination, travelMode)

                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = originDisplayName ?: "Your location",
                        destinationLabel = buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = isCrossCampus,
                        selectedMode = travelMode,
                        routeSummary = buildRouteSummary(step.route.distanceMeters, step.route.durationSeconds),
                        showActions = false,
                        route = step.route,
                        canUseShuttle = canUseShuttle
                    )
                )
            }
            else -> {
                onDirectionsTopBarState(DirectionsTopBarState(active = false))
            }
        }
    }

    LaunchedEffect(topBarSelectedSuggestion) {

        when (topBarSelectedSuggestion) {
            is CampusBuilding -> {
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

    // Permission handling
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
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

                when (campus) {
                    Campus.SGW -> loyOverlay?.setBuildingsVisible(false)
                    Campus.LOYOLA -> sgwOverlay?.setBuildingsVisible(false)
                }

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(targetLocation, CAMPUS_ZOOM_LEVEL),
                    CAMERA_ANIMATION_DURATION_MS,
                    null
                )

                val targetAttached = when (campus) {
                    Campus.SGW -> sgwAttached
                    Campus.LOYOLA -> loyAttached
                }

                if (targetAttached) {
                    when (campus) {
                        Campus.SGW -> sgwOverlay?.setBuildingsVisible(true)
                        Campus.LOYOLA -> loyOverlay?.setBuildingsVisible(true)
                    }
                } else {
                    launch(Dispatchers.IO) {
                        val json = loadGeoJson(
                            context,
                            when (campus) {
                                Campus.SGW -> R.raw.sgw_buildings
                                Campus.LOYOLA -> R.raw.loy_buildings
                            }
                        )

                        when (campus) {
                            Campus.SGW -> sgwOverlay?.attachToMapAsync(map, json)
                            Campus.LOYOLA -> loyOverlay?.attachToMapAsync(map, json)
                        }

                        withContext(Dispatchers.Main) {
                            when (campus) {
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
                            val current = getSavedCampus(context)
                            sgwOverlay?.setBuildingsVisible(current == Campus.SGW)
                            loyOverlay?.setBuildingsVisible(current == Campus.LOYOLA)
                        }
                    }
                }
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
                googleMap?.clear()
                mapView.onStop()
                mapView.onDestroy()
            }
        }

        // Map View
        AndroidView(
            factory = { ctx ->
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        googleMap = map

                        // Initialize overlays
                        sgwOverlay = GeoJsonOverlay(ctx, idPropertyName = "buildingCode")
                        loyOverlay = GeoJsonOverlay(ctx, idPropertyName = "buildingCode")



                        // Move camera to saved campus
                        val savedCampus = getSavedCampus(ctx)
                        val initialLocation = when (savedCampus) {
                            Campus.SGW -> LatLng(45.4972, -73.5789)
                            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
                        }
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, CAMPUS_ZOOM_LEVEL))

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
                            val shuttleIcon = ShuttleMarkerFactory.create(ctx)
                            shuttleTracker.getShuttleStops().forEach { stop ->
                                val marker = map.addMarker(
                                    MarkerOptions()
                                        .position(stop.latLng)
                                        .icon(shuttleIcon)
                                        .anchor(0.5f, 1.0f) // tip of pinpoints to coordinate
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
                        map.setOnMarkerClickListener { marker -> // NO SONAR
                            val stop = marker.tag as? ShuttleStop
                            if (stop != null) {
                                if (isPickingOrigin) {
                                    val step = directionsUiState.step as? DirectionsStep.PlanRoute
                                    if (step != null) {
                                        directionsUiState = directionsUiState.copy(
                                            step = step.copy(origin = stop.latLng),
                                            errorMessage = null
                                        )
                                        originDisplayName = stop.name
                                        originBuilding = null
                                        isPickingOrigin = false
                                    }
                                } else {
                                    selectedShuttleStop = stop
                                }
                                true
                            } else {
                                false
                            }
                        }

                        // Set up polygon click listener
                        map.setOnPolygonClickListener { polygon ->
                            val currentCampus = getSavedCampus(ctx)
                            val activeOverlay = when (currentCampus) {
                                Campus.SGW -> sgwOverlay
                                Campus.LOYOLA -> loyOverlay
                            }

                            val featureId = activeOverlay?.getPolygonId(polygon) ?: return@setOnPolygonClickListener
                            val props = activeOverlay.getBuildingProps()[featureId] ?: return@setOnPolygonClickListener
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
                                        originDisplayName = buildingInfo?.buildingName ?: buildingInfo?.buildingCode
                                        originBuilding = null
                                        isPickingOrigin = false
                                    }
                                }
                                is DirectionsStep.ShowingRoute -> {
                                    // ignore taps while showing route
                                }
                                else -> {
                                    // PickDestination or ConfirmDestination: show bottom sheet
                                    selectedBuildingInfo = buildingInfo
                                    selectedBuildingLatLng = latLng
                                }
                            }
                        }

                        // General map tap: pick origin when in picking mode (non-polygon areas)
                        map.setOnMapClickListener { latLng ->
                            if (isPickingOrigin) {
                                val step = directionsUiState.step as? DirectionsStep.PlanRoute ?: return@setOnMapClickListener
                                directionsUiState = directionsUiState.copy(
                                    step = step.copy(origin = latLng),
                                    errorMessage = null
                                )
                                originDisplayName = latLngShort(latLng)
                                originBuilding = null
                                isPickingOrigin = false
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
    }
}



