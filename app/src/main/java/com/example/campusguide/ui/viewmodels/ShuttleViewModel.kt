package com.example.campusguide.ui.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.shuttle.ShuttleTracker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class ShuttleViewModel : ViewModel() {

    var shuttleStops by mutableStateOf<List<ShuttleStop>>(emptyList())
        private set

    var shuttleUserLatLng by mutableStateOf<LatLng?>(null)
        private set

    var shuttleShowBothStops by mutableStateOf(false)
        private set

    fun handleSearchQuery(query: String, context: Context): Boolean {
        val trimmed = query.trim().lowercase()

        if (trimmed.length >= 2 && "shuttle".startsWith(trimmed)) {
            val tracker = ShuttleTracker()
            shuttleStops = tracker.getShuttleStops()
            fetchUserLocation(context)
            return true
        }

        clearShuttleState()
        return false
    }

    fun isShuttleQuery(query: String): Boolean {
        val trimmed = query.trim().lowercase()
        return trimmed.length >= 2 && "shuttle".startsWith(trimmed)
    }

    fun onShuttleStopSelected() {
        shuttleStops = emptyList()
        shuttleShowBothStops = true
    }

    fun consumeShowBothStops() {
        shuttleShowBothStops = false
    }

    private fun clearShuttleState() {
        shuttleStops = emptyList()
        shuttleUserLatLng = null
    }

    private fun fetchUserLocation(context: Context) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                shuttleUserLatLng = loc?.let { LatLng(it.latitude, it.longitude) }
            }
        }
    }
}