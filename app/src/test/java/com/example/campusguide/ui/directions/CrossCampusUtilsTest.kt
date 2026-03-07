package com.example.campusguide.ui.directions

import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class CrossCampusUtilsTest {

    private val sgwBuilding = CampusBuilding(
        buildingCode = "H",
        buildingName = "Henry F. Hall Building",
        address = "1455 De Maisonneuve Blvd. W.",
        campus = Campus.SGW
    )

    private val loyolaBuilding = CampusBuilding(
        buildingCode = "CC",
        buildingName = "Central Building",
        address = "7141 Sherbrooke St. W.",
        campus = Campus.LOYOLA
    )

    @Test
    fun `isCrossCampusRoute returns true when buildings are on different campuses`() {
        val result = isCrossCampusRoute(sgwBuilding, loyolaBuilding)
        assertTrue(result)
    }

    @Test
    fun `isCrossCampusRoute returns true when origin and destination are swapped`() {
        val result = isCrossCampusRoute(loyolaBuilding, sgwBuilding)
        assertTrue(result)
    }

    @Test
    fun `isCrossCampusRoute returns false when both buildings are on SGW campus`() {
        val sgwBuilding2 = CampusBuilding(
            buildingCode = "EV",
            buildingName = "EV Building",
            address = "1515 Ste-Catherine St. W.",
            campus = Campus.SGW
        )

        val result = isCrossCampusRoute(sgwBuilding, sgwBuilding2)
        assertFalse(result)
    }

    @Test
    fun `isCrossCampusRoute returns false when both buildings are on Loyola campus`() {
        val loyolaBuilding2 = CampusBuilding(
            buildingCode = "AD",
            buildingName = "Administration Building",
            address = "7141 Sherbrooke St. W.",
            campus = Campus.LOYOLA
        )

        val result = isCrossCampusRoute(loyolaBuilding, loyolaBuilding2)
        assertFalse(result)
    }

    @Test
    fun `isCrossCampusRoute returns false when origin is null`() {
        val result = isCrossCampusRoute(null, loyolaBuilding)
        assertFalse(result)
    }

    @Test
    fun `isCrossCampusRoute returns false when destination is null`() {
        val result = isCrossCampusRoute(sgwBuilding, null)
        assertFalse(result)
    }

    @Test
    fun `isCrossCampusRoute returns false when both are null`() {
        val result = isCrossCampusRoute(null, null)
        assertFalse(result)
    }

    @Test
    fun `detectCampus returns LOYOLA for Loyola coordinates`() {
        val loyolaLatLng = LatLng(45.4582, -73.6402)
        val result = detectCampus(loyolaLatLng)
        assertEquals(Campus.LOYOLA, result)
    }

    @Test
    fun `detectCampus returns SGW for SGW coordinates`() {
        val sgwLatLng = LatLng(45.4972, -73.5789)
        val result = detectCampus(sgwLatLng)
        assertEquals(Campus.SGW, result)
    }

    @Test
    fun `supportsCrossCampus returns true for all travel modes`() {
        assertTrue(supportsCrossCampus(TravelMode.TRANSIT))
        assertTrue(supportsCrossCampus(TravelMode.WALK))
        assertTrue(supportsCrossCampus(TravelMode.DRIVE))
    }

    @Test
    fun `recommendedCrossCampusMode returns TRANSIT`() {
        val result = recommendedCrossCampusMode()
        assertEquals(TravelMode.TRANSIT, result)
    }

    @Test
    fun `getCrossCampusMessage returns appropriate message for TRANSIT`() {
        val message = getCrossCampusMessage(TravelMode.TRANSIT)
        assertTrue(message.contains("Concordia Shuttle"))
        assertTrue(message.contains("cross-campus"))
    }

    @Test
    fun `getCrossCampusMessage returns appropriate message for WALK`() {
        val message = getCrossCampusMessage(TravelMode.WALK)
        assertTrue(message.contains("Walking"))
        assertTrue(message.contains("cross-campus"))
    }

    @Test
    fun `getCrossCampusMessage returns appropriate message for DRIVE`() {
        val message = getCrossCampusMessage(TravelMode.DRIVE)
        assertTrue(message.contains("Driving"))
        assertTrue(message.contains("cross-campus"))
    }

    @Test
    fun `getCrossCampusErrorMessage suggests Transit for DRIVE failures`() {
        val errorMessage = getCrossCampusErrorMessage(TravelMode.DRIVE)
        assertTrue(errorMessage.contains("Transit"))
        assertTrue(errorMessage.contains("Shuttle"))
    }

    @Test
    fun `getCrossCampusErrorMessage suggests Transit for WALK failures`() {
        val errorMessage = getCrossCampusErrorMessage(TravelMode.WALK)
        assertTrue(errorMessage.contains("Transit"))
        assertTrue(errorMessage.contains("Shuttle"))
    }

    @Test
    fun `getCrossCampusErrorMessage suggests different mode for TRANSIT failures`() {
        val errorMessage = getCrossCampusErrorMessage(TravelMode.TRANSIT)
        assertTrue(errorMessage.contains("not available"))
        assertTrue(errorMessage.contains("different travel mode"))
    }

    @Test
    fun `formatRouteSummary formats short duration correctly`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = 1320, // 22 minutes
            distanceMeters = 8300 // 8.3 km
        )
        val summary = formatRouteSummary(route)
        assertEquals("22 min • 8.3 km", summary)
    }

    @Test
    fun `formatRouteSummary formats long duration with hours`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = 5580, // 1 hr 33 min
            distanceMeters = 6600 // 6.6 km
        )
        val summary = formatRouteSummary(route)
        assertEquals("1 hr 33 min • 6.6 km", summary)
    }

    @Test
    fun `formatRouteSummary formats exact hours correctly`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = 7200, // 2 hours
            distanceMeters = 15000 // 15 km
        )
        val summary = formatRouteSummary(route)
        assertEquals("2 hr • 15.0 km", summary)
    }

    @Test
    fun `formatRouteSummary handles null duration`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = null,
            distanceMeters = 5000
        )
        val summary = formatRouteSummary(route)
        assertEquals("Unknown duration • 5.0 km", summary)
    }

    @Test
    fun `formatRouteSummary handles null distance`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = 600, // 10 min
            distanceMeters = null
        )
        val summary = formatRouteSummary(route)
        assertEquals("10 min • Unknown distance", summary)
    }

    @Test
    fun `formatRouteSummary handles both null values`() {
        val route = RouteResult(
            points = emptyList(),
            durationSeconds = null,
            distanceMeters = null
        )
        val summary = formatRouteSummary(route)
        assertEquals("Unknown duration • Unknown distance", summary)
    }
}
