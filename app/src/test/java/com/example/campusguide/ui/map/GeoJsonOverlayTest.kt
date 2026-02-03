package com.example.campusguide.ui.map

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.R
import com.example.campusguide.shadows.ShadowBitmapDescriptorFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.robolectric.annotation.Config

/**
 * Unit tests for GeoJsonOverlay
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], shadows = [ShadowBitmapDescriptorFactory::class])
class GeoJsonOverlayTest {

    private lateinit var context: Context
    private lateinit var mockMap: GoogleMap
    private lateinit var mockStyleMapper: GeoJsonStyleMapper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Create a lenient mock that allows all method calls
        mockMap = mock(GoogleMap::class.java, withSettings().lenient())

        // Mock the map methods that GeoJsonLayer uses - return lenient mocks
        `when`(mockMap.addPolygon(any())).thenAnswer {
            mock(Polygon::class.java, withSettings().lenient())
        }
        `when`(mockMap.addPolyline(any())).thenAnswer {
            mock(Polyline::class.java, withSettings().lenient())
        }
        `when`(mockMap.addMarker(any())).thenAnswer {
            mock(Marker::class.java, withSettings().lenient())
        }

        mockStyleMapper = mock(GeoJsonStyleMapper::class.java)
    }

    // ==================== Constructor tests ====================

    @Test
    fun geoJsonOverlay_canBeConstructed_withDefaultStyleMapper() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        assertNotNull(overlay)
    }

    @Test
    fun geoJsonOverlay_canBeConstructed_withCustomStyleMapper() {
        val customMapper = GeoJsonStyleMapper()
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings, customMapper)
        assertNotNull(overlay)
    }

    // ==================== addToMap tests ====================

    @Test
    fun addToMap_returnsGeoJsonLayer() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val layer = overlay.addToMap(mockMap, context)

        assertNotNull(layer)
    }

    @Test
    fun addToMap_addsLayerToMap() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val layer = overlay.addToMap(mockMap, context)

        assertNotNull(layer)
        assertTrue(layer.features.iterator().hasNext())
    }

    @Test
    fun addToMap_canBeCalledMultipleTimes() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val layer1 = overlay.addToMap(mockMap, context)
        assertNotNull(layer1)

        val layer2 = overlay.addToMap(mockMap, context)
        assertNotNull(layer2)
    }

    @Test
    fun addToMap_withDifferentGeoJsonFiles_loadsCorrectly() {
        val sgwOverlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val loyOverlay = GeoJsonOverlay(R.raw.loy_buildings)

        val sgwLayer = sgwOverlay.addToMap(mockMap, context)
        val loyLayer = loyOverlay.addToMap(mockMap, context)

        assertNotNull(sgwLayer)
        assertNotNull(loyLayer)
    }

    // ==================== removeFromMap tests ====================

    @Test
    fun removeFromMap_afterAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.removeFromMap()

        // Should not crash
    }

    @Test
    fun removeFromMap_withoutAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.removeFromMap()

        // Should not crash
    }

    @Test
    fun removeFromMap_canBeCalledMultipleTimes() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.removeFromMap()
        overlay.removeFromMap()

        // Should not crash
    }

    // ==================== changeAllBuildingColors tests ====================

    @Test
    fun changeAllBuildingColors_afterAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.changeAllBuildingColors("#ffaca6")

        // Should not crash
    }

    @Test
    fun changeAllBuildingColors_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.changeAllBuildingColors("#ffaca6")

        // Should not crash (no-op)
    }

    @Test
    fun changeAllBuildingColors_withDifferentColorFormats_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.changeAllBuildingColors("#FF0000")
        overlay.changeAllBuildingColors("#00ff00")
        overlay.changeAllBuildingColors("#0000FF")

        // Should not crash
    }

    @Test
    fun changeAllBuildingColors_afterRemovingFromMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)
        overlay.removeFromMap()

        overlay.changeAllBuildingColors("#ffaca6")

        // Should not crash (no-op)
    }

    @Test
    fun changeAllBuildingColors_appliesOnlyToPolygons() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        overlay.changeAllBuildingColors("#ffaca6")

        // Verify polygons have styles applied
        var polygonCount = 0
        var pointCount = 0
        layer.features.forEach { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    polygonCount++
                    assertNotNull(feature.polygonStyle)
                }
                is GeoJsonPoint -> {
                    pointCount++
                }
            }
        }

        assertTrue("Should have polygon features", polygonCount > 0)
    }

    // ==================== changeAllPointColors tests ====================

    @Test
    fun changeAllPointColors_afterAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.changeAllPointColors("#bc4949")

        // Should not crash
    }

    @Test
    fun changeAllPointColors_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.changeAllPointColors("#bc4949")

        // Should not crash (no-op)
    }

    @Test
    fun changeAllPointColors_withDifferentColorFormats_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.changeAllPointColors("#FF0000")
        overlay.changeAllPointColors("#00ff00")
        overlay.changeAllPointColors("#0000FF")

        // Should not crash
    }

    @Test
    fun changeAllPointColors_afterRemovingFromMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)
        overlay.removeFromMap()

        overlay.changeAllPointColors("#bc4949")

        // Should not crash (no-op)
    }

    @Test
    fun changeAllPointColors_appliesOnlyToPoints() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        overlay.changeAllPointColors("#bc4949")

        // Verify points have styles applied
        var pointCount = 0
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                pointCount++
                assertNotNull(feature.pointStyle)
            }
        }

        assertTrue("Should have point features", pointCount > 0)
    }

    // ==================== removeAllPoints tests ====================

    @Test
    fun removeAllPoints_afterAddingToMap_hidesPoints() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        overlay.removeAllPoints()

        // Verify points are hidden (alpha = 0)
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertNotNull(feature.pointStyle)
                assertEquals(0f, feature.pointStyle.alpha, 0.01f)
            }
        }
    }

    @Test
    fun removeAllPoints_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.removeAllPoints()

        // Should not crash (no-op)
    }

    @Test
    fun removeAllPoints_afterRemovingFromMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)
        overlay.removeFromMap()

        overlay.removeAllPoints()

        // Should not crash (no-op)
    }

    @Test
    fun removeAllPoints_canBeCalledMultipleTimes() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.removeAllPoints()
        overlay.removeAllPoints()

        // Should not crash
    }

    // ==================== restoreAllPoints tests ====================

    @Test
    fun restoreAllPoints_afterRemovingPoints_restoresPoints() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        overlay.removeAllPoints()
        overlay.restoreAllPoints()

        // Verify points are visible again
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertNotNull(feature.pointStyle)
                assertTrue("Point should be visible", feature.pointStyle.alpha > 0f)
            }
        }
    }

    @Test
    fun restoreAllPoints_withoutRemovingFirst_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.restoreAllPoints()

        // Should not crash
    }

    @Test
    fun restoreAllPoints_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.restoreAllPoints()

        // Should not crash (no-op)
    }

    @Test
    fun restoreAllPoints_restoresCustomColors() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        overlay.changeAllPointColors("#FF0000")
        overlay.removeAllPoints()
        overlay.restoreAllPoints()

        // Points should be visible with custom colors preserved
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertNotNull(feature.pointStyle)
                assertTrue("Point should be visible", feature.pointStyle.alpha > 0f)
            }
        }
    }

    // ==================== hideFromMap tests ====================

    @Test
    fun hideFromMap_makesPolygonsTransparent() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")

        overlay.hideFromMap()

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertEquals(0x00000000, feature.polygonStyle.fillColor)
                assertEquals(0x00000000, feature.polygonStyle.strokeColor)
                assertEquals(0f, feature.polygonStyle.strokeWidth, 0.01f)
            }
        }
    }

    @Test
    fun hideFromMap_makesPointsTransparent() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllPointColors("#bc4949")

        overlay.hideFromMap()

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertEquals(0f, feature.pointStyle.alpha, 0.01f)
            }
        }
    }

    @Test
    fun hideFromMap_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.hideFromMap()
        // no-op, should not crash
    }

    @Test
    fun hideFromMap_calledTwice_secondCallIsNoOp() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")

        overlay.hideFromMap() // sets isVisible = false

        // Manually set a non-transparent color to prove the second call doesn't touch features
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                val style = GeoJsonPolygonStyle()
                style.fillColor = 0xFFFF0000.toInt()
                feature.polygonStyle = style
            }
        }

        overlay.hideFromMap() // should be no-op

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertEquals(0xFFFF0000.toInt(), feature.polygonStyle.fillColor)
            }
        }
    }

    @Test
    fun hideFromMap_doesNotOverwriteAlreadySavedCustomStyles() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        // changeAllBuildingColors saves styles into customPolygonStyles
        overlay.changeAllBuildingColors("#ffaca6")
        overlay.changeAllPointColors("#bc4949")

        overlay.hideFromMap()
        overlay.showOnMap()

        // Custom colors should be restored, not default styles
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertNotEquals(0x00000000, feature.polygonStyle.fillColor)
            }
        }
    }

    // ==================== showOnMap tests ====================

    @Test
    fun showOnMap_afterHide_restoresPolygonStyles() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")

        overlay.hideFromMap()
        overlay.showOnMap()

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertNotEquals(0x00000000, feature.polygonStyle.fillColor)
            }
        }
    }

    @Test
    fun showOnMap_afterHide_restoresPointAlphaToOne() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllPointColors("#bc4949")

        overlay.hideFromMap()
        overlay.showOnMap()

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertEquals(1f, feature.pointStyle.alpha, 0.01f)
            }
        }
    }

    @Test
    fun showOnMap_withoutPriorHide_isNoOp() {
        // isVisible starts as true — showOnMap should not touch any features
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")

        // Force polygons transparent manually (simulates an external change)
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                val style = GeoJsonPolygonStyle()
                style.fillColor = 0x00000000
                feature.polygonStyle = style
            }
        }

        overlay.showOnMap() // no-op because isVisible is still true

        // Should still be transparent — showOnMap didn't run
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertEquals(0x00000000, feature.polygonStyle.fillColor)
            }
        }
    }

    @Test
    fun showOnMap_beforeAddingToMap_doesNotCrash() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.showOnMap()
        // isVisible is true by default, so this is an early return — no crash
    }

    @Test
    fun hideAndShow_multipleCycles_preservesStyles() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")
        overlay.changeAllPointColors("#bc4949")

        repeat(3) {
            overlay.hideFromMap()
            overlay.showOnMap()
        }

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertNotEquals(0x00000000, feature.polygonStyle.fillColor)
            }
            if (feature.geometry is GeoJsonPoint) {
                assertEquals(1f, feature.pointStyle.alpha, 0.01f)
            }
        }
    }

    // ==================== isVisible / removeFromMap reset tests ====================

    @Test
    fun removeFromMap_resetsVisibilityState() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)
        overlay.hideFromMap() // isVisible = false

        overlay.removeFromMap() // resets isVisible = true

        // Re-add and verify showOnMap is a no-op (isVisible is true again)
        val layer = overlay.addToMap(mockMap, context)
        overlay.changeAllBuildingColors("#ffaca6")

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                val style = GeoJsonPolygonStyle()
                style.fillColor = 0x00000000
                feature.polygonStyle = style
            }
        }

        overlay.showOnMap() // should be no-op — isVisible was reset to true

        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                assertEquals(0x00000000, feature.polygonStyle.fillColor)
            }
        }
    }

    @Test
    fun hideFromMap_savesDefaultStylesWhenNoCustomStyleExists() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)
        // Do NOT call changeAllBuildingColors — no custom styles saved

        overlay.hideFromMap() // should save default styles before hiding
        overlay.showOnMap()   // should restore the default styles

        // Features should be visible (restored from saved default styles)
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                // Default styles should have been saved and restored
                assertNotNull(feature.polygonStyle)
            }
            if (feature.geometry is GeoJsonPoint) {
                assertEquals(1f, feature.pointStyle.alpha, 0.01f)
            }
        }
    }

    // ==================== changeSpecificBuildingColor tests ====================

    @Test
    fun changeSpecificBuildingColor_withValidName_returnsTrue() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColor("H", "#FF0000")

        assertTrue("H building polygon exists in SGW GeoJSON", result)
    }

    @Test
    fun changeSpecificBuildingColor_withNonExistentName_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColor("NonExistentBuilding123", "#FF0000")

        assertFalse(result)
    }

    @Test
    fun changeSpecificBuildingColor_beforeAddingToMap_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val result = overlay.changeSpecificBuildingColor("H", "#FF0000")

        assertFalse(result)
    }

    @Test
    fun changeSpecificBuildingColor_withCaseInsensitiveName_works() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result1 = overlay.changeSpecificBuildingColor("h", "#FF0000")
        val result2 = overlay.changeSpecificBuildingColor("H", "#00FF00")

        assertTrue("Lowercase 'h' should match 'H' (case-insensitive)", result1)
        assertTrue("Uppercase 'H' should match 'H'", result2)
    }

    @Test
    fun changeSpecificBuildingColor_withWhitespace_trimsCorrectly() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColor("  H  ", "#FF0000")

        assertTrue("Whitespace around 'H' should be trimmed", result)
    }

    // ==================== changeSpecificPointColor tests ====================

    @Test
    fun changeSpecificPointColor_withValidName_works() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificPointColor("H", "#FF0000")

        assertTrue("H building point exists in SGW GeoJSON", result)
    }

    @Test
    fun changeSpecificPointColor_withNonExistentName_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificPointColor("NonExistentBuilding123", "#FF0000")

        assertFalse(result)
    }

    @Test
    fun changeSpecificPointColor_beforeAddingToMap_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val result = overlay.changeSpecificPointColor("H", "#FF0000")

        assertFalse(result)
    }

    @Test
    fun changeSpecificPointColor_withCaseInsensitiveName_works() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result1 = overlay.changeSpecificPointColor("h", "#FF0000")
        val result2 = overlay.changeSpecificPointColor("H", "#00FF00")

        assertTrue("Lowercase 'h' should match 'H' (case-insensitive)", result1)
        assertTrue("Uppercase 'H' should match 'H'", result2)
    }

    // ==================== changeSpecificBuildingColors tests ====================

    @Test
    fun changeSpecificBuildingColors_updatesBothPolygonAndPoint() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColors("H", "#FF0000", "#00FF00")

        assertTrue("H has both polygon and point in SGW", result)
    }

    @Test
    fun changeSpecificBuildingColors_withNonExistentName_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColors("NonExistentBuilding123", "#FF0000", "#00FF00")

        assertFalse(result)
    }

    @Test
    fun changeSpecificBuildingColors_beforeAddingToMap_returnsFalse() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        val result = overlay.changeSpecificBuildingColors("H", "#FF0000", "#00FF00")

        assertFalse(result)
    }

    @Test
    fun changeSpecificBuildingColors_returnsTrueIfEitherUpdates() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColors("H", "#FF0000", "#00FF00")

        assertTrue("H exists in SGW — both polygon and point updated", result)
    }

    // ==================== Integration and workflow tests ====================

    @Test
    fun overlay_supportsCompleteWorkflow() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        // Add to map
        val layer = overlay.addToMap(mockMap, context)
        assertNotNull(layer)

        // Change all colors
        overlay.changeAllBuildingColors("#ffaca6")
        overlay.changeAllPointColors("#bc4949")

        // Hide points
        overlay.removeAllPoints()

        // Restore points
        overlay.restoreAllPoints()

        // Change specific building
        overlay.changeSpecificBuildingColors("H", "#FF0000", "#00FF00")

        // Remove from map
        overlay.removeFromMap()

        // Should complete without crashes
    }

    @Test
    fun overlay_canBeReusedAfterRemoval() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)

        overlay.addToMap(mockMap, context)
        overlay.removeFromMap()
        overlay.addToMap(mockMap, context)

        // Should work correctly
        assertNotNull(overlay)
    }

    @Test
    fun overlay_multipleOverlaysCanCoexist() {
        val overlay1 = GeoJsonOverlay(R.raw.sgw_buildings)
        val overlay2 = GeoJsonOverlay(R.raw.loy_buildings)

        val layer1 = overlay1.addToMap(mockMap, context)
        val layer2 = overlay2.addToMap(mockMap, context)

        assertNotNull(layer1)
        assertNotNull(layer2)

        overlay1.changeAllBuildingColors("#FF0000")
        overlay2.changeAllBuildingColors("#00FF00")

        // Should work independently
    }

    @Test
    fun overlay_handlesMixedOperations() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        // Mix of operations
        overlay.changeAllBuildingColors("#ffaca6")
        overlay.removeAllPoints()
        overlay.changeAllPointColors("#bc4949") // Should work but points hidden
        overlay.restoreAllPoints() // Should restore with new color
        overlay.changeSpecificBuildingColor("H", "#FFFFFF")

        // Should complete without issues
    }

    @Test
    fun overlay_preservesCustomStylesThroughRestoration() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        val layer = overlay.addToMap(mockMap, context)

        // Set custom colors
        overlay.changeAllPointColors("#FF0000")

        // Hide and restore
        overlay.removeAllPoints()
        overlay.restoreAllPoints()

        // Custom colors should be preserved
        layer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                assertNotNull(feature.pointStyle)
                assertNotNull(feature.pointStyle.icon)
            }
        }
    }

    @Test
    fun overlay_handlesEmptyBuildingNameSearch() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        val result = overlay.changeSpecificBuildingColor("", "#FF0000")

        assertFalse(result)
    }

    @Test
    fun overlay_handlesMultipleColorChanges() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        overlay.changeAllBuildingColors("#FF0000")
        overlay.changeAllBuildingColors("#00FF00")
        overlay.changeAllBuildingColors("#0000FF")

        // Should work without issues
    }

    @Test
    fun overlay_customStyleMapperIsUsed() {
        val customMapper = GeoJsonStyleMapper()
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings, customMapper)

        val layer = overlay.addToMap(mockMap, context)

        assertNotNull(layer)
        // Custom mapper should be used for styling
    }

    @Test
    fun overlay_supportsRapidOperations() {
        val overlay = GeoJsonOverlay(R.raw.sgw_buildings)
        overlay.addToMap(mockMap, context)

        // Rapid successive operations
        repeat(10) {
            overlay.changeAllBuildingColors("#FF0000")
            overlay.removeAllPoints()
            overlay.restoreAllPoints()
        }

        // Should handle rapid operations
    }
}
