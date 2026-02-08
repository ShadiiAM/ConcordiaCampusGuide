package com.example.campusguide.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import com.example.campusguide.R
import com.example.campusguide.databinding.ActivityMapsBinding
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.map.geoJson.GeoJsonOverlay
import com.example.campusguide.ui.map.geoJson.GeoJsonStyle
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class MapsActivity() : AppCompatActivity(), OnMapReadyCallback {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var callback: LocationCallback

    private lateinit var sgwOverlay: GeoJsonOverlay
    private lateinit var loyOverlay: GeoJsonOverlay
    private var sgwAttached = false
    private var loyAttached = false
    var userMarker: Marker? = null



    // Coroutine scope for background operations
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request location permissions after layout is inflated
        onGPS()

        // Hide the ActionBar
        supportActionBar?.hide()

        // Set up the top search bar
        binding.searchBar.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ConcordiaCampusGuideTheme {
                    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
                    SearchBarWithProfile(
                        onSearchQueryChange = { /* TODO: Handle search */ },
                        onProfileClick = { showProfileOverlay() },
                        modifier = Modifier.padding(top = statusBarPadding.calculateTopPadding())
                    )
                }
            }
        }

        // Set up profile/accessibility overlay
        binding.profileOverlay.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        // Set up the campus toggle
        binding.campusToggle.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ConcordiaCampusGuideTheme {
                    var selectedCampus by remember { mutableStateOf(getSavedCampus()) }
                    MaterialTheme {
                        CampusToggle(
                            selectedCampus = selectedCampus,
                            onCampusSelected = { campus ->
                                selectedCampus = campus
                                saveCampus(campus)
                                switchCampus(campus)
                            },
                            showIcon = true
                        )
                    }
                }
            }
        }

        // Set up the bottom navigation
        binding.bottomNav.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                            label = { Text("Map") },
                            selected = true,
                            onClick = { }
                        )
                        NavigationBarItem(
                            icon = { Icon(painterResource(R.drawable.ic_directions), contentDescription = "Directions") },
                            label = { Text("Directions") },
                            selected = false,
                            onClick = { }
                        )
                        NavigationBarItem(
                            icon = { Icon(painterResource(R.drawable.ic_calendar), contentDescription = "Calendar") },
                            label = { Text("Calendar") },
                            selected = false,
                            onClick = { }
                        )
                        NavigationBarItem(
                            icon = { Icon(painterResource(R.drawable.ic_poi), contentDescription = "POI") },
                            label = { Text("POI") },
                            selected = false,
                            onClick = { }
                        )
                    }
                }
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * Quick operations (marker, camera) stay synchronous.
     * GeoJSON file I/O + parsing runs on IO; map mutations switch back to Main.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Construct overlay objects immediately so switchCampus can reference them
        // even before attachToMap populates them.
        sgwOverlay = GeoJsonOverlay(this, idPropertyName = "building-name")
        loyOverlay = GeoJsonOverlay(this, idPropertyName = "building-name")



        // Add a marker at Concordia University (SGW Campus) and move the camera
        val concordiaSGW = LatLng(45.4972, -73.5789)
        mMap.addMarker(MarkerOptions()
            .position(concordiaSGW)
            .title("Concordia University - SGW Campus"))

        // Move camera to saved campus location
        val savedCampus = getSavedCampus()
        val initialLocation = when (savedCampus) {
            Campus.SGW -> LatLng(45.4972, -73.5789)
            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, CAMPUS_ZOOM_LEVEL))

        sgwOverlay.attachToMap(mMap)
        loyOverlay.attachToMap(mMap)
         val defaultStyle = GeoJsonStyle(
             fillColor = 0x80ff8a8a.toInt(),
             strokeColor = 0xFF4d0000.toInt(),
             strokeWidth = 2f,
             zIndex = 10f,
             clickable = false,
             visible = true,
             markerColor = 0xFF974949.toInt(),
             markerAlpha = 1f,
             markerScale = 2f
         )
        sgwOverlay.setAllStyles(defaultStyle)
        loyOverlay.setAllStyles(defaultStyle)
        // Show the blue "My Location" dot if permission is already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        // Remove default Google Maps controls from bottom-right
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false

        // Only load the active campus on startup — halves the main-thread work.
        // The inactive campus loads on-demand the first time the user taps the toggle.
        activityScope.launch(Dispatchers.IO) {
            val activeCampus = getSavedCampus()
            val activeJson = loadGeoJson(when (activeCampus) {
                Campus.SGW    -> R.raw.sgw_buildings
                Campus.LOYOLA -> R.raw.loy_buildings
            })

            // Coordinate parsing stays on IO; only addPolygon/addMarker hit Main
            when (activeCampus) {
                Campus.SGW    -> sgwOverlay.attachToMapAsync(mMap, activeJson)
                Campus.LOYOLA -> loyOverlay.attachToMapAsync(mMap, activeJson)
            }

            withContext(Dispatchers.Main) {
                when (activeCampus) {
                    Campus.SGW    -> sgwAttached = true
                    Campus.LOYOLA -> loyAttached = true
                }
                initializeOverlays(getSavedCampus())
            }
        }
    }

    /** Read and parse a raw GeoJSON resource. Safe to call on any thread. */
    private fun loadGeoJson(rawRes: Int): JSONObject {
        val input = resources.openRawResource(rawRes)
        val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
        return JSONObject(text)
    }

    /**
     * Apply default styles and show/hide overlays based on selected campus,
     * then start tracking the user's location (if permission is granted).
     */
    internal fun initializeOverlays(campus: Campus) {
        sgwOverlay.setAllStyles(defaultOverlayStyle())
        loyOverlay.setAllStyles(defaultOverlayStyle())

        when (campus) {
            Campus.SGW -> {
                sgwOverlay.setVisibleAll(true)
                loyOverlay.setVisibleAll(false)
            }
            Campus.LOYOLA -> {
                loyOverlay.setVisibleAll(true)
                sgwOverlay.setVisibleAll(false)
            }
        }

        startLocationTracking()
    }

    private fun defaultOverlayStyle() = GeoJsonStyle(
        fillColor    = 0x80ffaca6.toInt(),
        strokeColor  = 0xFFbc4949.toInt(),
        strokeWidth  = 2f,
        zIndex       = 10f,
        clickable    = true,
        visible      = true,
        markerColor  = 0xFFbc4949.toInt(),
        markerAlpha  = 1f,
        markerScale  = 1.5f
    )

    // ==================== Campus switching ====================

    internal fun switchCampus(campus: Campus) {
        if (!::mMap.isInitialized) return
        activityScope.launch(Dispatchers.Main) {
            executeSwitchCampus(campus)
        }
    }

    /**
     * Show/hide campus overlays and animate the camera to the target campus.
     * If the target campus has never been loaded, its overlay is fetched in the
     * background while the camera animates — keeps the main thread responsive.
     */
    internal fun executeSwitchCampus(campus: Campus) {
        if (!::mMap.isInitialized) return

        val targetLocation = when (campus) {
            Campus.SGW    -> LatLng(45.4972, -73.5789)
            Campus.LOYOLA -> LatLng(45.4582, -73.6402)
        }

        // Hide the other campus immediately so stale buildings vanish
        when (campus) {
            Campus.SGW    -> loyOverlay.setVisibleAll(false)
            Campus.LOYOLA -> sgwOverlay.setVisibleAll(false)
        }

        // Animate camera right away — does not block on overlay loading
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(targetLocation, CAMPUS_ZOOM_LEVEL),
            CAMERA_ANIMATION_DURATION_MS,
            object : GoogleMap.CancelableCallback {
                override fun onFinish() { }
                override fun onCancel() { }
            }
        )

        val targetAttached = when (campus) {
            Campus.SGW    -> sgwAttached
            Campus.LOYOLA -> loyAttached
        }

        if (targetAttached) {
            // Already loaded — just show it
            when (campus) {
                Campus.SGW    -> sgwOverlay.setVisibleAll(true)
                Campus.LOYOLA -> loyOverlay.setVisibleAll(true)
            }
        } else {
            // First visit to this campus — load its overlay in the background
            activityScope.launch(Dispatchers.IO) {
                val json = loadGeoJson(when (campus) {
                    Campus.SGW    -> R.raw.sgw_buildings
                    Campus.LOYOLA -> R.raw.loy_buildings
                })

                // Pre-parse coordinates on IO; addPolygon/addMarker on Main
                when (campus) {
                    Campus.SGW    -> sgwOverlay.attachToMapAsync(mMap, json)
                    Campus.LOYOLA -> loyOverlay.attachToMapAsync(mMap, json)
                }

                withContext(Dispatchers.Main) {
                    when (campus) {
                        Campus.SGW    -> sgwAttached = true
                        Campus.LOYOLA -> loyAttached = true
                    }
                    when (campus) {
                        Campus.SGW    -> sgwOverlay.setAllStyles(defaultOverlayStyle())
                        Campus.LOYOLA -> loyOverlay.setAllStyles(defaultOverlayStyle())
                    }
                    // Race-safe: user may have switched back while we were loading
                    val current = getSavedCampus()
                    sgwOverlay.setVisibleAll(current == Campus.SGW)
                    loyOverlay.setVisibleAll(current == Campus.LOYOLA)
                }
            }
        }
    }

    internal fun showProfileOverlay() {
        binding.profileOverlay.visibility = View.VISIBLE
        binding.profileOverlay.setContent {
            ProfileOverlayContent(onDismiss = {
                binding.profileOverlay.visibility = View.GONE
            })
        }
    }

    internal fun getSavedCampus(): Campus {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCampusName = prefs.getString(KEY_SELECTED_CAMPUS, Campus.SGW.name)
        return try {
            Campus.valueOf(savedCampusName ?: Campus.SGW.name)
        } catch (e: IllegalArgumentException) {
            Campus.SGW
        }
    }

    internal fun saveCampus(campus: Campus) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SELECTED_CAMPUS, campus.name)
            .apply()
    }

    // ==================== Location services ====================

    fun onGPS() {
        if (!isLocationEnabled()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 200)
        }
    }

    /** Start location updates only if both permissions are actually granted at runtime. */
    internal fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callback = generateCallback()
            requestLocationUpdates(callback)
        }
    }

    /** Called by the system after the user responds to a permission dialog. */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            if (::mMap.isInitialized) {
                // Double-check permission before enabling location
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    startLocationTracking()
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun requestLocationUpdates(callback: LocationCallback) {
        val requestLocation = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10)
        ).setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()
        fusedLocationProviderClient.requestLocationUpdates(
            requestLocation, callback, Looper.getMainLooper()
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun requestLocation(): LatLng {
        var userLocation = LatLng(45.4972, -73.5789)
        if (isPermissionsGranted()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? -> userLocation = setLocation(location) }
            return userLocation
        } else {
            throw Exception("Location permissions not granted")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun setLocation(location: Location?): LatLng {
        if (location != null) {
            return LatLng(location.latitude, location.longitude)
        } else {
            callback = object : LocationCallback() {}
            requestLocationUpdates(callback)
            return requestLocation()
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun isPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    fun generateCallback(): LocationCallback {
        return object : LocationCallback() {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    if (userMarker == null) {
                        userMarker = mMap.addMarker(
                            MarkerOptions().position(userLatLng).title("You are here")
                        )
                    } else {
                        userMarker?.position = userLatLng
                    }
                }
            }
        }
    }

    // ==================== Lifecycle ====================
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines to prevent memory leaks
        activityScope.coroutineContext[Job]?.cancel()
    }

    companion object {
        private const val PREFS_NAME = "campus_preferences"
        private const val KEY_SELECTED_CAMPUS = "selected_campus"
        private const val CAMERA_ANIMATION_DURATION_MS = 1500
        private const val CAMPUS_ZOOM_LEVEL = 15f
    }
}

@Composable
internal fun ProfileOverlayContent(onDismiss: () -> Unit) {
    ConcordiaCampusGuideTheme {
        var showProfile by remember { mutableStateOf(true) }
        var showAccessibility by remember { mutableStateOf(false) }
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

        if (!showProfile && !showAccessibility) {
            onDismiss()
        }

        if (showAccessibility) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = statusBarPadding.calculateTopPadding())
            ) {
                AccessibilityScreen(
                    onBackClick = {
                        showAccessibility = false
                        onDismiss()
                    }
                )
            }
        } else if (showProfile) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = statusBarPadding.calculateTopPadding())
            ) {
                ProfileScreen(
                    onBackClick = {
                        showProfile = false
                        onDismiss()
                    },
                    onProfileClick = { /* TODO: Navigate to profile details */ },
                    onAccessibilityClick = { showAccessibility = true }
                )
            }
        }
    }
}
