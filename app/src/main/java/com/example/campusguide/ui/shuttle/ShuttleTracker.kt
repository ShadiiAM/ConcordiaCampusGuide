package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng

/**
 * Domain class for shuttle bus logic.
 * Applies "Extract Class" refactoring (Fowler) — shuttle logic lives here, not inline in MapScreen.
 * Matches the class diagram: campusStops, isOperational(), getShuttleStops().
 */
class ShuttleTracker(private val dataSource: ShuttleDataSource = StaticShuttleDataSource()) {

    /** Map of campus → shuttle stop LatLng (matches class diagram). */
    val campusStops: Map<Campus, LatLng> by lazy {
        dataSource.getShuttleStops().associate { it.campus to it.latLng }
    }

    /** Returns true when stop data is available and non-empty. Fails gracefully. */
    fun isOperational(): Boolean = try {
        dataSource.getShuttleStops().isNotEmpty()
    } catch (_: Exception) {
        false
    }

    fun getShuttleStops(): List<ShuttleStop> = dataSource.getShuttleStops()

    /** Stub for US-3.2 — next shuttle time. */
    fun getNextShuttle(): String? = null
}
