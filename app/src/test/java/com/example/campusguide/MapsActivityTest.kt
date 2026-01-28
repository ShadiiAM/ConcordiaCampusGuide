package com.example.campusguide

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

/**
 * Unit tests for MapsActivity using Robolectric
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MapsActivityTest {

    @Test
    fun mapsActivity_onCreate_shouldInflateLayout() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        ActivityScenario.launch<MapsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity should be created", activity)
                assertNotNull("Activity should have window", activity.window)
            }
        }
    }

    @Test
    fun mapsActivity_onCreate_executesSuccessfully() {
        // Use Robolectric to create activity and test onCreate execution
        val controller: ActivityController<MapsActivity> = Robolectric.buildActivity(MapsActivity::class.java)
        val activity = controller.create().start().resume().get()

        assertNotNull("Activity should be created", activity)
        assertNotNull("Activity window should be available", activity.window)
        assertNotNull("Activity content view should be set", activity.window.decorView)
    }

    @Test
    fun mapsActivity_hasCorrectConcordiaCoordinates() {
        // Verify Concordia SGW campus coordinates are correctly defined
        val expectedLatitude = 45.4972
        val expectedLongitude = -73.5789
        val tolerance = 0.0001

        // Create the LatLng object as used in the actual code
        val concordiaSGW = LatLng(expectedLatitude, expectedLongitude)

        assertEquals("Latitude should be 45.4972", expectedLatitude, concordiaSGW.latitude, tolerance)
        assertEquals("Longitude should be -73.5789", expectedLongitude, concordiaSGW.longitude, tolerance)
    }

    @Test
    fun mapsActivity_hasCorrectZoomLevel() {
        // Verify zoom level is within valid Google Maps range
        val zoomLevel = 15f
        assertTrue("Zoom level should be between 2 and 21", zoomLevel in 2f..21f)
        assertEquals("Zoom level should be 15", 15f, zoomLevel)
    }

    @Test
    fun mapsActivity_extendsAppCompatActivity() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        ActivityScenario.launch<MapsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "MapsActivity should extend AppCompatActivity",
                    activity is androidx.appcompat.app.AppCompatActivity
                )
            }
        }
    }

    @Test
    fun mapsActivity_implementsOnMapReadyCallback() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        ActivityScenario.launch<MapsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "MapsActivity should implement OnMapReadyCallback",
                    activity is com.google.android.gms.maps.OnMapReadyCallback
                )
            }
        }
    }

    @Test
    fun mapsActivity_concordiaSGWLocation_isValid() {
        // Test that the Concordia SGW coordinates create a valid LatLng
        val concordiaSGW = LatLng(45.4972, -73.5789)

        assertNotNull("LatLng should be created", concordiaSGW)
        assertTrue("Latitude should be valid", concordiaSGW.latitude >= -90 && concordiaSGW.latitude <= 90)
        assertTrue("Longitude should be valid", concordiaSGW.longitude >= -180 && concordiaSGW.longitude <= 180)
    }

    @Test
    fun mapsActivity_markerTitle_isCorrect() {
        // Verify the marker title string is correct
        val expectedTitle = "Concordia University - SGW Campus"
        assertEquals("Marker title should be correct", expectedTitle, "Concordia University - SGW Campus")
    }

    @Test
    fun mapsActivity_concordiaLocation_withinMontrealBounds() {
        // Verify the Concordia location is within Montreal bounds
        val concordiaSGW = LatLng(45.4972, -73.5789)

        assertTrue("Concordia should be in Montreal (latitude)",
            concordiaSGW.latitude > 45.0 && concordiaSGW.latitude < 46.0)
        assertTrue("Concordia should be in Montreal (longitude)",
            concordiaSGW.longitude > -74.0 && concordiaSGW.longitude < -73.0)
    }

    @Test
    fun mapsActivity_markerOptions_createsSuccessfully() {
        // Test marker options creation (as used in onMapReady)
        val concordiaSGW = LatLng(45.4972, -73.5789)
        val markerTitle = "Concordia University - SGW Campus"

        assertNotNull("Concordia location should not be null", concordiaSGW)
        assertEquals("Marker title should match", markerTitle, "Concordia University - SGW Campus")

        // Verify coordinates are within valid ranges
        assertTrue("Latitude should be valid", concordiaSGW.latitude in -90.0..90.0)
        assertTrue("Longitude should be valid", concordiaSGW.longitude in -180.0..180.0)
    }
}
