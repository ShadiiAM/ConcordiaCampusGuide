package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class RouteRequestTest {

    private val origin = LatLng(45.4972, -73.5789)
    private val destination = LatLng(45.4582, -73.6402)

    // app requests a route using the selected mode
    @Test
    fun `RouteRequest defaults to DRIVE travel mode`() {
        val request = RouteRequest(origin = origin, destination = destination)
        assertEquals("DRIVE", request.travelMode)
    }

    @Test
    fun `RouteRequest uses WALK when walk mode selected`() {
        val request = RouteRequest(
            origin = origin,
            destination = destination,
            travelMode = TravelMode.WALK.apiValue
        )
        assertEquals("WALK", request.travelMode)
    }

    @Test
    fun `RouteRequest uses TRANSIT when transit mode selected`() {
        val request = RouteRequest(
            origin = origin,
            destination = destination,
            travelMode = TravelMode.TRANSIT.apiValue
        )
        assertEquals("TRANSIT", request.travelMode)
    }

    @Test
    fun `RouteRequest uses DRIVE when drive mode selected`() {
        val request = RouteRequest(
            origin = origin,
            destination = destination,
            travelMode = TravelMode.DRIVE.apiValue
        )
        assertEquals("DRIVE", request.travelMode)
    }

    // Switching modes updates the route accordingly
    @Test
    fun `switching travel mode creates new request with updated mode`() {
        val original = RouteRequest(
            origin = origin,
            destination = destination,
            travelMode = TravelMode.DRIVE.apiValue
        )
        val switched = original.copy(travelMode = TravelMode.WALK.apiValue)
        assertNotEquals(original.travelMode, switched.travelMode)
        assertEquals("WALK", switched.travelMode)
    }

    @Test
    fun `RouteRequest preserves origin and destination when mode changes`() {
        val request = RouteRequest(
            origin = origin,
            destination = destination,
            travelMode = TravelMode.DRIVE.apiValue
        ).copy(travelMode = TravelMode.WALK.apiValue)

        assertEquals(origin, request.origin)
        assertEquals(destination, request.destination)
    }
}