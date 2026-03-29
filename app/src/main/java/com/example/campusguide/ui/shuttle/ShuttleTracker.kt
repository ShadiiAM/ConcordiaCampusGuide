package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng

/**
 * Domain class for shuttle bus logic.
 */
class ShuttleTracker(private val dataSource: ShuttleDataSource = StaticShuttleDataSource()) {

    // retrieves shuttle stops from the data source, failing gracefully with an empty list.
    private fun safeGetShuttleStops(): List<ShuttleStop> =
        runCatching { dataSource.getShuttleStops() }.getOrDefault(emptyList())

    // Map of campus → list of shuttle stop LatLngs
    val campusStops: Map<Campus, List<LatLng>> by lazy {
        safeGetShuttleStops().groupBy(ShuttleStop::campus) { it.latLng }
    }

    /** Returns true when stop data is available and non-empty. Fails gracefully. */
    fun isOperational(): Boolean = safeGetShuttleStops().isNotEmpty()

    fun getShuttleStops(): List<ShuttleStop> = safeGetShuttleStops()

    fun getNextShuttleForCampus(campus: Campus): String? {
        val next = ShuttleSchedule.nextDeparture(campus)
        if (next != null) return next.toString()

        val nextDay = ShuttleSchedule.nextDepartureNextDay(campus) ?: return null
        return "Next ${nextDay.first}: ${nextDay.second}"
    }
}
