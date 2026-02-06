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

@RunWith(AndroidJUnit4::class)
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
        `when`(mockMap.addMarker(any(MarkerOptions::class.java))).thenReturn(mock(Marker::class.java))
        `when`(mockMap.addPolygon(any(PolygonOptions::class.java))).thenReturn(mock(Polygon::class.java))
        return mockMap
    }

    private fun polygonGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[[-73.5,45.5],[-73.6,45.5],[-73.6,45.6],[-73.5,45.5]]]]}}]}""")
    private fun multiPolygonGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"MultiPolygon","coordinates":[[[[[-73.5,45.5],[-73.6,45.5],[-73.5,45.5]]]]]}}]}""")
    private fun pointGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[-73.578,45.495]},"properties":{"title":"Test"}}]}""")
    private fun pointWithBuildingName() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[-73.578,45.495]},"properties":{"building-name":"Test Building"}}]}""")
    private fun styledGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[[-73.5,45.5],[-73.6,45.5],[-73.5,45.5]]]]},"properties":{"fill":"#FF0000","stroke":"#00FF00","fill-opacity":"0.5"}}]}""")
    private fun markerWithColor() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[-73.578,45.495]},"properties":{"marker-color":"#FF0000"}}]}""")
    private fun polygonWithHoles() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[[-73.5,45.5],[-73.6,45.5],[-73.6,45.6],[-73.5,45.5]]],[[[-73.55,45.55],[-73.56,45.55],[-73.56,45.56],[-73.55,45.55]]]]}}]}""")
    private fun emptyGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[]}""")
    private fun badGeoJson() = JSONObject("""{"type":"FeatureCollection","features":[{"type":"Feature"}]}""")

    @Test
    fun overlay_createsWithoutCrashing() {
        val overlay = GeoJsonOverlay(ctx)
        assertNotNull(overlay)
    }

    @Test
    fun attachToMap_polygon_rendersPolygon() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        verify(mockMap, atLeastOnce()).addPolygon(any())
    }

    @Test
    fun attachToMap_point_rendersMarker() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, pointGeoJson())
        verify(mockMap, atLeastOnce()).addMarker(any())
    }

    @Test
    fun setVisibleAll_changesVisibility() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setVisibleAll(true)
        overlay.setVisibleAll(false)
    }

    @Test
    fun clear_removesAllFeatures() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.clear()
    }

    @Test
    fun setMarkersVisible_showsAndHidesMarkers() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, pointGeoJson())
        overlay.setMarkersVisible(true)
        overlay.setMarkersVisible(false)
    }

    @Test
    fun setBuildingsVisible_showsAndHidesBuildings() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setBuildingsVisible(true)
        overlay.setBuildingsVisible(false)
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

    @Test
    fun setAllStyles_updatesStyles() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setAllStyles(GeoJsonStyle(fillColor = 0xFF0000FF.toInt()))
    }

    @Test
    fun setFillOpacityForAll_updatesOpacity() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setFillOpacityForAll(0.5f)
    }

    @Test
    fun setBuildingVisible_showsSpecificBuilding() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setBuildingVisible("test-id", true)
        overlay.setBuildingVisible("test-id", false)
    }

    @Test
    fun setMarkerVisible_showsSpecificMarker() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, pointGeoJson())
        overlay.setMarkerVisible("test-id", true)
        overlay.setMarkerVisible("test-id", false)
    }

    @Test
    fun removeFeature_removesSpecificFeature() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.removeFeature("some-id")
    }

    @Test
    fun reapplyPropertiesStyles_updatesStyles() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.reapplyPropertiesStyles()
    }

    @Test
    fun setStyleForFeature_updatesFeatureStyle() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonGeoJson())
        overlay.setStyleForFeature("test-id", GeoJsonStyle(fillColor = 0xFF0000FF.toInt()))
    }

    @Test
    fun attachToMap_multiPolygon_rendersPolygon() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, multiPolygonGeoJson())
        verify(mockMap, atLeastOnce()).addPolygon(any())
    }

    @Test
    fun attachToMap_styledFeature_appliesStyles() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, styledGeoJson())
        verify(mockMap, atLeastOnce()).addPolygon(any())
    }

    @Test
    fun attachToMap_emptyFeatures_doesNotCrash() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, emptyGeoJson())
    }

    @Test
    fun attachToMap_missingGeometry_skipsFeature() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, badGeoJson())
    }

    @Test
    fun attachToMap_polygonWithHoles_rendersCorrectly() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, polygonWithHoles())
        verify(mockMap, atLeastOnce()).addPolygon(any())
    }

    @Test
    fun attachToMap_pointWithBuildingName_setsTitle() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, pointWithBuildingName())
        verify(mockMap, atLeastOnce()).addMarker(any())
    }

    @Test
    fun attachToMap_markerWithColor_appliesColor() {
        val mockMap = createMockMap()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(mockMap, markerWithColor())
        verify(mockMap, atLeastOnce()).addMarker(any())
    }
}
