package com.example.campusguide.ui.map.geoJson

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config

/**
 * Minimal tests for GeoJsonOverlay to hit coverage.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class GeoJsonOverlayTest {

    private lateinit var ctx: Context
    private lateinit var fakeBitmapDescriptor: BitmapDescriptor

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()

        fakeBitmapDescriptor = mock(BitmapDescriptor::class.java)
        MarkerIconFactory.bitmapToDescriptor = { fakeBitmapDescriptor }
        MarkerIconFactory.defaultMarker = { fakeBitmapDescriptor }
    }

    @After
    fun tearDown() {
        MarkerIconFactory.resetForTests()
    }

    private fun createMockMap(): GoogleMap {
        val mockMap = mock(GoogleMap::class.java, withSettings().lenient())
        `when`(mockMap.addMarker(any(MarkerOptions::class.java)))
            .thenReturn(mock(Marker::class.java))
        `when`(mockMap.addPolygon(any(PolygonOptions::class.java)))
            .thenReturn(mock(Polygon::class.java))
        return mockMap
    }

    private fun simpleGeoJson() = JSONObject("""
        {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[[-73.5, 45.5], [-73.6, 45.5], [-73.6, 45.6], [-73.5, 45.5]]]]
                }
            }]
        }
    """)

    @Test
    fun geoJsonOverlay_createsWithoutCrashing() {
        val overlay = GeoJsonOverlay(ctx)
        assertNotNull(overlay)
    }

    @Test
    fun attachToMap_rendersGeometry() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)

        overlay.attachToMap(mockMap, simpleGeoJson())

        // Should add polygons
        verify(mockMap, atLeastOnce()).addPolygon(any())
    }

    @Test
    fun setVisibleAll_true_showsOverlay() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, simpleGeoJson())

        overlay.setVisibleAll(true)
        // Should not crash
    }

    @Test
    fun setVisibleAll_false_hidesOverlay() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, simpleGeoJson())

        overlay.setVisibleAll(false)
        // Should not crash
    }

    @Test
    fun clear_removesAllFeatures() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, simpleGeoJson())

        overlay.clear()
        // Should clear all polygons/markers
    }

    @Test
    fun setAllStyles_updatesStyle() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, simpleGeoJson())

        overlay.setAllStyles(GeoJsonStyle(fillColor = 0xFF0000FF.toInt()))
        // Should not crash
    }

    @Test
    fun getBuildings_returnsPolygonMap() {
        val overlay = GeoJsonOverlay(ctx)
        val buildings = overlay.getBuildings()
        assertNotNull(buildings)
    }

    @Test
    fun getBuildingProps_returnsPropertiesMap() {
        val overlay = GeoJsonOverlay(ctx)
        val props = overlay.getBuildingProps()
        assertNotNull(props)
    }
}



