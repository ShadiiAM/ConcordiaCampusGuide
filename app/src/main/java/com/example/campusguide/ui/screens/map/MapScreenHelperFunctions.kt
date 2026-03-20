package com.example.campusguide.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.map.geoJson.GeoJsonOverlay
import com.example.campusguide.ui.map.geoJson.GeoJsonStyle
import com.example.campusguide.ui.map.utils.BuildingHit
import com.example.campusguide.ui.map.utils.BuildingLocator
import com.example.campusguide.ui.viewmodels.UserLocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "campus_preferences"
private const val KEY_SELECTED_CAMPUS = "selected_campus"
const val EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY"

// Helper Functions
internal fun getSavedCampus(context: Context): Campus {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedCampusName = prefs.getString(KEY_SELECTED_CAMPUS, Campus.SGW.name)
    return try {
        Campus.valueOf(savedCampusName ?: Campus.SGW.name)
    } catch (e: IllegalArgumentException) {
        Campus.SGW
    }
}

internal fun saveCampus(context: Context, campus: Campus) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit { putString(KEY_SELECTED_CAMPUS, campus.name) }
}

internal fun loadGeoJson(context: Context, rawRes: Int): JSONObject {
    val input = context.resources.openRawResource(rawRes)
    val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
    return JSONObject(text)
}

internal var defaultOverlayStyle = GeoJsonStyle(
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

internal fun initializeOverlays(
    campus: Campus,
    sgwOverlay: GeoJsonOverlay,
    loyOverlay: GeoJsonOverlay,
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    googleMap: GoogleMap?,
    sgwOverlayNullable: GeoJsonOverlay?,
    loyOverlayNullable: GeoJsonOverlay?,
    userLocationViewModel: UserLocationViewModel,
    setCallback: (LocationCallback) -> Unit,
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
        userLocationViewModel,
        setCallback
    )
}

internal fun startLocationTracking(
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    googleMap: GoogleMap?,
    sgwOverlay: GeoJsonOverlay?,
    loyOverlay: GeoJsonOverlay?,
    userLocationViewModel: UserLocationViewModel,
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
                    userLocationViewModel.onLocationUpdated(userLatLng)
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

internal fun highlightBuildingUserIsIn(
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

internal fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}


internal fun buildingTitle(
    hit: BuildingHit?,
    fallback: LatLng
): String {
    val props = hit?.properties
    val name = props
        ?.optString("building-name")
        ?.takeIf { it.isNotBlank() }

    return name ?: "Destination (${latLngShort(fallback)})"
}

internal fun latLngShort(p: LatLng): String =
    "%.5f, %.5f".format(p.latitude, p.longitude)