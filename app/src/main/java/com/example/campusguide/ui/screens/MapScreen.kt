package com.example.campusguide.ui.screens

import android.Manifest
import android.content.Context
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
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.campusguide.R
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.BuildingDetailsBottomSheet
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import com.example.campusguide.ui.map.geoJson.GeoJsonOverlay
import com.example.campusguide.ui.map.geoJson.GeoJsonStyle
import com.example.campusguide.ui.map.models.BuildingInfo
import com.example.campusguide.ui.directions.DirectionsStep
import com.example.campusguide.ui.directions.DirectionsUiState
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.directions.RouteRequest
import com.example.campusguide.ui.map.utils.BuildingHit
import com.example.campusguide.ui.map.utils.BuildingLocator
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import androidx.compose.ui.text.font.FontWeight
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.buildingSuggestions
import com.example.campusguide.ui.components.BuildingAutocompleteField
import com.example.campusguide.ui.directions.TravelMode

private const val PREFS_NAME = "campus_preferences"
private const val KEY_SELECTED_CAMPUS = "selected_campus"
private const val CAMERA_ANIMATION_DURATION_MS = 1500
private const val CAMPUS_ZOOM_LEVEL = 15f
const val EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY"

data class DirectionsTopBarState(
    val active: Boolean,
    val originLabel: String = "Your location",
    val destinationLabel: String = "",
    val isCrossCampus: Boolean = false,
    val selectedMode: TravelMode = TravelMode.DRIVE,
    val routeSummary: String? = null,
    val isLoadingRoute: Boolean = false,
    val showActions: Boolean = false,
)
@Composable
fun MapScreen(
    searchQuery: String = "",
    topBarSelectedBuilding: CampusBuilding? = null,
    onTopBarBuildingConsumed: () -> Unit = {},
    onBottomSearchClick: () -> Unit = {},
    onDirectionsTopBarState: (DirectionsTopBarState) -> Unit = {},
    directionsGoTrigger: Int = 0,
    directionsCancelTrigger: Int = 0,
    topBarTravelMode: TravelMode = TravelMode.DRIVE,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accessibilityState = LocalAccessibilityState.current

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
    var showProfile by remember { mutableStateOf(false) }
    var showAccessibility by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }

    val repo = remember { GoogleRoutesRepository() }
    var directionsUiState by remember { mutableStateOf(DirectionsUiState()) }
    var travelMode by rememberSaveable { mutableStateOf(TravelMode.DRIVE) }
    var isPickingOrigin by remember { mutableStateOf(false) }
    var routePolylineRef by remember {
        mutableStateOf<com.google.android.gms.maps.model.Polyline?>(null)
    }
    var defaultOrigin by remember { mutableStateOf(LatLng(45.4972, -73.5789)) }
    // Track the selected building's LatLng for directions
    var selectedBuildingLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Autocomplete state
    var crossCampusEnabled by rememberSaveable { mutableStateOf(false) }
    var originSuggestions  by remember { mutableStateOf<List<CampusBuilding>>(emptyList()) }
    var destSuggestions    by remember { mutableStateOf<List<CampusBuilding>>(emptyList()) }
    var originDisplayName  by remember { mutableStateOf<String?>(null) }



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
        runCatching {
            repo.getRoute(
                RouteRequest(
                    origin = step.origin,
                    destination = step.destination,
                    mode = travelMode,
                )
            )
        }.onSuccess { route ->
            routePolylineRef?.remove()
            routePolylineRef = googleMap?.addPolyline(
                PolylineOptions()
                    .addAll(route.points)
                    .color(0xFF1565C0.toInt())
                    .width(12f)
            )
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
            directionsUiState = directionsUiState.copy(
                isLoadingRoute = false,
                errorMessage = e.message ?: "Failed to get route",
            )
        }
    }

// Handle Cancel from top bar
    LaunchedEffect(directionsCancelTrigger) {
        if (directionsCancelTrigger == 0) return@LaunchedEffect
        routePolylineRef?.remove()
        routePolylineRef = null
        directionsUiState = directionsUiState.copy(
            step = DirectionsStep.PickDestination,
            errorMessage = null,
        )
        originSuggestions = emptyList()
        destSuggestions = emptyList()
    }

// Publish top-bar state to MainActivity whenever directions state changes
    LaunchedEffect(directionsUiState, travelMode, crossCampusEnabled, originDisplayName) {
        when (val step = directionsUiState.step) {
            is DirectionsStep.PlanRoute -> {
                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = originDisplayName ?: "Your location",
                        destinationLabel = buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = crossCampusEnabled,
                        selectedMode = travelMode,
                        isLoadingRoute = directionsUiState.isLoadingRoute,
                        showActions = true,
                    )
                )
            }
            is DirectionsStep.ShowingRoute -> {
                onDirectionsTopBarState(
                    DirectionsTopBarState(
                        active = true,
                        originLabel = originDisplayName ?: "Your location",
                        destinationLabel = buildingTitle(step.buildingHit, step.destination),
                        isCrossCampus = crossCampusEnabled,
                        selectedMode = travelMode,
                        routeSummary = buildRouteSummary(step.route.distanceMeters, step.route.durationSeconds),
                        showActions = false,
                    )
                )
            }
            else -> {
                onDirectionsTopBarState(DirectionsTopBarState(active = false))
            }
        }
    }

    LaunchedEffect(topBarSelectedBuilding) {
        val building = topBarSelectedBuilding ?: return@LaunchedEffect
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
        onTopBarBuildingConsumed()
    }

    // Get user location for default origin
    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return@LaunchedEffect

        val fused = LocationServices.getFusedLocationProviderClient(context)
        runCatching {
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        defaultOrigin = LatLng(loc.latitude, loc.longitude)
                    }
                }
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Could not retrieve your location")
            }
        }
    }

    // Location services
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

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
                        loyOverlay
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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    // Map controls
    fun moveUp() {
        googleMap?.animateCamera(CameraUpdateFactory.scrollBy(0f, -200f))
    }

    fun moveDown() {
        googleMap?.animateCamera(CameraUpdateFactory.scrollBy(0f, 200f))
    }

    fun moveLeft() {
        googleMap?.animateCamera(CameraUpdateFactory.scrollBy(-200f, 0f))
    }

    fun moveRight() {
        googleMap?.animateCamera(CameraUpdateFactory.scrollBy(200f, 0f))
    }

    fun zoomIn() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    fun recenter() {
        googleMap?.let { map ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                                    loyOverlay
                                ) { callback ->
                                    locationCallback = callback
                                }
                            }
                        }

                        scheduleSearch(pendingSearchQuery)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.onResume()
            }
        )


        // Campus Toggle + round search shortcut button (same row)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 10.dp),
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
                    .padding(end = 16.dp, bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { zoomIn() },
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
                        onClick = { moveLeft() },
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
                            onClick = { moveUp() },
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
                            onClick = { recenter() },
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
                            onClick = { moveDown() },
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
                        onClick = { moveRight() },
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
                    onClick = { zoomOut() },
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
                    onClick = { controlsVisible = !controlsVisible },
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
                onClick = { controlsVisible = true },
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


        // Profile/Accessibility Overlay
        if (showProfile || showAccessibility) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {
                if (showAccessibility) {
                    AccessibilityScreen(
                        onBackClick = {
                            showAccessibility = false
                        }
                    )
                } else if (showProfile) {
                    ProfileScreen(
                        onBackClick = { showProfile = false },
                        onProfileClick = { },
                        onAccessibilityClick = { showAccessibility = true }
                    )
                }
            }
        }

        // Building Details Bottom Sheet
        selectedBuildingInfo?.let { info ->
            BuildingDetailsBottomSheet(
                buildingInfo = info,
                onDismiss = { selectedBuildingInfo = null },
                onDirectionsClick = {
                    val latLng = selectedBuildingLatLng ?: LatLng(45.4972, -73.5789)
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Directions overlay
        when (val step = directionsUiState.step) {
            is DirectionsStep.PickDestination -> {
                // Normal map mode, nothing extra
            }
            is DirectionsStep.ConfirmDestination -> {
                // Shouldn't normally reach here since we go straight to PlanRoute
            }
            is DirectionsStep.PlanRoute -> {
                BottomCard(onDismiss = {
                    routePolylineRef?.remove()
                    routePolylineRef = null
                    directionsUiState = directionsUiState.copy(
                        step = DirectionsStep.PickDestination,
                        errorMessage = null,
                    )
                    originSuggestions = emptyList()
                    destSuggestions   = emptyList()
                }) {
                    Text(
                        text = "Route options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(12.dp))

                    BuildingAutocompleteField(
                        label         = "From:",
                        value = originDisplayName ?: latLngShort(step.origin),                        suggestions   = originSuggestions,
                        placeholder   = "My location or building…",
                        enabled       = !directionsUiState.isLoadingRoute,
                        onQueryChange = { query ->
                            originSuggestions = buildingSuggestions(
                                query        = query,
                                activeCampus = selectedCampus,
                                crossCampus  = crossCampusEnabled,
                            )
                        },
                        onSelected = { building ->
                            originSuggestions = emptyList()
                            originDisplayName = building.buildingName
                            directionsUiState = directionsUiState.copy(
                                step = step.copy(origin = resolveBuildingLatLng(building)),
                            )
                        },
                    )

                    Spacer(Modifier.height(8.dp))

                    BuildingAutocompleteField(
                        label         = "To:",
                        value         = buildingTitle(step.buildingHit, step.destination),
                        suggestions   = destSuggestions,
                        placeholder   = "Building name or code…",
                        enabled       = !directionsUiState.isLoadingRoute,
                        onQueryChange = { query ->
                            destSuggestions = buildingSuggestions(
                                query        = query,
                                activeCampus = selectedCampus,
                                crossCampus  = crossCampusEnabled,
                            )
                        },
                        onSelected = { building ->
                            val latLng = resolveBuildingLatLng(building)
                            destSuggestions = emptyList()
                            val hit = BuildingHit(
                                id = building.buildingCode,
                                properties = JSONObject().apply {
                                    put("building-code", building.buildingCode)
                                    put("building-name", building.buildingName)
                                    put("address",       building.address)
                                },
                            )
                            directionsUiState = directionsUiState.copy(
                                step = step.copy(
                                    destination = latLng,
                                    buildingHit = hit,
                                ),
                            )
                        },
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = "Cross-campus routing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked         = crossCampusEnabled,
                            onCheckedChange = {
                                crossCampusEnabled = it
                                originSuggestions  = emptyList()
                                destSuggestions    = emptyList()
                            },
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = !directionsUiState.isLoadingRoute,
                        onClick  = {
                            directionsUiState = directionsUiState.copy(
                                isLoadingRoute = true,
                                errorMessage   = null,
                            )
                            scope.launch {
                                runCatching {
                                    repo.getRoute(
                                        RouteRequest(
                                            origin      = step.origin,
                                            destination = step.destination,
                                            mode  = travelMode,
                                        )
                                    )
                                }.onSuccess { route ->
                                    val map = googleMap
                                    if (map != null) {
                                        routePolylineRef?.remove()
                                        routePolylineRef = map.addPolyline(
                                            PolylineOptions()
                                                .addAll(route.points)
                                                .color(0xFF1565C0.toInt())
                                                .width(12f)
                                        )
                                    }
                                    directionsUiState = directionsUiState.copy(
                                        isLoadingRoute = false,
                                        step = DirectionsStep.ShowingRoute(
                                            origin      = step.origin,
                                            destination = step.destination,
                                            buildingHit = step.buildingHit,
                                            route       = route,
                                        ),
                                    )
                                }.onFailure { e ->
                                    directionsUiState = directionsUiState.copy(
                                        isLoadingRoute = false,
                                        errorMessage   = e.message ?: "Failed to get route",
                                    )
                                }
                            }
                        },
                    ) {
                        Text(if (directionsUiState.isLoadingRoute) "Loading…" else "Go")
                    }

                    directionsUiState.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            is DirectionsStep.ShowingRoute -> {
                BottomCard(onDismiss = {
                    routePolylineRef?.remove()
                    routePolylineRef = null
                    directionsUiState = directionsUiState.copy(
                        step = DirectionsStep.PickDestination,
                        errorMessage = null
                    )
                }) {
                    Text(
                        text = "Directions ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("From: ${originDisplayName ?: latLngShort(step.origin)}")
                    Text("To: ${buildingTitle(step.buildingHit, step.destination)}")

                    // Display duration and distance if available
                    step.route.durationSeconds?.let { seconds ->
                        val minutes = seconds / 60
                        val displayTime = if (minutes < 60) {
                            "$minutes min"
                        } else {
                            val hours = minutes / 60
                            val remainingMins = minutes % 60
                            "${hours}h ${remainingMins}min"
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Duration: $displayTime",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    step.route.distanceMeters?.let { meters ->
                        val displayDistance = if (meters < 1000) {
                            "$meters m"
                        } else {
                            val km = meters / 1000.0
                            String.format(java.util.Locale.US, "%.1f km", km)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Distance: $displayDistance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            directionsUiState = directionsUiState.copy(
                                step = DirectionsStep.PlanRoute(
                                    origin = step.origin,
                                    destination = step.destination,
                                    buildingHit = step.buildingHit
                                )
                            )
                        }
                    ) {
                        Text("Edit")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            routePolylineRef?.remove()
                            routePolylineRef = null
                            directionsUiState = directionsUiState.copy(
                                step = DirectionsStep.PickDestination,
                                errorMessage = null
                            )
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomCard(
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (onDismiss != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close directions"
                            )
                        }
                    }
                }
                content()
            }
        }
    }
}

private fun buildingTitle(
    hit: BuildingHit?,
    fallback: LatLng
): String {
    val props = hit?.properties
    val name = props
        ?.optString("building-name")
        ?.takeIf { it.isNotBlank() }

    return name ?: "Destination (${latLngShort(fallback)})"
}

private fun latLngShort(p: LatLng): String =
    "%.5f, %.5f".format(p.latitude, p.longitude)

// Helper Functions
private fun getSavedCampus(context: Context): Campus {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedCampusName = prefs.getString(KEY_SELECTED_CAMPUS, Campus.SGW.name)
    return try {
        Campus.valueOf(savedCampusName ?: Campus.SGW.name)
    } catch (e: IllegalArgumentException) {
        Campus.SGW
    }
}

private fun saveCampus(context: Context, campus: Campus) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
        .putString(KEY_SELECTED_CAMPUS, campus.name)
        .apply()
}

private fun loadGeoJson(context: Context, rawRes: Int): JSONObject {
    val input = context.resources.openRawResource(rawRes)
    val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
    return JSONObject(text)
}

private var defaultOverlayStyle = GeoJsonStyle(
    fillColor = 0x80ffaca6.toInt(),
    strokeColor = 0xFFbc4949.toInt(),
    strokeWidth = 2f,
    zIndex = 10f,
    clickable = true,
    markerColor = 0xFFbc4949.toInt(),
    markerAlpha = 1f,
    markerScale = 1.5f
)

private var highlightedOverlayStyle = GeoJsonStyle(
    fillColor = 0xF0ffacaf.toInt(),
    strokeColor = 0xFFbc4949.toInt(),
    strokeWidth = 9f,
    zIndex = 10f,
    clickable = true,
    markerColor = 0xFFbc4949.toInt(),
    markerAlpha = 1f,
    markerScale = 1.5f
)

private fun initializeOverlays(
    campus: Campus,
    sgwOverlay: GeoJsonOverlay,
    loyOverlay: GeoJsonOverlay,
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    googleMap: GoogleMap?,
    sgwOverlayNullable: GeoJsonOverlay?,
    loyOverlayNullable: GeoJsonOverlay?,
    setCallback: (LocationCallback) -> Unit
) {
    sgwOverlay.setAllStyles(defaultOverlayStyle)
    loyOverlay.setAllStyles(defaultOverlayStyle)



    when (campus) {
        Campus.SGW -> {
            sgwOverlay.setBuildingsVisible(true)
            loyOverlay.setBuildingsVisible(false)
        }
        Campus.LOYOLA -> {
            loyOverlay.setBuildingsVisible(true)
            sgwOverlay.setBuildingsVisible(false)
        }
    }

    sgwOverlay.setMarkersVisible(false)
    loyOverlay.setMarkersVisible(false)

    startLocationTracking(
        context,
        fusedLocationProviderClient,
        googleMap,
        sgwOverlayNullable,
        loyOverlayNullable,
        setCallback
    )
}

private fun startLocationTracking(
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    googleMap: GoogleMap?,
    sgwOverlay: GeoJsonOverlay?,
    loyOverlay: GeoJsonOverlay?,
    setCallback: (LocationCallback) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    highlightBuildingUserIsIn(userLatLng, sgwOverlay, loyOverlay)
                }
            }
        }
        setCallback(callback)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10)
        ).setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
    }
}

private fun highlightBuildingUserIsIn(
    latLng: LatLng,
    sgwOverlay: GeoJsonOverlay?,
    loyOverlay: GeoJsonOverlay?
) {
    sgwOverlay?.let { sgw ->
        loyOverlay?.let { loy ->
            val sgwBuildingLocator = BuildingLocator(
                sgw.getBuildings(),
                sgw.getBuildingProps()
            )
            val loyBuildingLocator = BuildingLocator(
                loy.getBuildings(),
                loy.getBuildingProps()
            )

            val sgwIsHit = sgwBuildingLocator.pointInBuilding(latLng)
            val loyIsHit = loyBuildingLocator.pointInBuilding(latLng)

            sgw.setAllStyles(defaultOverlayStyle)
            loy.setAllStyles(defaultOverlayStyle)

            if (sgwIsHit) {
                val building = sgwBuildingLocator.findBuilding(latLng)
                building?.let {
                    sgw.setStyleForFeature(it.id, highlightedOverlayStyle)
                }
            }
            if (loyIsHit) {
                val building = loyBuildingLocator.findBuilding(latLng)
                building?.let {
                    loy.setStyleForFeature(it.id, highlightedOverlayStyle)
                }
            }
        }
    }

}

private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}