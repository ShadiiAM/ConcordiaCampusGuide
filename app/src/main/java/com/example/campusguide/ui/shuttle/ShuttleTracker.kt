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

    /** Safely retrieves shuttle stops from the data source, failing gracefully with an empty list. */
    private fun safeGetShuttleStops(): List<ShuttleStop> =
        runCatching { dataSource.getShuttleStops() }.getOrDefault(emptyList())

    /** Map of campus → list of shuttle stop LatLngs (matches class diagram). */
    val campusStops: Map<Campus, List<LatLng>> by lazy {
        safeGetShuttleStops().groupBy(ShuttleStop::campus) { it.latLng }
    }

    /** Returns true when stop data is available and non-empty. Fails gracefully. */
    fun isOperational(): Boolean = safeGetShuttleStops().isNotEmpty()

    fun getShuttleStops(): List<ShuttleStop> = safeGetShuttleStops()

    /** Stub for US-3.2 — next shuttle time. */
    fun getNextShuttle(): String? = null
}
