package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop

/**
 * Strategy interface for shuttle data retrieval.
 * Allows swapping static data for a live API without changing ShuttleTracker.
 */
interface ShuttleDataSource {
    fun getShuttleStops(): List<ShuttleStop>
}
