package com.example.campusguide

import android.content.pm.PackageManager
import android.Manifest
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
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
import java.util.concurrent.TimeUnit



class MapsActivity() : AppCompatActivity(), OnMapReadyCallback {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var callback: LocationCallback

    private lateinit var sgwOverlay: GeoJsonOverlay
    private lateinit var loyOverlay: GeoJsonOverlay
    var userMarker: Marker? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        onGPS()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        sgwOverlay = GeoJsonOverlay(this, R.raw.sgw_buildings, "building-name")
        loyOverlay = GeoJsonOverlay(this, R.raw.loy_buildings, "building-name")



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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))


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

        //Add a marker at User Position

        callback = generateCallback()

        requestLocationUpdates(callback)

    }


    //Turn on location and request permissions
    fun onGPS(){
        if(!isLocationEnabled()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),200)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),200)
        }
    }

    //Start the location updates
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun requestLocationUpdates(callback: LocationCallback){
        val requestLocation = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10)
            ).setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()
        fusedLocationProviderClient.requestLocationUpdates(
            requestLocation, callback , Looper.getMainLooper()
        )
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
     fun requestLocation() : LatLng{
        var userLocation = LatLng(45.4972, -73.5789)
        if(isPermissionsGranted()){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener{ location: Location? -> userLocation = setLocation(location)}
            return userLocation
        }
        else{
            throw Exception("Location permissions not granted")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun setLocation(location: Location?) : LatLng{
        if(location != null){
            return LatLng(location.latitude,location.longitude)
        }else{
            callback = object: LocationCallback(){}
            requestLocationUpdates(callback)
            return requestLocation()
        }
    }


    //Check if the location and network services are on
    fun isLocationEnabled() : Boolean{
        val locationManager= applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun isPermissionsGranted(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    fun generateCallback(): LocationCallback{
        return object: LocationCallback() {
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

    private fun switchCampus(campus: Campus) {
        if (!::mMap.isInitialized) return

        when (campus) {
            Campus.SGW -> {
                val concordiaSGW = LatLng(45.4972, -73.5789)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(concordiaSGW, 15f))
            }
            Campus.LOYOLA -> {
                val concordiaLoyola = LatLng(45.4582, -73.6402)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(concordiaLoyola, 15f))
            }
        }
    }

    private fun showProfileOverlay() {
        binding.profileOverlay.visibility = View.VISIBLE
        binding.profileOverlay.setContent {
            ConcordiaCampusGuideTheme {
                var showProfile by remember { mutableStateOf(true) }
                var showAccessibility by remember { mutableStateOf(false) }
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

                if (!showProfile && !showAccessibility) {
                    binding.profileOverlay.visibility = View.GONE
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
                                binding.profileOverlay.visibility = View.GONE
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
                                binding.profileOverlay.visibility = View.GONE
                            },
                            onProfileClick = { /* TODO: Navigate to profile details */ },
                            onAccessibilityClick = { showAccessibility = true }
                        )
                    }
                }
            }
        }
    }

    private fun getSavedCampus(): Campus {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCampusName = prefs.getString(KEY_SELECTED_CAMPUS, Campus.SGW.name)
        return try {
            Campus.valueOf(savedCampusName ?: Campus.SGW.name)
        } catch (e: IllegalArgumentException) {
            Campus.SGW
        }
    }

    private fun saveCampus(campus: Campus) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SELECTED_CAMPUS, campus.name)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "campus_preferences"
        private const val KEY_SELECTED_CAMPUS = "selected_campus"
    }
}