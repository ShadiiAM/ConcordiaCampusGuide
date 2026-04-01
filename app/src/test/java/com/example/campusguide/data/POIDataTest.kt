package com.example.campusguide.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.DayOfWeek
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class POIDataTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun cafe(
        name: String = "Test Cafe",
        address: String = "123 Test St",
        rating: Double = 4.0,
        latLng: LatLng = LatLng(45.497, -73.578),
        category: POIType = POIType.Cafe
    ) = OutsidePOI(
        name = name,
        description = "A test POI",
        category = category,
        workingHours = WorkingHours(8.0, 18.0, emptyList()),
        rating = rating,
        latLng = latLng,
        address = address
    )

    private val sgwLocation = LatLng(45.497, -73.578)

    // ── filterPOI – distance ──────────────────────────────────────────────────

    @Test
    fun `filterPOI passes when distanceLimit is 0 (no limit)`() {
        val poi = cafe(latLng = LatLng(80.0, 80.0)) // very far away
        val filters = POIFilterValues(distanceLimit = 0.0f)
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI passes when POI is within distance limit`() {
        val nearbyPoi = cafe(latLng = sgwLocation)
        val filters = POIFilterValues(distanceLimit = 100.0f) // 100 m
        assertTrue(nearbyPoi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI fails when POI is outside distance limit`() {
        val farPoi = cafe(latLng = LatLng(45.600, -73.700)) // ~15 km away
        val filters = POIFilterValues(distanceLimit = 100.0f)
        assertFalse(farPoi.filterPOI(sgwLocation, filters))
    }

    // ── filterPOI – category ─────────────────────────────────────────────────

    @Test
    fun `filterPOI passes when categoriesIncluded is empty`() {
        val poi = cafe(category = POIType.Restaurant)
        val filters = POIFilterValues(categoriesIncluded = emptySet())
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI passes when POI category is in the included set`() {
        val poi = cafe(category = POIType.Metro)
        val filters = POIFilterValues(categoriesIncluded = setOf(POIType.Metro, POIType.Cafe))
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI fails when POI category is not in the included set`() {
        val poi = cafe(category = POIType.Park)
        val filters = POIFilterValues(categoriesIncluded = setOf(POIType.Cafe, POIType.Restaurant))
        assertFalse(poi.filterPOI(sgwLocation, filters))
    }

    // ── filterPOI – rating ────────────────────────────────────────────────────

    @Test
    fun `filterPOI passes when rating equals minimum required rating`() {
        val poi = cafe(rating = 3.5)
        val filters = POIFilterValues(rating = 3.5)
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI passes when rating is above minimum required`() {
        val poi = cafe(rating = 4.8)
        val filters = POIFilterValues(rating = 3.0)
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI fails when rating is below minimum required`() {
        val poi = cafe(rating = 2.5)
        val filters = POIFilterValues(rating = 3.0)
        assertFalse(poi.filterPOI(sgwLocation, filters))
    }

    // ── filterPOI – combined ──────────────────────────────────────────────────

    @Test
    fun `filterPOI fails when only category does not match`() {
        val poi = cafe(category = POIType.Museum)
        val filters = POIFilterValues(
            distanceLimit = 0.0f,
            rating = 0.0,
            categoriesIncluded = setOf(POIType.Cafe)
        )
        assertFalse(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI fails when only rating is too low`() {
        val poi = cafe(rating = 1.0)
        val filters = POIFilterValues(rating = 4.0, categoriesIncluded = emptySet())
        assertFalse(poi.filterPOI(sgwLocation, filters))
    }

    @Test
    fun `filterPOI passes when all filters are satisfied`() {
        val poi = cafe(
            category = POIType.Cafe,
            rating = 4.5,
            latLng = sgwLocation
        )
        val filters = POIFilterValues(
            distanceLimit = 200.0f,
            rating = 4.0,
            categoriesIncluded = setOf(POIType.Cafe)
        )
        assertTrue(poi.filterPOI(sgwLocation, filters))
    }

    // ── score() ───────────────────────────────────────────────────────────────

    @Test
    fun `score returns 0 for empty query`() {
        val poi = cafe(name = "Bar Caffettiera")
        assertEquals(0, poi.score(""))
        assertEquals(0, poi.score("   "))
    }

    @Test
    fun `score returns 100 for exact full name match`() {
        val poi = cafe(name = "Bar Caffettiera")
        assertEquals(100, poi.score("bar caffettiera"))
    }

    @Test
    fun `score returns 80 when name starts with query`() {
        val poi = cafe(name = "Bar Caffettiera")
        assertEquals(80, poi.score("bar caf"))
    }

    @Test
    fun `score returns 60 when any word in name starts with query`() {
        val poi = cafe(name = "Bar Caffettiera")
        assertEquals(60, poi.score("caff"))
    }

    @Test
    fun `score returns 50 when category contains query`() {
        val poi = cafe(name = "Downtown Spot", category = POIType.Cafe)
        assertEquals(50, poi.score("cafe"))
    }

    @Test
    fun `score returns 40 when name contains query as substring`() {
        val poi = cafe(name = "The Grand Bistro")
        assertEquals(40, poi.score("rand"))
    }

    @Test
    fun `score returns 30 when address contains query`() {
        val poi = cafe(name = "Xyz Shop", address = "123 Sherbrooke St W")
        assertEquals(30, poi.score("sherbrooke"))
    }

    @Test
    fun `score returns 20 when query contains category`() {
        val poi = cafe(name = "Unrelated Name", category = POIType.Metro)
        assertEquals(20, poi.score("metro station nearby"))
    }

    @Test
    fun `score returns 0 when nothing matches`() {
        val poi = cafe(name = "Maple Leaf Cafe", address = "42 Oak Ave")
        assertEquals(0, poi.score("zzzzz"))
    }

    @Test
    fun `score is case insensitive`() {
        val poi = cafe(name = "Grand Bistro")
        assertEquals(poi.score("grand bistro"), poi.score("GRAND BISTRO"))
    }

    @Test
    fun `score trims whitespace from query`() {
        val poi = cafe(name = "Grand Bistro")
        assertEquals(100, poi.score("  grand bistro  "))
    }

    // ── POIFilterValues defaults ──────────────────────────────────────────────

    @Test
    fun `POIFilterValues has correct defaults`() {
        val filters = POIFilterValues()
        assertEquals(0.0f, filters.distanceLimit)
        assertEquals(0.0, filters.rating, 0.0)
        assertTrue(filters.categoriesIncluded.isEmpty())
    }

    // ── WorkingHours ──────────────────────────────────────────────────────────

    @Test
    fun `WorkingHours stores opening and closing hours`() {
        val hours = WorkingHours(9.0, 17.5, emptyList())
        assertEquals(9.0, hours.openingHour, 0.0)
        assertEquals(17.5, hours.closingHour, 0.0)
        assertTrue(hours.closedDays.isEmpty())
    }

    @Test
    fun `WorkingHours stores closed days`() {
        val closed = listOf(DayOfWeek.Monday, DayOfWeek.Sunday)
        val hours = WorkingHours(10.0, 18.0, closed)
        assertEquals(2, hours.closedDays.size)
        assertTrue(DayOfWeek.Monday in hours.closedDays)
        assertTrue(DayOfWeek.Sunday in hours.closedDays)
    }

    // ── ALL_POI ───────────────────────────────────────────────────────────────

    @Test
    fun `ALL_POI is not empty`() {
        assertTrue(ALL_POI.isNotEmpty())
    }

    @Test
    fun `ALL_POI contains all POIType categories`() {
        val categories = ALL_POI.map { it.category }.toSet()
        assertEquals(POIType.entries.toSet(), categories)
    }

    @Test
    fun `ALL_POI entries have positive ratings`() {
        for (poi in ALL_POI) {
            assertTrue("${poi.name} has non-positive rating", poi.rating > 0)
        }
    }

    @Test
    fun `ALL_POI entries have non-blank names and addresses`() {
        for (poi in ALL_POI) {
            assertTrue("${poi.name} has blank name", poi.name.isNotBlank())
            assertTrue("${poi.name} has blank address", poi.address.isNotBlank())
        }
    }

    // ── fullPOISuggestions() ──────────────────────────────────────────────────

    @Test
    fun `fullPOISuggestions returns empty list for empty query`() {
        assertEquals(emptyList<Suggestion>(), fullPOISuggestions(""))
        assertEquals(emptyList<Suggestion>(), fullPOISuggestions("   "))
    }

    @Test
    fun `fullPOISuggestions returns empty list for no match`() {
        assertTrue(fullPOISuggestions("zzzzzzzzz").isEmpty())
    }

    @Test
    fun `fullPOISuggestions returns results sorted by descending score`() {
        val results = fullPOISuggestions("cafe")
        assertTrue(results.isNotEmpty())
        // All results must be OutsidePOI with score > 0
        results.filterIsInstance<OutsidePOI>().forEach { poi ->
            assertTrue(poi.score("cafe") > 0)
        }
    }

    @Test
    fun `fullPOISuggestions respects max parameter`() {
        val results = fullPOISuggestions("a", max = 2)
        assertTrue(results.size <= 2)
    }

    @Test
    fun `fullPOISuggestions returns exact match as first result`() {
        val first = fullPOISuggestions("Bar Caffettiera").firstOrNull()
        assertNotNull(first)
        assertEquals("Bar Caffettiera", (first as OutsidePOI).name)
    }

    @Test
    fun `fullPOISuggestions excludes POIs with score 0`() {
        val results = fullPOISuggestions("cafe")
        results.filterIsInstance<OutsidePOI>().forEach { poi ->
            assertNotEquals(0, poi.score("cafe"))
        }
    }
}
