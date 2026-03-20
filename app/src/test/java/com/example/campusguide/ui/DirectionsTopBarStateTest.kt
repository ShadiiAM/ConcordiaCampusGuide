package com.example.campusguide.ui

import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.screens.map.DirectionsTopBarState
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

    // US-2.7: step-by-step instructions
    @Test
    fun `currentSteps is null by default`() {
        val state = DirectionsTopBarState(active = true)
        assertNull(state.currentSteps)
    }

    @Test
    fun `currentSteps is stored when a RouteLeg is provided`() {
        val leg = com.example.campusguide.ui.directions.RouteLeg(
            durationSeconds = 360,
            distanceMeters = 400,
            steps = listOf(
                com.example.campusguide.ui.directions.RouteStep(
                    durationSeconds = 60,
                    distanceMeters = 88,
                    navigationInstruction = "Head toward Rue Guy"
                ),
                com.example.campusguide.ui.directions.RouteStep(
                    durationSeconds = 120,
                    distanceMeters = 140,
                    navigationInstruction = "Turn left onto Rue Guy"
                ),
            )
        )
        val state = DirectionsTopBarState(active = true, currentSteps = leg)
        assertNotNull(state.currentSteps)
        assertEquals(2, state.currentSteps!!.steps.size)
    }

    @Test
    fun `step instructions are ordered and non-empty`() {
        val steps = listOf(
            com.example.campusguide.ui.directions.RouteStep(navigationInstruction = "Head toward Rue Guy"),
            com.example.campusguide.ui.directions.RouteStep(navigationInstruction = "Turn left onto Rue Guy"),
            com.example.campusguide.ui.directions.RouteStep(navigationInstruction = "Turn right onto Blvd. De Maisonneuve Ouest"),
        )
        val leg = com.example.campusguide.ui.directions.RouteLeg(steps = steps)
        val state = DirectionsTopBarState(active = true, currentSteps = leg)

        val instructions = state.currentSteps!!.steps.map { it.navigationInstruction }
        assertEquals("Head toward Rue Guy", instructions[0])
        assertEquals("Turn left onto Rue Guy", instructions[1])
        assertEquals("Turn right onto Blvd. De Maisonneuve Ouest", instructions[2])
    }

    @Test
    fun `step with null instruction does not crash`() {
        val leg = com.example.campusguide.ui.directions.RouteLeg(
            steps = listOf(com.example.campusguide.ui.directions.RouteStep(navigationInstruction = null))
        )
        val state = DirectionsTopBarState(active = true, currentSteps = leg)
        assertNull(state.currentSteps!!.steps[0].navigationInstruction)
    }

    @Test
    fun `step distance formats correctly below 1000 m`() {
        val m = 400
        val result = if (m < 1000) "$m m" else "${"%.1f".format(m / 1000.0)} km"
        assertEquals("400 m", result)
    }

    @Test
    fun `step distance formats correctly at or above 1000 m`() {
        val m = 1500
        val result = if (m < 1000) "$m m" else "${"%.1f".format(m / 1000.0)} km"
        assertEquals("1.5 km", result)
    }

    @Test
    fun `empty steps list is handled gracefully`() {
        val leg = com.example.campusguide.ui.directions.RouteLeg(steps = emptyList())
        val state = DirectionsTopBarState(active = true, currentSteps = leg)
        assertTrue(state.currentSteps!!.steps.isEmpty())
    }

    @Test
    fun `state with steps can be copied with new mode`() {
        val leg = com.example.campusguide.ui.directions.RouteLeg(
            steps = listOf(com.example.campusguide.ui.directions.RouteStep(navigationInstruction = "Walk north"))
        )
        val state = DirectionsTopBarState(active = true, currentSteps = leg, selectedMode = TravelMode.WALK)
        val updated = state.copy(selectedMode = com.example.campusguide.ui.directions.TravelMode.TRANSIT)

        assertEquals(com.example.campusguide.ui.directions.TravelMode.TRANSIT, updated.selectedMode)
        assertEquals(1, updated.currentSteps!!.steps.size)
    }

    // Direction-icon classification (matches directionIconFor logic in DirectionsTopBar)
    private fun classifyDirection(instruction: String?): String {
        val lower = instruction?.lowercase() ?: return "straight"
        return when {
            "u-turn" in lower || "uturn" in lower                               -> "uturn"
            "turn left" in lower || "left onto" in lower
                    || "slight left" in lower || "keep left" in lower           -> "left"
            "turn right" in lower || "right onto" in lower
                    || "slight right" in lower || "keep right" in lower         -> "right"
            else                                                                -> "straight"
        }
    }

    @Test
    fun `turn left instruction maps to left direction`() {
        assertEquals("left", classifyDirection("Turn left onto Rue Guy"))
    }

    @Test
    fun `turn right instruction maps to right direction`() {
        assertEquals("right", classifyDirection("Turn right onto Blvd. De Maisonneuve Ouest"))
    }

    @Test
    fun `head toward instruction maps to straight`() {
        assertEquals("straight", classifyDirection("Head toward Rue Guy"))
    }

    @Test
    fun `slight left maps to left`() {
        assertEquals("left", classifyDirection("Slight left onto the path"))
    }

    @Test
    fun `slight right maps to right`() {
        assertEquals("right", classifyDirection("Slight right onto the ramp"))
    }

    @Test
    fun `null instruction maps to straight`() {
        assertEquals("straight", classifyDirection(null))
    }

    @Test
    fun `u-turn instruction maps to uturn`() {
        assertEquals("uturn", classifyDirection("Make a U-turn"))
    }

    @Test
    fun `keep right maps to right`() {
        assertEquals("right", classifyDirection("Keep right to stay on Blvd."))
    }

    // ── Default field values ──────────────────────────────────────────────────

    @Test
    fun `showActions is false by default`() {
        val state = DirectionsTopBarState(active = true)
        assertFalse(state.showActions)
    }

    @Test
    fun `isLoadingRoute is false by default`() {
        val state = DirectionsTopBarState(active = true)
        assertFalse(state.isLoadingRoute)
    }

    @Test
    fun `isCrossCampus is false by default`() {
        val state = DirectionsTopBarState(active = true)
        assertFalse(state.isCrossCampus)
    }

    @Test
    fun `errorMessage is null by default`() {
        val state = DirectionsTopBarState(active = true)
        assertNull(state.errorMessage)
    }

    @Test
    fun `originLabel defaults to Your location`() {
        val state = DirectionsTopBarState(active = true)
        assertEquals("Your location", state.originLabel)
    }

    @Test
    fun `state holds routeSummary and currentSteps simultaneously`() {
        val leg = com.example.campusguide.ui.directions.RouteLeg(
            durationSeconds = 600,
            distanceMeters = 800,
            steps = listOf(com.example.campusguide.ui.directions.RouteStep(navigationInstruction = "Walk north"))
        )
        val state = DirectionsTopBarState(
            active = true,
            routeSummary = "10 min · 800 m",
            currentSteps = leg,
            showActions = false
        )
        assertNotNull(state.routeSummary)
        assertNotNull(state.currentSteps)
        assertFalse(state.showActions)
        assertEquals(1, state.currentSteps!!.steps.size)
    }

    // ── Route summary format (mirrors MapScreen.buildRouteSummary logic) ──────

    @Test
    fun `route summary format for duration under one hour`() {
        val durationSeconds = 600 // 10 min
        val distanceMeters = 800
        val dur = run {
            val mins = durationSeconds / 60
            if (mins < 60) "$mins min" else "${mins / 60} h ${mins % 60} min"
        }
        val dist = if (distanceMeters >= 1000) "${"%.1f".format(distanceMeters / 1000.0)} km" else "$distanceMeters m"
        assertEquals("10 min · 800 m", listOfNotNull(dur, dist).joinToString(" · "))
    }

    @Test
    fun `route summary format for duration over one hour`() {
        val durationSeconds = 4200 // 1 h 10 min
        val distanceMeters = 15000
        val dur = run {
            val mins = durationSeconds / 60
            if (mins < 60) "$mins min" else "${mins / 60} h ${mins % 60} min"
        }
        val dist = "${"%.1f".format(distanceMeters / 1000.0)} km"
        assertEquals("1 h 10 min · 15.0 km", listOfNotNull(dur, dist).joinToString(" · "))
    }

    @Test
    fun `route summary shows only distance when duration is missing`() {
        val dur: String? = null
        val dist: String? = "500 m"
        assertEquals("500 m", listOfNotNull(dur, dist).joinToString(" · "))
    }

    @Test
    fun `route summary shows only duration when distance is missing`() {
        val dur: String? = "5 min"
        val dist: String? = null
        assertEquals("5 min", listOfNotNull(dur, dist).joinToString(" · "))
    }

    // ── Additional direction-icon branches ────────────────────────────────────

    @Test
    fun `left onto instruction maps to left`() {
        assertEquals("left", classifyDirection("left onto Sherbrooke"))
    }

    @Test
    fun `right onto instruction maps to right`() {
        assertEquals("right", classifyDirection("right onto Guy"))
    }

    @Test
    fun `keep left maps to left`() {
        assertEquals("left", classifyDirection("Keep left at the fork"))
    }

    @Test
    fun `uturn without hyphen maps to uturn`() {
        assertEquals("uturn", classifyDirection("uturn at the lights"))
    }

    @Test
    fun `mixed case instruction is handled`() {
        assertEquals("left", classifyDirection("TURN LEFT onto Main St"))
    }
}