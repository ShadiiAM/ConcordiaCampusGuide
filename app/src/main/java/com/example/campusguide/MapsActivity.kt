package com.example.campusguide

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
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
import com.example.campusguide.databinding.ActivityMapsBinding
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.map.GeoJsonOverlay
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val sgwBuildingsOverlay = GeoJsonOverlay(R.raw.sgw_buildings)
    private val loyBuildingsOverlay = GeoJsonOverlay(R.raw.loy_buildings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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

        //Adds The Overlay
        sgwBuildingsOverlay.addToMap(mMap, this)
        loyBuildingsOverlay.addToMap(mMap, this)

        sgwBuildingsOverlay.changeAllBuildingColors("#ffaca6")
        sgwBuildingsOverlay.changeAllPointColors("#bc4949")
        loyBuildingsOverlay.changeAllBuildingColors("#ffaca6")
        loyBuildingsOverlay.changeAllPointColors("#bc4949")

        sgwBuildingsOverlay.removeAllPoints()
    }

    private fun switchCampus(campus: Campus) {
        if (!::mMap.isInitialized) return

        val (targetLocation, campusName) = when (campus) {
            Campus.SGW -> Pair(LatLng(45.4972, -73.5789), "SGW Campus")
            Campus.LOYOLA -> Pair(LatLng(45.4582, -73.6402), "Loyola Campus")
        }

        // Animate camera with controlled duration and callback
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(targetLocation, CAMPUS_ZOOM_LEVEL),
            CAMERA_ANIMATION_DURATION_MS,
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    // Animation completed successfully - map is interactive
                }

                override fun onCancel() {
                    // Animation was cancelled (e.g., user interacted with map)
                    // Map remains interactive
                }
            }
        )
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
        private const val CAMERA_ANIMATION_DURATION_MS = 1500
        private const val CAMPUS_ZOOM_LEVEL = 15f
    }

}