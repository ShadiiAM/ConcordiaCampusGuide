package com.example.campusguide.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

// Map controls
fun moveUp(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.scrollBy(0f, -200f))
}

fun moveDown(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.scrollBy(0f, 200f))
}

fun moveLeft(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.scrollBy(-200f, 0f))
}

fun moveRight(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.scrollBy(200f, 0f))
}

fun zoomIn(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
}

fun zoomOut(googleMap: GoogleMap?) {
    googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
}

fun recenter(googleMap: GoogleMap?, fusedLocationProviderClient: FusedLocationProviderClient, context: Context) {
    googleMap?.let { map ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }
        }
    }
}

fun centerOnOrigin(googleMap: GoogleMap?, origin: LatLng, context: Context) {
    googleMap?.let { map ->
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f))
    }


}