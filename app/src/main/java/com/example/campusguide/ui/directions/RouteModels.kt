package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng

data class RouteRequest(
    val origin: LatLng,
    val destination: LatLng,
    val mode: TravelMode = TravelMode.DRIVE,
) {
    val travelMode: String
        get() = mode.apiValue
}

data class RouteResult(
    val points: List<LatLng>,
    val durationSeconds: Int? = null,
    val distanceMeters: Int? = null,
    val legs: List<RouteLeg> = emptyList(),
    val isShuttleRoute: Boolean = false
)

data class RouteLeg(
    val durationSeconds: Int? = null,
    val distanceMeters: Int? = null,
    val steps: List<RouteStep> = emptyList(),
)

data class RouteStep(
    val durationSeconds: Int? = null,
    val distanceMeters: Int? = null,
    val navigationInstruction: String? = null,
    val transitDetails: TransitDetails? = null,
    val travelMode: TravelMode? = null,
    val polyline: List<LatLng> = emptyList()
)

data class TransitDetails(
    val stopDetails: TransitStopDetails? = null,
    val localizedValues: TransitLocalizedValues? = null,
    val headsign: String? = null,
    val transitLine: TransitLine? = null,
    val stopCount: Int? = null,
)

data class TransitStopDetails(
    val arrivalStop: TransitStop? = null,
    val departureStop: TransitStop? = null,
)

data class TransitStop(
    val name: String? = null,
    val location: LatLng? = null,
)

data class TransitLocalizedValues(
    val arrivalTime: String? = null,
    val departureTime: String? = null,
)

data class TransitLine(
    val name: String? = null,
    val shortName: String? = null,
    val color: String? = null,
    val vehicle: TransitVehicle? = null,
)

data class TransitVehicle(
    val name: LocalizedText? = null,
    val type: String? = null,
)
