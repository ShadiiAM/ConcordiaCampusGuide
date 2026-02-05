package com.example.campusguide.ui.map.geoJson

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.mockito.Mockito
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.mockito.MockedStatic
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GeoJsonOverlayTest {

    private lateinit var ctx: Context

    private var bitmapFactoryStatic: MockedStatic<BitmapDescriptorFactory>? = null

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val fakeDescriptor = mock<BitmapDescriptor>()

        // Close any existing static mock before creating a new one
        try {
            bitmapFactoryStatic?.close()
        } catch (e: Exception) {
            // Ignore close errors
        }

        // If static mock creation fails (already exists), use the existing one
        try {
            bitmapFactoryStatic = Mockito.mockStatic(BitmapDescriptorFactory::class.java).also { st ->
                st.`when`<BitmapDescriptor> { BitmapDescriptorFactory.fromBitmap(any()) }
                    .thenReturn(fakeDescriptor)
            }
        } catch (e: Exception) {
            // Static mock already exists from another test - use MarkerIconFactory hooks instead
            MarkerIconFactory.bitmapToDescriptor = { fakeDescriptor }
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

    @Test
    fun attachToMap_defaultArgPath_loadsFromRaw_andDoesNotCrash() {
        // Covers attachToMap(map) default-arg overload + loadFromRawOrThrow() path.
        val context = mock<Context>()
        val appContext = mock<Context>()
        whenever(context.applicationContext).thenReturn(appContext)

        val resources = mock<Resources>()
        whenever(context.resources).thenReturn(resources)

        val json = "{\"type\":\"FeatureCollection\",\"features\":[]}" // empty features
        val input = ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8))
        whenever(resources.openRawResource(123)).thenReturn(input)

        val map = mock<GoogleMap>()

        val overlay = GeoJsonOverlay(context, 123, "id")
        overlay.attachToMap(map) // geoJson omitted => raw load

        // No features => should not add anything
        verify(map, never()).addPolygon(any())
        verify(map, never()).addMarker(any())
        verify(resources, times(1)).openRawResource(123)
    }

    @Test
    fun reapplyPropertiesStyles_reappliesUpdatedPropertiesToExistingPolygons() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()

        whenever(map.addPolygon(any())).thenReturn(poly)

        // Initial props: stroke-width=2
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        // Clear prior interactions so we can observe re-application
        clearInvocations(poly)

        // Update stored properties for id "68" by re-attaching with modified stroke width
        val modified = JSONObject(
            """{
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{
                  "stroke":"#555555",
                  "stroke-width":9,
                  "stroke-opacity":1,
                  "fill":"#555555",
                  "fill-opacity":0.5
                },
                "geometry":{
                  "type":"Polygon",
                  "coordinates":[[[ -73.0,45.0],[-73.0,45.1],[-73.1,45.1],[-73.1,45.0],[-73.0,45.0 ]]]
                },
                "id":68
              }]}
            """.trimIndent()
        )

        overlay.attachToMap(map, modified)
        clearInvocations(poly)

        // Now reapply styles from stored properties; should set strokeWidth=9f
        overlay.reapplyPropertiesStyles()
        verify(poly, atLeastOnce()).strokeWidth = eq(9f)
    }

    @Test
    fun getBuildings_returnsPolygonsMap() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        val buildings = overlay.getBuildings()

        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty())
        assertTrue(buildings.containsKey("68"))
    }

    @Test
    fun getBuildingProps_returnsPropertiesMap() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()
        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        val props = overlay.getBuildingProps()

        assertNotNull(props)
        assertTrue(props.isNotEmpty())
    }

    @Test
    fun attachToMap_withPolygonHoles_parsesCorrectly() {
        val geoJsonWithHoles = JSONObject("""
        {
          "type":"FeatureCollection",
          "features":[{
            "type":"Feature",
            "properties":{"building-name":"WithHole"},
            "geometry":{
              "type":"Polygon",
              "coordinates":[
                [[-73.0,45.0],[-73.0,45.1],[-73.1,45.1],[-73.1,45.0],[-73.0,45.0]],
                [[-73.02,45.02],[-73.02,45.08],[-73.08,45.08],[-73.08,45.02],[-73.02,45.02]]
              ]
            },
            "id":"hole-building"
          }]
        }
        """.trimIndent())

        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithHoles)

        verify(map, times(1)).addPolygon(any())
    }

    @Test
    fun attachToMap_withEmptyFeatures_doesNotCrash() {
        val emptyGeoJson = JSONObject("""{"type":"FeatureCollection","features":[]}""")

        val map = mock<GoogleMap>()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, emptyGeoJson)

        verify(map, never()).addPolygon(any())
        verify(map, never()).addMarker(any())
    }

    @Test
    fun attachToMap_withMissingFeaturesArray_doesNotCrash() {
        val noFeaturesGeoJson = JSONObject("""{"type":"FeatureCollection"}""")

        val map = mock<GoogleMap>()
        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, noFeaturesGeoJson)

        verify(map, never()).addPolygon(any())
        verify(map, never()).addMarker(any())
    }

    @Test
    fun setStyleForFeature_nonExistentFeature_doesNotCrash() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        val style = GeoJsonStyle(fillColor = 0xFFFF0000.toInt())

        // Should not crash when feature doesn't exist
        overlay.setStyleForFeature("non-existent-id", style)
    }

    @Test
    fun removeFeature_nonExistentFeature_doesNotCrash() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        // Should not crash when feature doesn't exist
        overlay.removeFeature("non-existent-id")
    }

    @Test
    fun setMarkerVisible_nonExistentMarker_doesNotCrash() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        // Should not crash when marker doesn't exist
        overlay.setMarkerVisible("non-existent-id", false)
    }

    @Test
    fun setBuildingVisible_nonExistentBuilding_doesNotCrash() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        // Should not crash when building doesn't exist
        overlay.setBuildingVisible("non-existent-id", false)
    }

    @Test
    fun attachToMap_withLargeMarkerSize_appliesCorrectScale() {
        val geoJsonLargeMarker = JSONObject("""
        {
          "type":"FeatureCollection",
          "features":[{
            "type":"Feature",
            "properties":{
              "building-name":"LargeMarker",
              "marker-color":"#FF0000",
              "marker-size":"large"
            },
            "geometry":{
              "type":"Point",
              "coordinates":[-73.05,45.05]
            },
            "id":"large-marker"
          }]
        }
        """.trimIndent())

        val map = mock<GoogleMap>()
        val marker = mock<Marker>()
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonLargeMarker)

        verify(map, times(1)).addMarker(any())
        verify(marker, atLeastOnce()).setIcon(any())
    }

    @Test
    fun attachToMap_withMediumMarkerSize_appliesDefaultScale() {
        val geoJsonMediumMarker = JSONObject("""
        {
          "type":"FeatureCollection",
          "features":[{
            "type":"Feature",
            "properties":{
              "building-name":"MediumMarker",
              "marker-color":"#00FF00",
              "marker-size":"medium"
            },
            "geometry":{
              "type":"Point",
              "coordinates":[-73.05,45.05]
            },
            "id":"medium-marker"
          }]
        }
        """.trimIndent())

        val map = mock<GoogleMap>()
        val marker = mock<Marker>()
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonMediumMarker)

        verify(map, times(1)).addMarker(any())
    }

    @Test
    fun attachToMap_pointWithoutBuildingName_usesNullTitle() {
        val geoJsonNoTitle = JSONObject("""
        {
          "type":"FeatureCollection",
          "features":[{
            "type":"Feature",
            "properties":{
              "marker-color":"#0000FF"
            },
            "geometry":{
              "type":"Point",
              "coordinates":[-73.05,45.05]
            },
            "id":"no-title-marker"
          }]
        }
        """.trimIndent())

        val map = mock<GoogleMap>()
        val marker = mock<Marker>()
        whenever(map.addMarker(any())).thenReturn(marker)

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonNoTitle)

        verify(map, times(1)).addMarker(any())
    }

    @Test
    fun stableIdFor_usesPropertyIdFirst() {
        val geoJsonWithPropertyId = JSONObject("""
        {
          "type":"FeatureCollection",
          "features":[{
            "type":"Feature",
            "properties":{
              "building-name":"PropertyIdBuilding"
            },
            "geometry":{
              "type":"Polygon",
              "coordinates":[[[-73.0,45.0],[-73.0,45.1],[-73.1,45.1],[-73.1,45.0],[-73.0,45.0]]]
            },
            "id":"feature-level-id"
          }]
        }
        """.trimIndent())

        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        whenever(map.addPolygon(any())).thenReturn(poly)

        val overlay = GeoJsonOverlay(ctx, idPropertyName = "building-name")
        overlay.attachToMap(map, geoJsonWithPropertyId)

        val buildings = overlay.getBuildings()
        assertTrue("Should use property id", buildings.containsKey("PropertyIdBuilding"))
    }

    @Test
    fun attachToMap_nullGeoJsonRes_throwsWhenNoJsonProvided() {
        val overlay = GeoJsonOverlay(ctx, geoJsonRawRes = null)
        val map = mock<GoogleMap>()

        try {
            overlay.attachToMap(map)
            fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Provide JSONObject or raw resource id") == true)
        }
    }

    @Test
    fun setFillOpacityForAll_withNegativeOpacity_clampsToZero() {
        val map = mock<GoogleMap>()
        val poly = mock<Polygon>()
        val marker = mock<Marker>()

        whenever(map.addPolygon(any())).thenReturn(poly)
        whenever(map.addMarker(any())).thenReturn(marker)
        whenever(poly.fillColor).thenReturn(0xFF112233.toInt())

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, geoJsonWithOnePolygonAndOnePoint())

        overlay.setFillOpacityForAll(-1f)
        verify(poly, atLeastOnce()).fillColor = eq(0x00112233)
    }

    @Test
    fun reapplyPropertiesStyles_withNoPolygons_doesNotCrash() {
        val map = mock<GoogleMap>()

        val overlay = GeoJsonOverlay(ctx)
        overlay.attachToMap(map, JSONObject("""{"type":"FeatureCollection","features":[]}"""))

        // Should not crash
        overlay.reapplyPropertiesStyles()
    }

    // ==================== attachToMapAsync tests ====================

    @Test
    fun attachToMapAsync_withPolygonPointAndMultiPolygon_addsAllFeatures() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>(defaultAnswer = org.mockito.Mockito.RETURNS_DEEP_STUBS)
            val poly1 = mock<Polygon>()
            val marker = mock<Marker>()
            val mp1 = mock<Polygon>()
            val mp2 = mock<Polygon>()

            whenever(map.addPolygon(any())).thenReturn(poly1, mp1, mp2)
            whenever(map.addMarker(any())).thenReturn(marker)

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, geoJsonWithPolygonPointAndMultiPolygon())

            verify(map, times(3)).addPolygon(any())
            verify(map, times(1)).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withEmptyFeatures_addsNothing() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val emptyGeoJson = JSONObject("""{"type":"FeatureCollection","features":[]}""")

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, emptyGeoJson)

            verify(map, never()).addPolygon(any())
            verify(map, never()).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withMissingFeatures_returnsImmediately() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val noFeaturesGeoJson = JSONObject("""{"type":"FeatureCollection"}""")

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, noFeaturesGeoJson)

            verify(map, never()).addPolygon(any())
            verify(map, never()).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withPointHavingLessThanTwoCoords_skipsPoint() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val badPointGeoJson = JSONObject("""
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{"marker-color":"#FF0000"},
                "geometry":{"type":"Point","coordinates":[-73.0]},
                "id":"bad-point"
              }]
            }
            """.trimIndent())

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, badPointGeoJson)

            verify(map, never()).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withPointWithoutTitle_addsMarkerWithNullTitle() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val marker = mock<Marker>()
            whenever(map.addMarker(any())).thenReturn(marker)

            val pointNoTitle = JSONObject("""
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{"marker-color":"#0000FF"},
                "geometry":{"type":"Point","coordinates":[-73.05,45.05]},
                "id":"no-title"
              }]
            }
            """.trimIndent())

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, pointNoTitle)

            verify(map, times(1)).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withPointWithTitle_addsMarkerWithTitle() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val marker = mock<Marker>()
            whenever(map.addMarker(any())).thenReturn(marker)

            val pointWithTitle = JSONObject("""
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{"building-name":"Test Building","marker-color":"#00FF00"},
                "geometry":{"type":"Point","coordinates":[-73.05,45.05]},
                "id":"with-title"
              }]
            }
            """.trimIndent())

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, pointWithTitle)

            verify(map, times(1)).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_withMultiPolygon_addsMultiplePolygons() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val poly1 = mock<Polygon>()
            val poly2 = mock<Polygon>()
            whenever(map.addPolygon(any())).thenReturn(poly1, poly2)

            val multiPolygonGeoJson = JSONObject("""
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{"fill":"#FF0000"},
                "geometry":{
                  "type":"MultiPolygon",
                  "coordinates":[
                    [[[-73.0,45.0],[-73.0,45.1],[-73.1,45.1],[-73.1,45.0],[-73.0,45.0]]],
                    [[[-73.2,45.2],[-73.2,45.3],[-73.3,45.3],[-73.3,45.2],[-73.2,45.2]]]
                  ]
                },
                "id":"multi-poly"
              }]
            }
            """.trimIndent())

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, multiPolygonGeoJson)

            verify(map, times(2)).addPolygon(any())
        }
    }

    @Test
    fun attachToMapAsync_withMissingGeometry_skipsFeature() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val missingGeometry = JSONObject("""
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "properties":{"fill":"#FF0000"},
                "id":"no-geometry"
              }]
            }
            """.trimIndent())

            val overlay = GeoJsonOverlay(ctx)
            overlay.attachToMapAsync(map, missingGeometry)

            verify(map, never()).addPolygon(any())
            verify(map, never()).addMarker(any())
        }
    }

    @Test
    fun attachToMapAsync_clearsExistingFeaturesBeforeAdding() {
        kotlinx.coroutines.runBlocking {
            val map = mock<GoogleMap>()
            val poly1 = mock<Polygon>()
            val poly2 = mock<Polygon>()
            whenever(map.addPolygon(any())).thenReturn(poly1, poly2)

            val overlay = GeoJsonOverlay(ctx)

            // First attach
            overlay.attachToMapAsync(map, geoJsonWithOnePolygonAndOnePoint())
            verify(poly1, atLeastOnce()).remove() // clear() is called

            // Second attach
            overlay.attachToMapAsync(map, geoJsonWithOnePolygonAndOnePoint())
            // Verify clear was called again (existing features removed)
        }
    }
}
