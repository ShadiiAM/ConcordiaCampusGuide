package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

/**
 * Finds the shuttle stop closest to the user's current location.
 */
object NearestShuttleStopFinder {

    // User is considered "at" a stop when within this radius
    private const val AT_STOP_RADIUS_METRES = 30.0

    data class NearestStopResult(
        val stop: ShuttleStop,
        val distanceMetres: Float,
        /** True when the user is within [AT_STOP_RADIUS_METRES] of the stop. */
        val isAtStop: Boolean,
    )

    /**
     * Returns the nearest stop to [userLatLng], or null if [stops] is empty.
     */
    fun find(userLatLng: LatLng, stops: List<ShuttleStop>): NearestStopResult? {
        if (stops.isEmpty()) return null
        val results = stops.map { stop ->
            val distance = distanceBetween(userLatLng, stop.latLng)
            Triple(stop, distance, distance <= AT_STOP_RADIUS_METRES)
        }
        val nearest = results.minByOrNull { it.second } ?: return null
        return NearestStopResult(nearest.first, nearest.second, nearest.third)
    }

    /** Formats a distance in metres as "X m" or "X.X km". */
    fun formatDistance(metres: Float): String =
        if (metres < 1000f) "${metres.toInt()} m"
        else "${"%.1f".format(metres / 1000f)} km"

    /**
     * Haversine formula: returns the great-circle distance between two points in metres.
     */
    fun distanceBetween(from: LatLng, to: LatLng): Float {
        val earthRadiusM = 6_371_000.0
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)

        // Standard haversine intermediate value
        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadiusM * c).toFloat()
    }
}