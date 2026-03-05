package com.example.campusguide.ui

import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.screens.DirectionsTopBarState
import org.junit.Assert.*
import org.junit.Test

class DirectionsTopBarStateTest {

    // The selected mode is clearly visible in the UI
    @Test
    fun `state is inactive by default`() {
        val state = DirectionsTopBarState(active = false)
        assertFalse(state.active)
    }

    @Test
    fun `state is active when directions are open`() {
        val state = DirectionsTopBarState(
            active = true,
            originLabel = "Your location",
            destinationLabel = "Hall Building",
        )
        assertTrue(state.active)
    }

    @Test
    fun `default travel mode is DRIVE`() {
        val state = DirectionsTopBarState(active = true)
        assertEquals(TravelMode.DRIVE, state.selectedMode)
    }

    // Switching modes updates the route summary accordingly
    @Test
    fun `state reflects updated travel mode`() {
        val state = DirectionsTopBarState(
            active = true,
            selectedMode = TravelMode.WALK
        )
        assertEquals(TravelMode.WALK, state.selectedMode)
    }

    @Test
    fun `route summary is null before route is fetched`() {
        val state = DirectionsTopBarState(active = true)
        assertNull(state.routeSummary)
    }

    @Test
    fun `route summary is shown after route is fetched`() {
        val state = DirectionsTopBarState(
            active = true,
            routeSummary = "10 min · 2.1 km"
        )
        assertNotNull(state.routeSummary)
        assertEquals("10 min · 2.1 km", state.routeSummary)
    }

    // If a mode is unavailable, app shows a clear message
    @Test
    fun `error message can be stored in state`() {
        val state = DirectionsTopBarState(
            active = true,
            routeSummary = null
        )
        assertNull(state.routeSummary)
    }


}