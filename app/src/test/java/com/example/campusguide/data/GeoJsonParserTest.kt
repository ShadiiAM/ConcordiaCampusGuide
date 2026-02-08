package com.example.campusguide.data

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/**
 * Fast unit tests for GeoJsonParser - no Robolectric overhead.
 * These tests run in milliseconds instead of seconds.
 */
class GeoJsonParserTest {

    @Test
    fun parseCoordinates_polygon_returnsLatLngList() {
        val geometry = JSONObject("""
            {
                "type": "Polygon",
                "coordinates": [
                    [
                        [-73.5, 45.5],
                        [-73.6, 45.5],
                        [-73.6, 45.6],
                        [-73.5, 45.6],
                        [-73.5, 45.5]
                    ]
                ]
            }
        """)

        val result = GeoJsonParser.parseCoordinates(geometry)

        assertEquals(5, result.size)
        assertEquals(LatLng(45.5, -73.5), result[0])
        assertEquals(LatLng(45.5, -73.6), result[1])
    }

    @Test
    fun parseCoordinates_multiPolygon_returnsFirstPolygon() {
        val geometry = JSONObject("""
            {
                "type": "MultiPolygon",
                "coordinates": [
                    [
                        [[-73.5, 45.5], [-73.6, 45.6], [-73.5, 45.5]]
                    ],
                    [
                        [[-74.0, 46.0], [-74.1, 46.1], [-74.0, 46.0]]
                    ]
                ]
            }
        """)

        val result = GeoJsonParser.parseCoordinates(geometry)

        assertEquals(3, result.size)
        assertEquals(LatLng(45.5, -73.5), result[0])
    }

    @Test
    fun parseCoordinates_point_returnsSingleLatLng() {
        val geometry = JSONObject("""
            {
                "type": "Point",
                "coordinates": [-73.578, 45.495]
            }
        """)

        val result = GeoJsonParser.parseCoordinates(geometry)

        assertEquals(1, result.size)
        assertEquals(LatLng(45.495, -73.578), result[0])
    }

    @Test
    fun parseCoordinates_nullGeometry_returnsEmptyList() {
        val result = GeoJsonParser.parseCoordinates(null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseCoordinates_missingCoordinates_returnsEmptyList() {
        val geometry = JSONObject("""{"type": "Polygon"}""")
        val result = GeoJsonParser.parseCoordinates(geometry)
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseCoordinates_unknownType_returnsEmptyList() {
        val geometry = JSONObject("""
            {
                "type": "UnknownType",
                "coordinates": [[[-73.5, 45.5]]]
            }
        """)

        val result = GeoJsonParser.parseCoordinates(geometry)
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseCoordinates_invalidNumbers_skipsInvalidPoints() {
        val geometry = JSONObject("""
            {
                "type": "Polygon",
                "coordinates": [
                    [
                        [-73.5, 45.5],
                        ["invalid", "data"],
                        [-73.6, 45.6]
                    ]
                ]
            }
        """)

        val result = GeoJsonParser.parseCoordinates(geometry)

        // Should skip invalid point
        assertEquals(2, result.size)
        assertEquals(LatLng(45.5, -73.5), result[0])
        assertEquals(LatLng(45.6, -73.6), result[1])
    }

    @Test
    fun hasValidGeometry_withValidPolygon_returnsTrue() {
        val feature = JSONObject("""
            {
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[-73.5, 45.5], [-73.6, 45.6]]]
                }
            }
        """)

        assertTrue(GeoJsonParser.hasValidGeometry(feature))
    }

    @Test
    fun hasValidGeometry_missingGeometry_returnsFalse() {
        val feature = JSONObject("""{"properties": {}}""")
        assertFalse(GeoJsonParser.hasValidGeometry(feature))
    }

    @Test
    fun hasValidGeometry_missingType_returnsFalse() {
        val feature = JSONObject("""
            {
                "geometry": {
                    "coordinates": [[[-73.5, 45.5]]]
                }
            }
        """)

        assertFalse(GeoJsonParser.hasValidGeometry(feature))
    }

    @Test
    fun hasValidGeometry_emptyCoordinates_returnsFalse() {
        val feature = JSONObject("""
            {
                "geometry": {
                    "type": "Polygon",
                    "coordinates": []
                }
            }
        """)

        assertFalse(GeoJsonParser.hasValidGeometry(feature))
    }

    @Test
    fun extractTitle_withTitle_returnsTitle() {
        val feature = JSONObject("""
            {
                "properties": {
                    "title": "Test Building"
                }
            }
        """)

        assertEquals("Test Building", GeoJsonParser.extractTitle(feature))
    }

    @Test
    fun extractTitle_noProperties_returnsNull() {
        val feature = JSONObject("""{"geometry": {}}""")
        assertNull(GeoJsonParser.extractTitle(feature))
    }

    @Test
    fun extractTitle_noTitle_returnsNull() {
        val feature = JSONObject("""{"properties": {"name": "Other"}}""")
        assertNull(GeoJsonParser.extractTitle(feature))
    }

    @Test
    fun extractTitle_emptyTitle_returnsNull() {
        val feature = JSONObject("""{"properties": {"title": ""}}""")
        assertNull(GeoJsonParser.extractTitle(feature))
    }
}
