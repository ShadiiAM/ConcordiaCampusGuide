package com.example.campusguide.ui.directions

import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng

/**
 * Detects if a route is cross-campus based on building information.
 * If [originBuilding] is null, falls back to detecting the campus from [originLatLng].
 */
fun isCrossCampusRoute(
    originBuilding: CampusBuilding?,
    destinationBuilding: CampusBuilding?,
    originLatLng: LatLng? = null,
): Boolean {
    if (destinationBuilding == null) return false
    val originCampus = originBuilding?.campus
        ?: originLatLng?.let { detectCampus(it) }
        ?: return false
    return originCampus != destinationBuilding.campus
}

/**
 * Detects campus from LatLng coordinates.
 * SGW campus is roughly centered at (45.4972, -73.5789)
 * Loyola campus is roughly centered at (45.4582, -73.6402)
 */
fun detectCampus(latLng: LatLng): Campus {
    // Simple heuristic: Loyola is west of -73.60 longitude
    // SGW is east of -73.60 longitude
    return if (latLng.longitude < -73.60) {
        Campus.LOYOLA
    } else {
        Campus.SGW
    }
}

/**
 * Checks if the given travel mode supports cross-campus routing.
 * TRANSIT is recommended for cross-campus routes (Concordia Shuttle).
 * WALK may work but is very long.
 * DRIVE may not work well due to traffic and parking.
 */
fun supportsCrossCampus(mode: TravelMode): Boolean {
    return when (mode) {
        TravelMode.TRANSIT -> true  // Concordia Shuttle available
        TravelMode.WALK -> true     // Possible but very long (~2 hours)
        TravelMode.DRIVE -> true    // Possible but not recommended (traffic, parking)
    }
}

/**
 * Returns a recommended travel mode for cross-campus routing.
 */
fun recommendedCrossCampusMode(): TravelMode {
    return TravelMode.TRANSIT  // Concordia Shuttle is the best option
}

/**
 * Returns a user-friendly message explaining cross-campus routing options.
 */
fun getCrossCampusMessage(mode: TravelMode): String {
    return when (mode) {
        TravelMode.TRANSIT -> "Using Concordia Shuttle for cross-campus route"
        TravelMode.WALK -> "Walking cross-campus will take approximately 2 hours"
        TravelMode.DRIVE -> "Driving cross-campus may encounter traffic and parking challenges"
    }
}

/**
 * Returns an error message when a route cannot be found for the selected mode.
 */
fun getCrossCampusErrorMessage(mode: TravelMode): String {
    return when (mode) {
        TravelMode.DRIVE -> "Driving directions not available for this cross-campus route. Try Transit mode for the Concordia Shuttle."
        TravelMode.WALK -> "Walking directions not available for this cross-campus route. Try Transit mode for the Concordia Shuttle."
        TravelMode.TRANSIT -> "Shuttle bus is not available. Select a different travel mode."
    }
}

/**
 * Formats route summary from RouteResult for display.
 * Returns formatted string like "22 min • 8.3 km"
 */
fun formatRouteSummary(route: RouteResult): String {
    val durationStr = route.durationSeconds?.let { seconds ->
        val minutes = (seconds / 60)
        if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMins = minutes % 60
            if (remainingMins > 0) {
                "$hours hr $remainingMins min"
            } else {
                "$hours hr"
            }
        }
    } ?: "Unknown duration"

    val distanceStr = route.distanceMeters?.let { meters ->
        val km = meters / 1000.0
        String.format("%.1f km", km)
    } ?: "Unknown distance"

    return "$durationStr • $distanceStr"
}
