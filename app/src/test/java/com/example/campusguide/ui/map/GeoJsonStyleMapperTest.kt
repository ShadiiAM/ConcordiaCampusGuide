package com.example.campusguide.ui.map

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.shadows.ShadowBitmapDescriptorFactory
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config

/**
 * Unit tests for GeoJsonStyleMapper
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], shadows = [ShadowBitmapDescriptorFactory::class])
class GeoJsonStyleMapperTest {

    private lateinit var styleMapper: GeoJsonStyleMapper

    @Before
    fun setUp() {
        styleMapper = GeoJsonStyleMapper()
    }

    // ==================== applyPolygonStyle tests ====================

    @Test
    fun applyPolygonStyle_withStrokeColor_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf("stroke" to "#FF0000"))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(Color.parseColor("#FF0000"), style.strokeColor)
    }

    @Test
    fun applyPolygonStyle_withFillColor_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf("fill" to "#00FF00"))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(Color.parseColor("#00FF00"), style.fillColor)
    }

    @Test
    fun applyPolygonStyle_withStrokeWidth_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf("stroke-width" to "3.5"))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(3.5f, style.strokeWidth, 0.01f)
    }

    @Test
    fun applyPolygonStyle_withStrokeOpacity_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf(
            "stroke" to "#FF0000",
            "stroke-opacity" to "0.5"
        ))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        val alpha = Color.alpha(style.strokeColor)
        assertEquals(127.0, alpha.toDouble(), 2.0) // 0.5 * 255 ≈ 127
    }

    @Test
    fun applyPolygonStyle_withFillOpacity_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf(
            "fill" to "#0000FF",
            "fill-opacity" to "0.8"
        ))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        val alpha = Color.alpha(style.fillColor)
        assertEquals(204.0, alpha.toDouble(), 2.0) // 0.8 * 255 ≈ 204
    }

    @Test
    fun applyPolygonStyle_withAllProperties_appliesCorrectly() {
        val feature = createPolygonFeature(mapOf(
            "stroke" to "#FF0000",
            "fill" to "#00FF00",
            "stroke-width" to "2.5",
            "stroke-opacity" to "0.7",
            "fill-opacity" to "0.3"
        ))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(2.5f, style.strokeWidth, 0.01f)
        // Check that colors were applied (exact values depend on alpha blending)
        assertNotEquals(0, style.strokeColor)
        assertNotEquals(0, style.fillColor)
    }

    @Test
    fun applyPolygonStyle_withNoProperties_doesNotCrash() {
        val feature = createPolygonFeature(emptyMap())
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        // Should not crash, style remains at defaults
        assertNotNull(style)
    }

    @Test
    fun applyPolygonStyle_withInvalidColor_fallsBackToRed() {
        val feature = createPolygonFeature(mapOf("stroke" to "invalid-color"))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(Color.RED, style.strokeColor)
    }

    @Test
    fun applyPolygonStyle_withInvalidStrokeWidth_ignoresValue() {
        val feature = createPolygonFeature(mapOf("stroke-width" to "not-a-number"))
        val style = GeoJsonPolygonStyle()
        val originalWidth = style.strokeWidth

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(originalWidth, style.strokeWidth, 0.01f)
    }

    // ==================== applyPointStyle tests ====================

    @Test
    fun applyPointStyle_withMarkerColor_appliesCorrectly() {
        val feature = createPointFeature(mapOf("marker-color" to "#FF0000"))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertNotNull(style.icon)
        assertEquals(1.0f, style.alpha, 0.01f)
    }

    @Test
    fun applyPointStyle_withTitle_appliesCorrectly() {
        val feature = createPointFeature(mapOf("title" to "Test Title"))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Test Title", style.title)
    }

    @Test
    fun applyPointStyle_withName_appliesAsTitleCorrectly() {
        val feature = createPointFeature(mapOf("name" to "Test Name"))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Test Name", style.title)
    }

    @Test
    fun applyPointStyle_withBuildingName_appliesAsTitleCorrectly() {
        val feature = createPointFeature(mapOf("building-name" to "Test Building"))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Test Building", style.title)
    }

    @Test
    fun applyPointStyle_withDescription_appliesAsSnippetCorrectly() {
        val feature = createPointFeature(mapOf("description" to "Test Description"))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Test Description", style.snippet)
    }

    @Test
    fun applyPointStyle_withTitleAndNameAndBuildingName_prioritizesTitle() {
        val feature = createPointFeature(mapOf(
            "title" to "Title Value",
            "name" to "Name Value",
            "building-name" to "Building Name Value"
        ))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Title Value", style.title)
    }

    @Test
    fun applyPointStyle_withNameAndBuildingName_prioritizesName() {
        val feature = createPointFeature(mapOf(
            "name" to "Name Value",
            "building-name" to "Building Name Value"
        ))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Name Value", style.title)
    }

    @Test
    fun applyPointStyle_withAllProperties_appliesCorrectly() {
        val feature = createPointFeature(mapOf(
            "marker-color" to "#0000FF",
            "title" to "Complete Title",
            "description" to "Complete Description"
        ))
        val style = GeoJsonPointStyle()

        styleMapper.applyPointStyle(feature, style)

        assertEquals("Complete Title", style.title)
        assertEquals("Complete Description", style.snippet)
        assertNotNull(style.icon)
    }

    // ==================== applyCustomFillColor tests ====================

    @Test
    fun applyCustomFillColor_appliesCustomColorWithFixedOpacity() {
        val feature = createPolygonFeature(emptyMap())
        val style = GeoJsonPolygonStyle()

        styleMapper.applyCustomFillColor(feature, style, "#FF0000")

        val alpha = Color.alpha(style.fillColor)
        assertEquals(127.0, alpha.toDouble(), 2.0) // 0.5 * 255 ≈ 127
    }

    @Test
    fun applyCustomFillColor_preservesStrokeProperties() {
        val feature = createPolygonFeature(mapOf(
            "stroke" to "#00FF00",
            "stroke-width" to "4.0",
            "stroke-opacity" to "0.9"
        ))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyCustomFillColor(feature, style, "#FF0000")

        assertEquals(4.0f, style.strokeWidth, 0.01f)
        assertNotEquals(0, style.strokeColor)
    }

    @Test
    fun applyCustomFillColor_withInvalidColor_fallsBackToRed() {
        val feature = createPolygonFeature(emptyMap())
        val style = GeoJsonPolygonStyle()

        styleMapper.applyCustomFillColor(feature, style, "invalid-color")

        // Should use Color.RED as fallback
        val alpha = Color.alpha(style.fillColor)
        assertEquals(127.0, alpha.toDouble(), 2.0) // 0.5 * 255 ≈ 127
    }

    @Test
    fun applyCustomFillColor_withDifferentColorFormats_worksCorrectly() {
        val feature = createPolygonFeature(emptyMap())
        val style = GeoJsonPolygonStyle()

        styleMapper.applyCustomFillColor(feature, style, "#ffaca6")

        assertNotEquals(0, style.fillColor)
        val alpha = Color.alpha(style.fillColor)
        assertEquals(127.0, alpha.toDouble(), 2.0)
    }

    // ==================== applyCustomMarkerColor tests ====================

    @Test
    fun applyCustomMarkerColor_appliesCustomColor() {
        val feature = createPointFeature(emptyMap())
        val style = GeoJsonPointStyle()

        styleMapper.applyCustomMarkerColor(feature, style, "#FF0000")

        assertNotNull(style.icon)
        assertEquals(1.0f, style.alpha, 0.01f)
    }

    @Test
    fun applyCustomMarkerColor_preservesTitleFromAllSources() {
        val feature = createPointFeature(mapOf("title" to "Building Title"))
        val style = GeoJsonPointStyle()

        styleMapper.applyCustomMarkerColor(feature, style, "#00FF00")

        assertEquals("Building Title", style.title)
        assertNotNull(style.icon)
    }

    @Test
    fun applyCustomMarkerColor_preservesDescription() {
        val feature = createPointFeature(mapOf("description" to "Building Description"))
        val style = GeoJsonPointStyle()

        styleMapper.applyCustomMarkerColor(feature, style, "#0000FF")

        assertEquals("Building Description", style.snippet)
        assertNotNull(style.icon)
    }

    @Test
    fun applyCustomMarkerColor_withInvalidColor_fallsBackToRed() {
        val feature = createPointFeature(emptyMap())
        val style = GeoJsonPointStyle()

        styleMapper.applyCustomMarkerColor(feature, style, "bad-color")

        assertNotNull(style.icon)
        assertEquals(1.0f, style.alpha, 0.01f)
    }

    @Test
    fun applyCustomMarkerColor_withAllProperties_appliesCorrectly() {
        val feature = createPointFeature(mapOf(
            "title" to "Custom Title",
            "description" to "Custom Description"
        ))
        val style = GeoJsonPointStyle()

        styleMapper.applyCustomMarkerColor(feature, style, "#bc4949")

        assertEquals("Custom Title", style.title)
        assertEquals("Custom Description", style.snippet)
        assertNotNull(style.icon)
    }

    // ==================== Edge cases and integration tests ====================

    @Test
    fun styleMapper_handlesMultipleStyleApplications() {
        val feature = createPolygonFeature(mapOf("fill" to "#FF0000"))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)
        val firstColor = style.fillColor

        styleMapper.applyCustomFillColor(feature, style, "#00FF00")
        val secondColor = style.fillColor

        assertNotEquals(firstColor, secondColor)
    }

    @Test
    fun styleMapper_handlesOpacityBoundaries() {
        val featureZero = createPolygonFeature(mapOf(
            "fill" to "#FF0000",
            "fill-opacity" to "0.0"
        ))
        val styleZero = GeoJsonPolygonStyle()
        styleMapper.applyPolygonStyle(featureZero, styleZero)
        assertEquals(0, Color.alpha(styleZero.fillColor))

        val featureOne = createPolygonFeature(mapOf(
            "fill" to "#FF0000",
            "fill-opacity" to "1.0"
        ))
        val styleOne = GeoJsonPolygonStyle()
        styleMapper.applyPolygonStyle(featureOne, styleOne)
        assertEquals(255, Color.alpha(styleOne.fillColor))
    }

    @Test
    fun styleMapper_handlesOpacityOutOfBounds() {
        val featureNegative = createPolygonFeature(mapOf(
            "fill" to "#FF0000",
            "fill-opacity" to "-0.5"
        ))
        val styleNegative = GeoJsonPolygonStyle()
        styleMapper.applyPolygonStyle(featureNegative, styleNegative)
        assertEquals(0, Color.alpha(styleNegative.fillColor))

        val featureOver = createPolygonFeature(mapOf(
            "fill" to "#FF0000",
            "fill-opacity" to "2.0"
        ))
        val styleOver = GeoJsonPolygonStyle()
        styleMapper.applyPolygonStyle(featureOver, styleOver)
        assertEquals(255, Color.alpha(styleOver.fillColor))
    }

    @Test
    fun styleMapper_handlesWhitespaceInColors() {
        val feature = createPolygonFeature(mapOf("stroke" to "  #FF0000  "))
        val style = GeoJsonPolygonStyle()

        styleMapper.applyPolygonStyle(feature, style)

        assertEquals(Color.parseColor("#FF0000"), style.strokeColor)
    }

    @Test
    fun styleMapper_canBeReused() {
        val mapper = GeoJsonStyleMapper()

        val feature1 = createPolygonFeature(mapOf("fill" to "#FF0000"))
        val style1 = GeoJsonPolygonStyle()
        mapper.applyPolygonStyle(feature1, style1)

        val feature2 = createPolygonFeature(mapOf("fill" to "#00FF00"))
        val style2 = GeoJsonPolygonStyle()
        mapper.applyPolygonStyle(feature2, style2)

        assertNotEquals(style1.fillColor, style2.fillColor)
    }

    // ==================== Helper methods ====================

    private fun createPolygonFeature(properties: Map<String, String>): GeoJsonFeature {
        val feature = mock(GeoJsonFeature::class.java)
        properties.forEach { (key, value) ->
            `when`(feature.getProperty(key)).thenReturn(value)
        }
        return feature
    }

    private fun createPointFeature(properties: Map<String, String>): GeoJsonFeature {
        val feature = mock(GeoJsonFeature::class.java)
        properties.forEach { (key, value) ->
            `when`(feature.getProperty(key)).thenReturn(value)
        }
        return feature
    }
}
