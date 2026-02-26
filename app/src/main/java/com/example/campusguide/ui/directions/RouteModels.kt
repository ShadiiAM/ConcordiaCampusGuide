package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng

data class RouteRequest(
    val origin: LatLng,
    val destination: LatLng,
    val mode: TravelMode = TravelMode.WALKING,
)

enum class TravelMode {
    WALKING,
}

data class RouteResult(
    val points: List<LatLng>,
)
