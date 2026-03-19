package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class NearestShuttleStopFinderTest {

    // --- Test data ---
    private val sgwStop = ShuttleStop(
        id = "sgw",
        name = "SGW Shuttle Stop",
        description = "Henry F. Hall Building",
        latLng = LatLng(45.4972, -73.5789),
        campus = Campus.SGW,
    )
    private val loyArrivalStop = ShuttleStop(
        id = "loy_arrival",
        name = "Loyola Shuttle Stop (Arrival)",
        description = "Vanier Library",
        latLng = LatLng(45.4582, -73.6402),
        campus = Campus.LOYOLA,
    )
    private val loyDepartureStop = ShuttleStop(
        id = "loy_departure",
        name = "Loyola Shuttle Stop (Departure)",
        description = "Central Building",
        latLng = LatLng(45.4580, -73.6400),
        campus = Campus.LOYOLA,
    )
    private val allStops = listOf(sgwStop, loyArrivalStop, loyDepartureStop)

    // --- find() ---

    @Test
    fun `find returns null when stop list is empty`() {
        val userLatLng = LatLng(45.4972, -73.5789)
        val result = NearestShuttleStopFinder.find(userLatLng, emptyList())
        assertNull(result)
    }

    @Test
    fun `find returns single stop when list has one element`() {
        val userLatLng = LatLng(45.4972, -73.5789)
        val result = NearestShuttleStopFinder.find(userLatLng, listOf(sgwStop))
        assertNotNull(result)
        assertEquals(sgwStop.id, result!!.stop.id)
    }

    @Test
    fun `find returns nearest stop when user is near SGW`() {
        val userNearSgw = LatLng(45.4970, -73.5785)
        val result = NearestShuttleStopFinder.find(userNearSgw, allStops)
        assertNotNull(result)
        assertEquals(sgwStop.id, result!!.stop.id)
    }

    @Test
    fun `find returns nearest stop when user is near Loyola`() {
        val userNearLoy = LatLng(45.4581, -73.6401)
        val result = NearestShuttleStopFinder.find(userNearLoy, allStops)
        assertNotNull(result)
        assertTrue(result!!.stop.campus == Campus.LOYOLA)
    }

    @Test
    fun `find sets isAtStop true when user is within 30 metres`() {
        // Place user exactly at SGW stop coordinates
        val userAtStop = LatLng(sgwStop.latLng.latitude, sgwStop.latLng.longitude)
        val result = NearestShuttleStopFinder.find(userAtStop, allStops)
        assertNotNull(result)
        assertTrue(result!!.isAtStop)
    }

    @Test
    fun `find sets isAtStop false when user is far from all stops`() {
        val userFarAway = LatLng(45.5000, -73.5500)
        val result = NearestShuttleStopFinder.find(userFarAway, allStops)
        assertNotNull(result)
        assertFalse(result!!.isAtStop)
    }

    @Test
    fun `find returns positive distance`() {
        val userLatLng = LatLng(45.4900, -73.5700)
        val result = NearestShuttleStopFinder.find(userLatLng, allStops)
        assertNotNull(result)
        assertTrue(result!!.distanceMetres > 0f)
    }

    // --- formatDistance() ---

    @Test
    fun `formatDistance shows metres when under 1000m`() {
        val formatted = NearestShuttleStopFinder.formatDistance(500f)
        assertEquals("500 m", formatted)
    }

    @Test
    fun `formatDistance shows km when 1000m or more`() {
        val formatted = NearestShuttleStopFinder.formatDistance(1500f)
        assertEquals("1.5 km", formatted)
    }

    @Test
    fun `formatDistance shows zero metres for zero distance`() {
        val formatted = NearestShuttleStopFinder.formatDistance(0f)
        assertEquals("0 m", formatted)
    }

    @Test
    fun `formatDistance rounds km to one decimal place`() {
        val formatted = NearestShuttleStopFinder.formatDistance(2345f)
        assertEquals("2.3 km", formatted)
    }

    // --- distanceBetween() ---

    @Test
    fun `distanceBetween returns zero for identical coordinates`() {
        val point = LatLng(45.4972, -73.5789)
        val distance = NearestShuttleStopFinder.distanceBetween(point, point)
        assertEquals(0f, distance, 1f)
    }

    @Test
    fun `distanceBetween returns positive value for different coordinates`() {
        val distance = NearestShuttleStopFinder.distanceBetween(
            sgwStop.latLng,
            loyArrivalStop.latLng
        )
        assertTrue(distance > 0f)
    }

    @Test
    fun `distanceBetween is symmetric`() {
        val d1 = NearestShuttleStopFinder.distanceBetween(sgwStop.latLng, loyArrivalStop.latLng)
        val d2 = NearestShuttleStopFinder.distanceBetween(loyArrivalStop.latLng, sgwStop.latLng)
        assertEquals(d1, d2, 1f)
    }

    @Test
    fun `distanceBetween SGW to Loyola is approximately 6km`() {
        val distance = NearestShuttleStopFinder.distanceBetween(
            sgwStop.latLng,
            loyArrivalStop.latLng
        )
        // SGW to Loyola is roughly 6km straight line
        assertTrue("Expected ~6km but got ${distance}m", distance in 5000f..7000f)
    }
}