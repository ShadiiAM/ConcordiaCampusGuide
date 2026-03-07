package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class ShuttleTrackerTest {

    private val tracker = ShuttleTracker(StaticShuttleDataSource())

    @Test
    fun `getShuttleStops returns 3 stops`() {
        assertEquals(3, tracker.getShuttleStops().size)
    }

    @Test
    fun `campusStops contains SGW key`() {
        assertTrue(tracker.campusStops.containsKey(Campus.SGW))
    }

    @Test
    fun `campusStops contains LOYOLA key`() {
        assertTrue(tracker.campusStops.containsKey(Campus.LOYOLA))
    }

    @Test
    fun `campusStops has exactly two entries`() {
        assertEquals(2, tracker.campusStops.size)
    }

    @Test
    fun `isOperational returns true for static data source`() {
        assertTrue(tracker.isOperational())
    }

    @Test
    fun `isOperational returns false when data source throws`() {
        val failingSource = object : ShuttleDataSource {
            override fun getShuttleStops(): List<ShuttleStop> = throw RuntimeException("network error")
        }
        val failingTracker = ShuttleTracker(failingSource)
        assertFalse(failingTracker.isOperational())
    }

    @Test
    fun `isOperational returns false for empty data source`() {
        val emptySource = object : ShuttleDataSource {
            override fun getShuttleStops(): List<ShuttleStop> = emptyList()
        }
        assertFalse(ShuttleTracker(emptySource).isOperational())
    }

    @Test
    fun `getNextShuttle returns null as stub for US-3_2`() {
        assertNull(tracker.getNextShuttle())
    }

    @Test
    fun `custom data source is used via Strategy pattern`() {
        val customStop = ShuttleStop("custom", "Custom Stop", Campus.SGW, LatLng(0.0, 0.0))
        val customSource = object : ShuttleDataSource {
            override fun getShuttleStops() = listOf(customStop)
        }
        val customTracker = ShuttleTracker(customSource)
        assertEquals(1, customTracker.getShuttleStops().size)
        assertEquals("custom", customTracker.getShuttleStops().first().id)
    }
}
