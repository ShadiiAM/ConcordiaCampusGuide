package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop

/**
 * Strategy interface for shuttle data retrieval.
 * Allows swapping static data for a live API without changing ShuttleTracker.
 *
 * Declared as a functional interface so implementations can be expressed as lambdas
 * when needed (e.g. test doubles), following Kotlin SAM conventions.
 */
fun interface ShuttleDataSource {
    fun getShuttleStops(): List<ShuttleStop>
}
