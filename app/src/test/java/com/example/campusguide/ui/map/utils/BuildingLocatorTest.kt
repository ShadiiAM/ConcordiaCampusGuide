package com.example.campusguide.ui.map.utils

import com.example.campusguide.ui.map.utils.BuildingLocator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class BuildingLocatorTest {

    private fun mockPolygon(
        outer: List<LatLng>,
        holes: List<List<LatLng>> = emptyList()
    ): Polygon {
        val poly = mock(Polygon::class.java)
        `when`(poly.points).thenReturn(outer)
        `when`(poly.holes).thenReturn(holes)
        return poly
    }

    @Test
    fun findBuilding_pointInsideOuter_returnsHitWithProps() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        val props = JSONObject().put("building-name", "HALL")
        val locator = BuildingLocator(
            polygonsById = linkedMapOf("HALL" to listOf(poly)),
            propsById = mapOf("HALL" to props),
            geodesic = true
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))

        assertNotNull(hit)
        assertEquals("HALL", hit!!.id)
        assertNotNull(hit.properties)
        assertEquals("HALL", hit.properties!!.getString("building-name"))
    }

    @Test
    fun findBuilding_pointOutsideAll_returnsNull() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = true
        )

        val hit = locator.findBuilding(LatLng(2.0, 2.0))

        assertNull(hit)
    }

    @Test
    fun pointInBuilding_delegatesToFindBuilding() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = true
        )

        assertTrue(locator.pointInBuilding(LatLng(0.5, 0.5)))
        assertFalse(locator.pointInBuilding(LatLng(5.0, 5.0)))
    }

    @Test
    fun findBuilding_pointInsideHole_returnsNull() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 2.0),
            LatLng(2.0, 2.0),
            LatLng(2.0, 0.0)
        )
        val hole = listOf(
            LatLng(0.75, 0.75),
            LatLng(0.75, 1.25),
            LatLng(1.25, 1.25),
            LatLng(1.25, 0.75)
        )
        val poly = mockPolygon(outer, holes = listOf(hole))

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = true
        )

        // Inside hole -> should NOT count as inside building
        val hit = locator.findBuilding(LatLng(1.0, 1.0))

        assertNull(hit)
    }

    @Test
    fun findBuilding_multipleBuildings_returnsFirstMatchingInInsertionOrder() {
        val outerA = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val outerB = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 2.0),
            LatLng(2.0, 2.0),
            LatLng(2.0, 0.0)
        )

        val polyA = mockPolygon(outerA)
        val polyB = mockPolygon(outerB)

        // Point (0.5, 0.5) is inside BOTH; linkedMapOf preserves insertion order
        val locator = BuildingLocator(
            polygonsById = linkedMapOf(
                "A" to listOf(polyA),
                "B" to listOf(polyB)
            ),
            propsById = emptyMap(),
            geodesic = true
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))

        assertNotNull(hit)
        assertEquals("A", hit!!.id)
    }

    @Test
    fun findBuilding_propsMissing_stillReturnsHitWithNullProperties() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(), // missing
            geodesic = true
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))

        assertNotNull(hit)
        assertEquals("A", hit!!.id)
        assertNull(hit.properties)
    }

    @Test
    fun findBuilding_geodesicFalse_stillFindsSameForSimpleSquare() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = false
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))

        assertNotNull(hit)
        assertEquals("A", hit!!.id)
    }
}
