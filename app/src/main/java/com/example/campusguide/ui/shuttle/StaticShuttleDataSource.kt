package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng

// Applies "Replace Magic Literal" refactoring — named constants for all stop coordinates
// Coordinates sourced from https://www.concordia.ca/maps/shuttle-bus.html (DMS converted to decimal)
private val SGW_STOP_LAT_LNG      = LatLng(45.4971, -73.5785)  // Hall Building front door, De Maisonneuve Blvd W
private val LOYOLA_ARRIVAL_LAT_LNG   = LatLng(45.4579, -73.6389)  // Loyola stop — arriving from downtown
private val LOYOLA_DEPARTURE_LAT_LNG = LatLng(45.4576, -73.6390)  // Loyola stop — departing to downtown

/**
 * Static MVP implementation of [ShuttleDataSource].
 * Stop locations sourced from https://www.concordia.ca/maps/shuttle-bus.html
 * Three stops total: one at SGW, two at Loyola (arrival and departure sides).
 */
class StaticShuttleDataSource : ShuttleDataSource {
    override fun getShuttleStops(): List<ShuttleStop> = listOf(
        ShuttleStop(
            id = "sgw_shuttle",
            name = "SGW Shuttle Stop",
            campus = Campus.SGW,
            latLng = SGW_STOP_LAT_LNG,
            description = "Henry F. Hall Building, 1455 De Maisonneuve Blvd. W."
        ),
        ShuttleStop(
            id = "loyola_shuttle_arrival",
            name = "Loyola Shuttle Stop (Arrival)",
            campus = Campus.LOYOLA,
            latLng = LOYOLA_ARRIVAL_LAT_LNG,
            description = "Loyola Chapel, 7137 Sherbrooke St. W. — Drop-off from downtown"
        ),
        ShuttleStop(
            id = "loyola_shuttle_departure",
            name = "Loyola Shuttle Stop (Departure)",
            campus = Campus.LOYOLA,
            latLng = LOYOLA_DEPARTURE_LAT_LNG,
            description = "Loyola Chapel, 7137 Sherbrooke St. W. — Pick-up to downtown"
        )
    )
}
