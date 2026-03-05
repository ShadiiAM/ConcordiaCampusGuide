package com.example.campusguide.ui.directions

import org.junit.Assert.*
import org.junit.Test

class TravelModeTest {

    // The selected mode is clearly visible in the UI
    // Tests that each mode has a non-empty label for display
    @Test
    fun `each travel mode has a non-empty label`() {
        TravelMode.entries.forEach { mode ->
            assertTrue(
                "Mode ${mode.name} has empty label",
                mode.label.isNotBlank()
            )
        }
    }

    //  app requests and displays a route using that mode
    // Tests that each mode maps to a valid Google Routes API value
    @Test
    fun `each travel mode has a valid api value`() {
        val validApiValues = setOf("DRIVE", "TRANSIT", "WALK")
        TravelMode.entries.forEach { mode ->
            assertTrue(
                "Mode ${mode.name} has invalid apiValue '${mode.apiValue}'",
                mode.apiValue in validApiValues
            )
        }
    }

    // Accessibility - mode selection controls have clear labels
    @Test
    fun `each travel mode has a non-empty content description for accessibility`() {
        TravelMode.entries.forEach { mode ->
            assertTrue(
                "Mode ${mode.name} has empty contentDescription",
                mode.contentDescription.isNotBlank()
            )
        }
    }

    @Test
    fun `DRIVE mode has correct values`() {
        assertEquals("Drive", TravelMode.DRIVE.label)
        assertEquals("DRIVE", TravelMode.DRIVE.apiValue)
    }

    @Test
    fun `TRANSIT mode has correct values`() {
        assertEquals("Transit", TravelMode.TRANSIT.label)
        assertEquals("TRANSIT", TravelMode.TRANSIT.apiValue)
    }

    @Test
    fun `WALK mode has correct values`() {
        assertEquals("Walk", TravelMode.WALK.label)
        assertEquals("WALK", TravelMode.WALK.apiValue)
    }

    @Test
    fun `there are exactly three travel modes`() {
        assertEquals(3, TravelMode.entries.size)
    }
}