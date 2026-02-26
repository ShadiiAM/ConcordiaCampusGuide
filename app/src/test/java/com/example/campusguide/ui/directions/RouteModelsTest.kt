package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class RouteModelsTest {

    @Test
    fun routeRequest_createsWithDefaults() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        
        val request = RouteRequest(origin, destination)
        
        assertEquals(origin, request.origin)
        assertEquals(destination, request.destination)
        assertEquals(TravelMode.WALKING, request.mode)
    }

    @Test
    fun routeRequest_createsWithCustomMode() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        
        val request = RouteRequest(origin, destination, TravelMode.WALKING)
        
        assertEquals(TravelMode.WALKING, request.mode)
    }

    @Test
    fun routeRequest_dataClassProperties() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        
        val request1 = RouteRequest(origin, destination)
        val request2 = RouteRequest(origin, destination)
        
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun routeRequest_copy() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        
        val request1 = RouteRequest(origin, destination)
        val request2 = request1.copy()
        
        assertEquals(request1, request2)
    }

    @Test
    fun routeRequest_copyWithModification() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val newDestination = LatLng(45.5000, -73.6000)
        
        val request1 = RouteRequest(origin, destination)
        val request2 = request1.copy(destination = newDestination)
        
        assertNotEquals(request1, request2)
        assertEquals(newDestination, request2.destination)
        assertEquals(origin, request2.origin)
    }

    @Test
    fun travelMode_walking() {
        assertEquals(TravelMode.WALKING, TravelMode.valueOf("WALKING"))
    }

    @Test
    fun travelMode_enumProperties() {
        val mode = TravelMode.WALKING
        assertEquals("WALKING", mode.name)
    }

    @Test
    fun routeResult_createsWithPoints() {
        val points = listOf(
            LatLng(45.4972, -73.5789),
            LatLng(45.4980, -73.5795),
            LatLng(45.4582, -73.6402)
        )
        
        val result = RouteResult(points)
        
        assertEquals(points, result.points)
        assertEquals(3, result.points.size)
    }

    @Test
    fun routeResult_emptyPoints() {
        val result = RouteResult(emptyList())
        
        assertTrue(result.points.isEmpty())
    }

    @Test
    fun routeResult_dataClassProperties() {
        val points = listOf(LatLng(45.4972, -73.5789))
        
        val result1 = RouteResult(points)
        val result2 = RouteResult(points)
        
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun routeResult_copy() {
        val points = listOf(LatLng(45.4972, -73.5789))
        
        val result1 = RouteResult(points)
        val result2 = result1.copy()
        
        assertEquals(result1, result2)
    }

    @Test
    fun routeResult_copyWithModification() {
        val points1 = listOf(LatLng(45.4972, -73.5789))
        val points2 = listOf(LatLng(45.4582, -73.6402))
        
        val result1 = RouteResult(points1)
        val result2 = result1.copy(points = points2)
        
        assertNotEquals(result1, result2)
        assertEquals(points2, result2.points)
    }

    @Test
    fun routeRequest_toString() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val request = RouteRequest(origin, destination)
        
        val string = request.toString()
        assertTrue(string.contains("RouteRequest"))
        assertTrue(string.contains("origin"))
        assertTrue(string.contains("destination"))
    }

    @Test
    fun routeResult_toString() {
        val points = listOf(LatLng(45.4972, -73.5789))
        val result = RouteResult(points)
        
        val string = result.toString()
        assertTrue(string.contains("RouteResult"))
        assertTrue(string.contains("points"))
    }

    @Test
    fun routeRequest_componentAccess() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)
        val request = RouteRequest(origin, destination, TravelMode.WALKING)
        
        val (o, d, m) = request
        assertEquals(origin, o)
        assertEquals(destination, d)
        assertEquals(TravelMode.WALKING, m)
    }

    @Test
    fun routeResult_componentAccess() {
        val points = listOf(LatLng(45.4972, -73.5789))
        val result = RouteResult(points)
        
        val (p) = result
        assertEquals(points, p)
    }
}
