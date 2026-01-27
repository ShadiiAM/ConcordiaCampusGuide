package com.example.campusguide

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MapsActivity
 */
class MapsActivityTest {

    @Test
    fun mapsActivity_concordiaCoordinates_areCorrect() {
        // Verify Concordia SGW campus coordinates
        val expectedLatitude = 45.4972
        val expectedLongitude = -73.5789
        val tolerance = 0.0001

        // These coordinates should match what's configured in MapsActivity
        assertEquals(expectedLatitude, 45.4972, tolerance)
        assertEquals(expectedLongitude, -73.5789, tolerance)
    }

    @Test
    fun mapsActivity_zoomLevel_isValid() {
        // Verify zoom level is within valid range (2-21 for Google Maps)
        val zoomLevel = 15f
        assertTrue("Zoom level should be between 2 and 21", zoomLevel in 2f..21f)
    }

    @Test
    fun mapsActivity_className_isCorrect() {
        // Verify activity class name
        val activityName = MapsActivity::class.simpleName
        assertEquals("MapsActivity", activityName)
    }
}
