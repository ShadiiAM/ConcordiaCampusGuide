package com.example.campusguide.ui.map.geoJson

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedConstruction
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.mockito.Mockito.mockConstruction
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.mockito.MockedStatic
import org.mockito.Mockito

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GeoJsonOverlayTest {

    private lateinit var ctx: Context


    private var markerIconFactoryConstruction: MockedConstruction<MarkerIconFactory>? = null

    private var bitmapFactoryStatic: MockedStatic<BitmapDescriptorFactory>? = null

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val fakeDescriptor = mock<BitmapDescriptor>()

        bitmapFactoryStatic = Mockito.mockStatic(BitmapDescriptorFactory::class.java).also { st ->
            st.`when`<BitmapDescriptor> { BitmapDescriptorFactory.fromBitmap(any()) }
                .thenReturn(fakeDescriptor)
        }
    }

    @After
    fun tearDown() {
        bitmapFactoryStatic?.close()
        bitmapFactoryStatic = null
    }

    // --------------------------
    // GeoJSON builders
    // --------------------------

    private fun geoJsonWithPolygonPointAndMultiPolygon(): JSONObject {
        val polygon = """
        {
          "type":"Feature",
          "properties":{
            "building-name":"SB",
            "stroke":"#555555",
            "stroke-width":2,
            "stroke-opacity":1,
            "fill":"#555555",
            "fill-opacity":0.5
          },
          "geometry":{
            "type":"Polygon",
            "coordinates":[[
              [-73.0,45.0],
              [-73.0,45.1],
              [-73.1,45.1],
              [-73.1,45.0],
              [-73.0,45.0]
            ]]
          },
          "id":68
        }
        """.trimIndent()

        val point = """
        {
          "type":"Feature",
          "properties":{
            "building-name":"SB",
            "marker-color":"#00bfff",
            "marker-size":"small"
          },
          "geometry":{
            "type":"Point",
            "coordinates":[-73.05,45.05]
          },
          "id":69
        }
        """.trimIndent()

        val multiPolygon = """
        {
          "type":"Feature",
          "properties":{
            "stroke":"#FF0000",
            "stroke-width":"3",
            "stroke-opacity":"0.5",
            "fill":"#00FF00",
            "fill-opacity":"0.25"
          },
          "geometry":{
            "type":"MultiPolygon",
            "coordinates":[
              [[
                [-73.2,45.2],
                [-73.2,45.3],
                [-73.3,45.3],
                [-73.3,45.2],
                [-73.2,45.2]
              ]],
              [[
                [-73.4,45.4],
                [-73.4,45.5],
                [-73.5,45.5],
                [-73.5,45.4],
                [-73.4,45.4]
              ]]
            ]
          },
          "id":70
        }
        """.trimIndent()

        return JSONObject("""{"type":"FeatureCollection","features":[$polygon,$point,$multiPolygon]}""")
    }

    private fun geoJsonWithOnePolygonAndOnePoint(): JSONObject {
        val polygon = """
        {
          "type":"Feature",
          "properties":{
            "stroke":"#555555",
            "stroke-width":2,
            "stroke-opacity":1,
            "fill":"#555555",
            "fill-opacity":0.5
          },
          "geometry":{
            "type":"Polygon",
            "coordinates":[[
              [-73.0,45.0],
              [-73.0,45.1],
              [-73.1,45.1],
              [-73.1,45.0],
              [-73.0,45.0]
            ]]
          },
          "id":68
        }
        """.trimIndent()

        val point = """
        {
          "type":"Feature",
          "properties":{
            "building-name":"SB",
            "marker-color":"#00bfff",
            "marker-size":"small"
          },
          "geometry":{
            "type":"Point",
            "coordinates":[-73.05,45.05]
          },
          "id":69
        }
        """.trimIndent()

        return JSONObject("""{"type":"FeatureCollection","features":[$polygon,$point]}""")
    }

    private fun geoJsonMissingGeometryAndUnknownTypeAndBadPoint(): JSONObject {
        val missingGeometry = """{"type":"Feature","properties":{"fill":"#FFFFFF"},"id":"a"}"""
        val unknownType = """
        {
          "type":"Feature",
          "properties":{"fill":"#FFFFFF"},
          "geometry":{"type":"LineString","coordinates":[[-73.0,45.0],[-73.1,45.1]]},
          "id":"b"
        }
        """.trimIndent()
        val badPoint = """
        {
          "type":"Feature",
          "properties":{"marker-color":"#00bfff"},
          "geometry":{"type":"Point","coordinates":[-73.0]},
          "id":"c"
        }
        """.trimIndent()

        return JSONObject("""{"type":"FeatureCollection","features":[$missingGeometry,$unknownType,$badPoint]}""")
    }

    // --------------------------
    // Tests
    // --------------------------

    @Test
    fun attachToMap_addsPolygonMarkerAndMultiPolygon_appliesNonColorStyles_andSetsMarkerIcon() {
        val map = mock<GoogleMap>()

        val poly1 = mock<Polygon>()
        val marker = mock<Marker>()
        val mp1 = mock<Polygon>()
        val mp2 = mock<Polygon>()

        whenever(map.addPolygon(any())).thenReturn(poly1, mp1, mp2)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithPolygonPointAndMultiPolygon())

        verify(map, times(3)).addPolygon(any())
        verify(map, times(1)).addMarker(any())

        // matches your production behavior (from your logs)
        verify(poly1, atLeastOnce()).strokeWidth = eq(2f)
        verify(poly1, atLeastOnce()).isClickable = eq(true)

        verify(mp1, atLeastOnce()).strokeWidth = eq(3f)
        verify(mp2, atLeastOnce()).strokeWidth = eq(3f)

        verify(marker, atLeastOnce()).setIcon(any())
    }

    @Test
    fun setFillOpacityForAll_clamps_andRewritesFillColor() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()

        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        whenever(poly.fillColor).thenReturn(0xFF112233.toInt())

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        overlay.setFillOpacityForAll(5f) // clamp to 1
        verify(poly, atLeastOnce()).fillColor = eq(0xFF112233.toInt())

        overlay.setFillOpacityForAll(0f) // alpha becomes 0
        verify(poly, atLeastOnce()).fillColor = eq(0x00112233)
    }

    @Test
    fun visibilityHelpers_toggleEverything() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        overlay.setVisibleAll(false)
        verify(poly, atLeastOnce()).isVisible = eq(false)
        verify(marker, atLeastOnce()).isVisible = eq(false)

        overlay.setMarkersVisible(true)
        verify(marker, atLeastOnce()).isVisible = eq(true)

        overlay.setMarkerVisible("69", false)
        verify(marker, atLeastOnce()).isVisible = eq(false)

        overlay.setBuildingsVisible(true)
        verify(poly, atLeastOnce()).isVisible = eq(true)

        overlay.setBuildingVisible("68", false)
        verify(poly, atLeastOnce()).isVisible = eq(false)
    }

    @Test
    fun removeFeature_removesPolygonAndMarker() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        overlay.removeFeature("68")
        verify(poly, atLeastOnce()).remove()

        overlay.removeFeature("69")
        verify(marker, atLeastOnce()).remove()
    }

    @Test
    fun clear_removesEverything_andSecondClearNoOps() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        overlay.clear()
        verify(poly, atLeastOnce()).remove()
        verify(marker, atLeastOnce()).remove()

        clearInvocations(poly, marker)
        overlay.clear()
        verifyNoInteractions(poly, marker)
    }

    @Test
    fun setStyleForFeature_appliesToPolygonAndMarker_withoutCrashing() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        val style = GeoJsonStyle(
            fillColor = 0xFF112233.toInt(),
            strokeColor = 0xFFFF0000.toInt(),
            strokeWidth = 7f,
            clickable = true,
            markerColor = 0xFFFF0000.toInt(),
            markerScale = 1f,
            markerAlpha = 0.3f,
            visible = true,
            zIndex = 10f
        )

        overlay.setStyleForFeature("68", style)
        verify(poly, atLeastOnce()).strokeWidth = eq(7f)
        verify(poly, atLeastOnce()).isClickable = eq(true)

        overlay.setStyleForFeature("69", style)
        verify(marker, atLeastOnce()).alpha = eq(0.3f)
        verify(marker, atLeastOnce()).zIndex = eq(10f)
        verify(marker, atLeastOnce()).isVisible = eq(true)
        verify(marker, atLeastOnce()).setIcon(any())
    }

    @Test
    fun setAllStyles_appliesToAllPolygonsAndMarkers_withoutCrashing() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        val style = GeoJsonStyle(
            fillColor = 0xFF112233.toInt(),
            strokeColor = 0xFFFF0000.toInt(),
            strokeWidth = 4f,
            clickable = false,
            markerColor = 0xFF00BFFF.toInt(),
            markerScale = 1f,
            markerAlpha = 1f,
            visible = false,
            zIndex = 2f
        )

        overlay.setAllStyles(style)

        verify(poly, atLeastOnce()).strokeWidth = eq(4f)
        verify(poly, atLeastOnce()).isClickable = eq(false)
        verify(poly, atLeastOnce()).isVisible = eq(false)
        verify(poly, atLeastOnce()).zIndex = eq(2f)

        verify(marker, atLeastOnce()).isVisible = eq(false)
        verify(marker, atLeastOnce()).zIndex = eq(2f)
        verify(marker, atLeastOnce()).alpha = eq(1f)
        verify(marker, atLeastOnce()).setIcon(any())
    }

    @Test
    fun attachToMap_ignoresMissingGeometry_unknownType_andBadPointCoords() {
        val map = mock<GoogleMap>()
        val overlay = GeoJsonOverlay(ctx)

        overlay.attachToMap(map, geoJsonMissingGeometryAndUnknownTypeAndBadPoint())

        verify(map, never()).addPolygon(any())
        verify(map, never()).addMarker(any())
    }
}
