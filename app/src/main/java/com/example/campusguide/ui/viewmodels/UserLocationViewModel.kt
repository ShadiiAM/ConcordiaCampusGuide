package com.example.campusguide.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.ALL_POI
import com.example.campusguide.data.find
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.example.campusguide.ui.shuttle.ShuttleTracker
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Tracks the user's real-time GPS location and derives nearest shuttle stop / POI.
 *
 * Location updates arrive via [FusedLocationProviderClient] every 5 seconds when
 * ACCESS_FINE_LOCATION permission is granted. The ViewModel stops updates in [onCleared]
 * to avoid leaking the callback.
 */
class UserLocationViewModel(application: Application) : AndroidViewModel(application) {

    private val _userLatLng = MutableStateFlow<LatLng?>(null)
    val userLatLng: StateFlow<LatLng?> = _userLatLng

    // SGW campus center used as the fallback when location is unavailable
    private val _defaultOrigin = MutableStateFlow(LatLng(45.4953, -73.5788))

    /** Real location when available, default SGW origin otherwise. */
    val effectiveOrigin: StateFlow<LatLng> = userLatLng
        .map { it ?: _defaultOrigin.value }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _defaultOrigin.value)

    private val _nearestShuttleId = MutableStateFlow<String?>(null)
    val nearestId: StateFlow<String?> = _nearestShuttleId

    private val _nearestPOIName = MutableStateFlow<String?>(null)
    val nearestPOIName: StateFlow<String?> = _nearestPOIName

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    // Receives location batches from the OS and updates the flow
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _userLatLng.value = LatLng(it.latitude, it.longitude)
            }
        }
    }

    init {
        fetchUserLocation()
        // Recompute nearest stop and POI whenever the location changes
        viewModelScope.launch {
            userLatLng.collect { latLng ->
                updateNearestId(latLng)
                updateNearestPOI(latLng)
            }
        }
    }

    /**
     * Starts continuous location updates if the fine location permission is granted.
     * Safe to call multiple times; the OS deduplicates duplicate requests.
     */
    fun fetchUserLocation() {
        val context = getApplication<Application>()
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop updates to prevent the callback from outliving the ViewModel
        fusedClient.removeLocationUpdates(locationCallback)
    }

    /** Finds the closest shuttle stop to [userLatLng] and stores its ID. */
    fun updateNearestId(userLatLng: LatLng?) {
        val allShuttleStop = ShuttleTracker().getShuttleStops()
        _nearestShuttleId.value = userLatLng?.let {
            NearestShuttleStopFinder.find(it, allShuttleStop)?.stop?.id
        }
    }

    /** Finds the closest POI to [userLatLng] and stores its display name. */
    fun updateNearestPOI(userLatLng: LatLng?) {
        _nearestPOIName.value = userLatLng?.let {
            find(it, ALL_POI)
        }
    }

    /** Called when the map provides a location update (e.g. from the blue dot). */
    fun onLocationUpdated(latLng: LatLng) {
        _userLatLng.value = latLng
    }
}
