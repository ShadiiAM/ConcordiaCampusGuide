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
        assertEquals("DRIVE", request.travelMode)
    }

    @Test
    fun routeRequest_createsWithCustomMode() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)

        val request = RouteRequest(origin, destination, TravelMode.WALK.apiValue)
        assertEquals("WALK", request.travelMode)

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
        assertEquals(TravelMode.WALK, TravelMode.valueOf("WALK"))
    }

    @Test
    fun travelMode_enumProperties() {
        val mode = TravelMode.WALK
        assertEquals("WALK", mode.name)
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
        val request = RouteRequest(origin, destination, TravelMode.WALK.apiValue)
        
        val (o, d, m) = request
        assertEquals(origin, o)
        assertEquals(destination, d)
        assertEquals("WALK", request.travelMode)
    }

    @Test
    fun routeResult_componentAccess() {
        val points = listOf(LatLng(45.4972, -73.5789))
        val result = RouteResult(points)
        
        val (p) = result
        assertEquals(points, p)
    }

    @Test
    fun travelMode_allModes_exist() {
        assertNotNull(TravelMode.WALKING)
        assertNotNull(TravelMode.DRIVING)
        assertNotNull(TravelMode.BICYCLING)
        assertNotNull(TravelMode.TRANSIT)
    }

    @Test
    fun travelMode_driving() {
        assertEquals(TravelMode.DRIVING, TravelMode.valueOf("DRIVING"))
        assertEquals("DRIVING", TravelMode.DRIVING.name)
    }

    @Test
    fun travelMode_bicycling() {
        assertEquals(TravelMode.BICYCLING, TravelMode.valueOf("BICYCLING"))
        assertEquals("BICYCLING", TravelMode.BICYCLING.name)
    }

    @Test
    fun travelMode_transit() {
        assertEquals(TravelMode.TRANSIT, TravelMode.valueOf("TRANSIT"))
        assertEquals("TRANSIT", TravelMode.TRANSIT.name)
    }

    @Test
    fun routeRequest_withDrivingMode() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4582, -73.6402)

        val request = RouteRequest(origin, destination, TravelMode.DRIVING)

        assertEquals(TravelMode.DRIVING, request.mode)
    }

    @Test
    fun routeResult_withDurationAndDistance() {
        val points = listOf(LatLng(45.4972, -73.5789))

        val result = RouteResult(
            points = points,
            durationSeconds = 480,
            distanceMeters = 650
        )

        assertEquals(points, result.points)
        assertEquals(480, result.durationSeconds)
        assertEquals(650, result.distanceMeters)
        assertTrue(result.legs.isEmpty())
    }

    @Test
    fun routeResult_withNullDurationAndDistance() {
        val points = listOf(LatLng(45.4972, -73.5789))

        val result = RouteResult(points = points)

        assertNull(result.durationSeconds)
        assertNull(result.distanceMeters)
        assertTrue(result.legs.isEmpty())
    }

    @Test
    fun routeResult_copyWithDuration() {
        val points = listOf(LatLng(45.4972, -73.5789))
        val result1 = RouteResult(points)
        val result2 = result1.copy(durationSeconds = 300)

        assertNull(result1.durationSeconds)
        assertEquals(300, result2.durationSeconds)
    }

    @Test
    fun routeLeg_createsCorrectly() {
        val leg = RouteLeg(
            durationSeconds = 480,
            distanceMeters = 650,
            steps = emptyList()
        )

        assertEquals(480, leg.durationSeconds)
        assertEquals(650, leg.distanceMeters)
        assertTrue(leg.steps.isEmpty())
    }

    @Test
    fun routeLeg_withSteps() {
        val step = RouteStep(
            durationSeconds = 240,
            distanceMeters = 325
        )

        val leg = RouteLeg(
            durationSeconds = 480,
            distanceMeters = 650,
            steps = listOf(step)
        )

        assertEquals(1, leg.steps.size)
        assertEquals(step, leg.steps[0])
    }

    @Test
    fun routeStep_createsCorrectly() {
        val step = RouteStep(
            durationSeconds = 240,
            distanceMeters = 325,
            navigationInstruction = "Turn left"
        )

        assertEquals(240, step.durationSeconds)
        assertEquals(325, step.distanceMeters)
        assertEquals("Turn left", step.navigationInstruction)
        assertNull(step.transitDetails)
    }

    @Test
    fun routeStep_withTransitDetails() {
        val transitDetails = TransitDetails(
            headsign = "Downtown",
            stopCount = 5
        )

        val step = RouteStep(
            durationSeconds = 600,
            distanceMeters = 2000,
            transitDetails = transitDetails
        )

        assertNotNull(step.transitDetails)
        assertEquals("Downtown", step.transitDetails?.headsign)
        assertEquals(5, step.transitDetails?.stopCount)
    }

    @Test
    fun transitDetails_createsCorrectly() {
        val transitDetails = TransitDetails(
            headsign = "Côte-Vertu",
            stopCount = 8
        )

        assertEquals("Côte-Vertu", transitDetails.headsign)
        assertEquals(8, transitDetails.stopCount)
        assertNull(transitDetails.stopDetails)
        assertNull(transitDetails.localizedValues)
        assertNull(transitDetails.transitLine)
    }

    @Test
    fun transitStop_createsCorrectly() {
        val stop = TransitStop(
            name = "Metro Guy-Concordia",
            location = LatLng(45.4972, -73.5789)
        )

        assertEquals("Metro Guy-Concordia", stop.name)
        assertNotNull(stop.location)
        assertEquals(45.4972, stop.location?.latitude ?: 0.0, 0.0001)
    }

    @Test
    fun transitStopDetails_createsCorrectly() {
        val departureStop = TransitStop(name = "Stop A", location = LatLng(45.4972, -73.5789))
        val arrivalStop = TransitStop(name = "Stop B", location = LatLng(45.5000, -73.5800))

        val stopDetails = TransitStopDetails(
            arrivalStop = arrivalStop,
            departureStop = departureStop
        )

        assertEquals("Stop A", stopDetails.departureStop?.name)
        assertEquals("Stop B", stopDetails.arrivalStop?.name)
    }

    @Test
    fun transitLocalizedValues_createsCorrectly() {
        val values = TransitLocalizedValues(
            departureTime = "3:30 PM",
            arrivalTime = "3:45 PM"
        )

        assertEquals("3:30 PM", values.departureTime)
        assertEquals("3:45 PM", values.arrivalTime)
    }

    @Test
    fun transitLine_createsCorrectly() {
        val vehicle = TransitVehicle(name = "Metro", type = "SUBWAY")
        val line = TransitLine(
            name = "Orange Line",
            shortName = "2",
            color = "#FF6600",
            vehicle = vehicle
        )

        assertEquals("Orange Line", line.name)
        assertEquals("2", line.shortName)
        assertEquals("#FF6600", line.color)
        assertEquals("Metro", line.vehicle?.name)
        assertEquals("SUBWAY", line.vehicle?.type)
    }

    @Test
    fun transitVehicle_createsCorrectly() {
        val vehicle = TransitVehicle(
            name = "Bus",
            type = "BUS"
        )

        assertEquals("Bus", vehicle.name)
        assertEquals("BUS", vehicle.type)
    }

    @Test
    fun routeResult_withCompleteData() {
        val points = listOf(
            LatLng(45.4972, -73.5789),
            LatLng(45.5000, -73.5800)
        )

        val step = RouteStep(
            durationSeconds = 240,
            distanceMeters = 325,
            navigationInstruction = "Walk north"
        )

        val leg = RouteLeg(
            durationSeconds = 480,
            distanceMeters = 650,
            steps = listOf(step)
        )

        val result = RouteResult(
            points = points,
            durationSeconds = 480,
            distanceMeters = 650,
            legs = listOf(leg)
        )

        assertEquals(2, result.points.size)
        assertEquals(480, result.durationSeconds)
        assertEquals(650, result.distanceMeters)
        assertEquals(1, result.legs.size)
        assertEquals(1, result.legs[0].steps.size)
        assertEquals("Walk north", result.legs[0].steps[0].navigationInstruction)
    }

    @Test
    fun transitDetails_withAllFields() {
        val departureStop = TransitStop("Station A", LatLng(45.49, -73.57))
        val arrivalStop = TransitStop("Station B", LatLng(45.50, -73.58))
        val stopDetails = TransitStopDetails(
            arrivalStop = arrivalStop,
            departureStop = departureStop
        )

        val localizedValues = TransitLocalizedValues(
            arrivalTime = "2:45 PM",
            departureTime = "2:30 PM"
        )

        val vehicle = TransitVehicle("Metro", "SUBWAY")
        val transitLine = TransitLine("Green Line", "1", "#00AA00", vehicle)

        val transitDetails = TransitDetails(
            stopDetails = stopDetails,
            localizedValues = localizedValues,
            headsign = "Angrignon",
            transitLine = transitLine,
            stopCount = 3
        )

        assertNotNull(transitDetails.stopDetails)
        assertEquals("Station A", transitDetails.stopDetails?.departureStop?.name)
        assertEquals("Station B", transitDetails.stopDetails?.arrivalStop?.name)
        assertEquals("2:30 PM", transitDetails.localizedValues?.departureTime)
        assertEquals("2:45 PM", transitDetails.localizedValues?.arrivalTime)
        assertEquals("Angrignon", transitDetails.headsign)
        assertEquals("Green Line", transitDetails.transitLine?.name)
        assertEquals("1", transitDetails.transitLine?.shortName)
        assertEquals(3, transitDetails.stopCount)
    }
}
