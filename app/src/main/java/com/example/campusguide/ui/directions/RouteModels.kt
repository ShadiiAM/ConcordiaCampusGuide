package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng

data class RouteRequest(
    val origin: LatLng,
    val destination: LatLng,
    val travelMode: String = "DRIVE",
)


data class RouteResult(
    val points: List<LatLng>,
    val distanceMeters: Int? = null,
    val durationSeconds: Int? = null,
)
