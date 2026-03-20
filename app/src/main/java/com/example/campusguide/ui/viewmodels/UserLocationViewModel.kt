package com.example.campusguide.ui.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserLocationViewModel : ViewModel() {
    private val _userLatLng = MutableStateFlow<LatLng?>(null)
    val userLatLng: StateFlow<LatLng?> = _userLatLng

    private val _nearestShuttleId = MutableStateFlow<String?>(null)
    val nearestId: StateFlow<String?> = _nearestShuttleId

    init {
        viewModelScope.launch {
            userLatLng.collect { latLng ->
                updateNearestId(latLng)
            }
        }
    }

    fun updateNearestId(userLatLng: LatLng?) {

        val allShuttleStop = ALL_SUGGESTIONS.map { it }.filterIsInstance<ShuttleStop>()

        _nearestShuttleId.value = userLatLng?.let {
            NearestShuttleStopFinder.find(it, allShuttleStop)?.stop?.id
        }
    }
    fun fetchUserLocation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {

            val fineGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted) {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                fused.lastLocation.addOnSuccessListener { loc ->
                    _userLatLng.value = loc?.let { LatLng(it.latitude, it.longitude) }
                }
            }
        }
    }

    fun onLocationUpdated(latLng: LatLng) {
        _userLatLng.value = latLng
    }

}
