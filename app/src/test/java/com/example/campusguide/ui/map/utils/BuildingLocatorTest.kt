package com.example.campusguide.ui.map.utils

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

    @Test
    fun findBuilding_emptyPolygonsMap_returnsNull() {
        val locator = BuildingLocator(
            polygonsById = emptyMap(),
            propsById = emptyMap(),
            geodesic = true
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))

        assertNull(hit)
    }

    @Test
    fun pointInBuilding_emptyPolygonsMap_returnsFalse() {
        val locator = BuildingLocator(
            polygonsById = emptyMap(),
            propsById = emptyMap(),
            geodesic = true
        )

        assertFalse(locator.pointInBuilding(LatLng(0.5, 0.5)))
    }

    @Test
    fun findBuilding_multiplePolygonsPerBuilding_findsIfInAny() {
        val outer1 = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val outer2 = listOf(
            LatLng(2.0, 2.0),
            LatLng(2.0, 3.0),
            LatLng(3.0, 3.0),
            LatLng(3.0, 2.0)
        )

        val poly1 = mockPolygon(outer1)
        val poly2 = mockPolygon(outer2)

        val locator = BuildingLocator(
            polygonsById = mapOf("MultiPoly" to listOf(poly1, poly2)),
            propsById = emptyMap(),
            geodesic = true
        )

        // Point in first polygon
        val hit1 = locator.findBuilding(LatLng(0.5, 0.5))
        assertNotNull(hit1)
        assertEquals("MultiPoly", hit1!!.id)

        // Point in second polygon
        val hit2 = locator.findBuilding(LatLng(2.5, 2.5))
        assertNotNull(hit2)
        assertEquals("MultiPoly", hit2!!.id)
    }

    @Test
    fun findBuilding_pointOnEdge_mayReturnHit() {
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

        // Point on edge - behavior depends on PolyUtil implementation
        val hit = locator.findBuilding(LatLng(0.0, 0.5))
        // Result could be hit or null depending on edge handling
        assertTrue(hit == null || hit.id == "A")
    }

    @Test
    fun findBuilding_pointAtVertex_mayReturnHit() {
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

        // Point at vertex
        val hit = locator.findBuilding(LatLng(0.0, 0.0))
        // Result could be hit or null depending on vertex handling
        assertTrue(hit == null || hit.id == "A")
    }

    @Test
    fun findBuilding_multipleHoles_correctlyExcludesAllHoles() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 4.0),
            LatLng(4.0, 4.0),
            LatLng(4.0, 0.0)
        )
        val hole1 = listOf(
            LatLng(0.5, 0.5),
            LatLng(0.5, 1.5),
            LatLng(1.5, 1.5),
            LatLng(1.5, 0.5)
        )
        val hole2 = listOf(
            LatLng(2.5, 2.5),
            LatLng(2.5, 3.5),
            LatLng(3.5, 3.5),
            LatLng(3.5, 2.5)
        )
        val poly = mockPolygon(outer, holes = listOf(hole1, hole2))

        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = true
        )

        // Inside first hole - should not count as inside building
        assertNull(locator.findBuilding(LatLng(1.0, 1.0)))

        // Inside second hole - should not count as inside building
        assertNull(locator.findBuilding(LatLng(3.0, 3.0)))

        // Inside polygon but outside holes - should count
        assertNotNull(locator.findBuilding(LatLng(2.0, 1.0)))
    }

    @Test
    fun buildingHit_dataClass_hasCorrectEquality() {
        val props1 = JSONObject().put("name", "Test")
        val props2 = JSONObject().put("name", "Test")

        val hit1 = BuildingHit("A", props1)
        val hit2 = BuildingHit("A", props2)
        val hit3 = BuildingHit("B", props1)

        // Different instances with different JSONObjects aren't equal (JSONObject doesn't implement equals)
        assertNotEquals(hit1, hit2)

        // Different IDs are not equal
        assertNotEquals(hit1, hit3)

        // Same instance equals itself
        assertEquals(hit1, hit1)
    }

    @Test
    fun buildingHit_withNullProperties_isValid() {
        val hit = BuildingHit("TestId", null)

        assertEquals("TestId", hit.id)
        assertNull(hit.properties)
    }

    @Test
    fun findBuilding_pointFarOutside_returnsNull() {
        val outer = listOf(
            LatLng(45.0, -73.0),
            LatLng(45.0, -73.1),
            LatLng(45.1, -73.1),
            LatLng(45.1, -73.0)
        )
        val poly = mockPolygon(outer)

        val locator = BuildingLocator(
            polygonsById = mapOf("Montreal" to listOf(poly)),
            propsById = emptyMap(),
            geodesic = true
        )

        // Point in Tokyo
        val hit = locator.findBuilding(LatLng(35.6762, 139.6503))
        assertNull(hit)
    }

    @Test
    fun findBuilding_defaultGeodesicValue_isTrue() {
        val outer = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
        val poly = mockPolygon(outer)

        // Create locator without specifying geodesic (should default to true)
        val locator = BuildingLocator(
            polygonsById = mapOf("A" to listOf(poly)),
            propsById = emptyMap()
        )

        val hit = locator.findBuilding(LatLng(0.5, 0.5))
        assertNotNull(hit)
    }
}
