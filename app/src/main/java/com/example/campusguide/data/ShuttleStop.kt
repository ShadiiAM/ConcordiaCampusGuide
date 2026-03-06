package com.example.campusguide.data

import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng

/**
 * Represents a Concordia shuttle bus stop.
 * Applies "Introduce Parameter Object" refactoring — consolidates stop fields into a cohesive object.
 */
data class ShuttleStop(
    val id: String,
    val name: String,
    val campus: Campus,
    val latLng: LatLng,
    val description: String = "Concordia Shuttle Service"
)
