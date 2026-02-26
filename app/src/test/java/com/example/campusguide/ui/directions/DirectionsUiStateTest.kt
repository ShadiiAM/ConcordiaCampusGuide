package com.example.campusguide.ui.directions

import com.example.campusguide.ui.map.utils.BuildingHit
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class DirectionsUiStateTest {

    @Test
    fun directionsStep_pickDestination() {
        val step = DirectionsStep.PickDestination
        assertTrue(step is DirectionsStep.PickDestination)
    }

    @Test
    fun directionsStep_confirmDestination_createsCorrectly() {
        val destination = LatLng(45.4972, -73.5789)
        val buildingHit = BuildingHit("H", JSONObject().apply {
            put("building-code", "H")
            put("building-name", "Henry F. Hall")
        })
        
        val step = DirectionsStep.ConfirmDestination(destination, buildingHit)
        
        assertEquals(destination, step.destination)
        assertEquals(buildingHit, step.buildingHit)
    }

    @Test
    fun directionsStep_confirmDestination_withNullBuilding() {
        val destination = LatLng(45.4972, -73.5789)
        
        val step = DirectionsStep.ConfirmDestination(destination, null)
        
        assertEquals(destination, step.destination)
        assertNull(step.buildingHit)
    }

    @Test
    fun directionsStep_planRoute_createsCorrectly() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val buildingHit = BuildingHit("MB", JSONObject())
        
        val step = DirectionsStep.PlanRoute(origin, destination, buildingHit)
        
        assertEquals(origin, step.origin)
        assertEquals(destination, step.destination)
        assertEquals(buildingHit, step.buildingHit)
    }

    @Test
    fun directionsStep_showingRoute_createsCorrectly() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val route = RouteResult(listOf(origin, destination))
        val buildingHit = BuildingHit("H", JSONObject())
        
        val step = DirectionsStep.ShowingRoute(origin, destination, buildingHit, route)
        
        assertEquals(origin, step.origin)
        assertEquals(destination, step.destination)
        assertEquals(buildingHit, step.buildingHit)
        assertEquals(route, step.route)
    }

    @Test
    fun directionsStep_dataClassEquality() {
        val destination = LatLng(45.4972, -73.5789)
        val buildingHit = BuildingHit("H", JSONObject())
        
        val step1 = DirectionsStep.ConfirmDestination(destination, buildingHit)
        val step2 = DirectionsStep.ConfirmDestination(destination, buildingHit)
        
        assertEquals(step1, step2)
    }

    @Test
    fun directionsStep_copyConfirmDestination() {
        val destination = LatLng(45.4972, -73.5789)
        val buildingHit = BuildingHit("H", JSONObject())
        
        val step1 = DirectionsStep.ConfirmDestination(destination, buildingHit)
        val step2 = step1.copy()
        
        assertEquals(step1, step2)
    }

    @Test
    fun directionsStep_copyPlanRoute() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val buildingHit = BuildingHit("H", JSONObject())
        
        val step1 = DirectionsStep.PlanRoute(origin, destination, buildingHit)
        val step2 = step1.copy()
        
        assertEquals(step1, step2)
    }

    @Test
    fun directionsStep_copyShowingRoute() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val route = RouteResult(listOf(origin, destination))
        val buildingHit = BuildingHit("H", JSONObject())
        
        val step1 = DirectionsStep.ShowingRoute(origin, destination, buildingHit, route)
        val step2 = step1.copy()
        
        assertEquals(step1, step2)
    }

    @Test
    fun directionsUiState_defaultValues() {
        val state = DirectionsUiState()
        
        assertTrue(state.step is DirectionsStep.PickDestination)
        assertFalse(state.isLoadingRoute)
        assertNull(state.errorMessage)
    }

    @Test
    fun directionsUiState_withCustomStep() {
        val destination = LatLng(45.4972, -73.5789)
        val step = DirectionsStep.ConfirmDestination(destination, null)
        
        val state = DirectionsUiState(step = step)
        
        assertEquals(step, state.step)
        assertFalse(state.isLoadingRoute)
        assertNull(state.errorMessage)
    }

    @Test
    fun directionsUiState_withLoadingState() {
        val state = DirectionsUiState(isLoadingRoute = true)
        
        assertTrue(state.isLoadingRoute)
    }

    @Test
    fun directionsUiState_withErrorMessage() {
        val errorMessage = "Failed to load route"
        
        val state = DirectionsUiState(errorMessage = errorMessage)
        
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun directionsUiState_copy() {
        val state1 = DirectionsUiState(isLoadingRoute = true)
        val state2 = state1.copy()
        
        assertEquals(state1, state2)
    }

    @Test
    fun directionsUiState_copyWithModification() {
        val state1 = DirectionsUiState(isLoadingRoute = false)
        val state2 = state1.copy(isLoadingRoute = true)
        
        assertFalse(state1.isLoadingRoute)
        assertTrue(state2.isLoadingRoute)
    }

    @Test
    fun directionsUiState_allProperties() {
        val destination = LatLng(45.4972, -73.5789)
        val step = DirectionsStep.ConfirmDestination(destination, null)
        val errorMessage = "Network error"
        
        val state = DirectionsUiState(
            step = step,
            isLoadingRoute = true,
            errorMessage = errorMessage
        )
        
        assertEquals(step, state.step)
        assertTrue(state.isLoadingRoute)
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun directionsUiState_dataClassEquality() {
        val state1 = DirectionsUiState(isLoadingRoute = true)
        val state2 = DirectionsUiState(isLoadingRoute = true)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun directionsUiState_toString() {
        val state = DirectionsUiState(isLoadingRoute = true)
        
        val string = state.toString()
        assertTrue(string.contains("DirectionsUiState"))
        assertTrue(string.contains("isLoadingRoute"))
    }

    @Test
    fun directionsStep_sealedClass_whenExpression() {
        val steps = listOf<DirectionsStep>(
            DirectionsStep.PickDestination,
            DirectionsStep.ConfirmDestination(LatLng(45.0, -73.0), null),
            DirectionsStep.PlanRoute(LatLng(45.0, -73.0), LatLng(46.0, -74.0), null),
            DirectionsStep.ShowingRoute(LatLng(45.0, -73.0), LatLng(46.0, -74.0), null, RouteResult(emptyList()))
        )
        
        val results = steps.map { step ->
            when (step) {
                is DirectionsStep.PickDestination -> "pick"
                is DirectionsStep.ConfirmDestination -> "confirm"
                is DirectionsStep.PlanRoute -> "plan"
                is DirectionsStep.ShowingRoute -> "showing"
            }
        }
        
        assertEquals(listOf("pick", "confirm", "plan", "showing"), results)
    }

    @Test
    fun directionsStep_componentAccess_confirmDestination() {
        val destination = LatLng(45.4972, -73.5789)
        val buildingHit = BuildingHit("H", JSONObject())
        val step = DirectionsStep.ConfirmDestination(destination, buildingHit)
        
        val (d, b) = step
        assertEquals(destination, d)
        assertEquals(buildingHit, b)
    }

    @Test
    fun directionsStep_componentAccess_planRoute() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val buildingHit = BuildingHit("H", JSONObject())
        val step = DirectionsStep.PlanRoute(origin, destination, buildingHit)
        
        val (o, d, b) = step
        assertEquals(origin, o)
        assertEquals(destination, d)
        assertEquals(buildingHit, b)
    }

    @Test
    fun directionsUiState_componentAccess() {
        val state = DirectionsUiState(isLoadingRoute = true, errorMessage = "Error")
        
        val (step, loading, error) = state
        assertTrue(step is DirectionsStep.PickDestination)
        assertTrue(loading)
        assertEquals("Error", error)
    }
}
