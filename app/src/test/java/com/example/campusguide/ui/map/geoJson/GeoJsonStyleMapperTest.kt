package com.example.campusguide.ui.map.geoJson

import com.example.campusguide.map.geoJson.DefaultGeoJsonStyleMapper
import com.example.campusguide.map.geoJson.GeoJsonColorUtils
import com.google.maps.android.data.geojson.GeoJsonFeature
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
    class GeoJsonStyleMapperTest {

    private val mapper = DefaultGeoJsonStyleMapper()

    private fun featureWithProps(vararg pairs: Pair<String, String>): GeoJsonFeature {
        val f = mock(GeoJsonFeature::class.java)
        `when`(f.getProperty(anyString())).thenReturn(null)
        for ((k, v) in pairs) {
            `when`(f.getProperty(eq(k))).thenReturn(v)
        }
        return f
    }

    @Test
    fun styleFor_noProperties_returnsAllNulls_clickableTrue() {
        val feature = featureWithProps()

        val style = mapper.styleFor(feature)

        assertNull(style.fillColor)
        assertNull(style.strokeColor)
        assertNull(style.strokeWidth)
        assertEquals(true, style.clickable) // Boolean? safe
    }

    @Test
    fun styleFor_fillOnly_parsesFillColor() {
        val feature = featureWithProps("fill" to "#112233")

        val style = mapper.styleFor(feature)

        assertEquals(GeoJsonColorUtils.parse("#112233"), style.fillColor)
        assertNull(style.strokeColor)
        assertNull(style.strokeWidth)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_fillAndOpacity_appliesOpacity() {
        val feature = featureWithProps(
            "fill" to "#112233",
            "fill-opacity" to "0.5"
        )

        val style = mapper.styleFor(feature)

        val base = GeoJsonColorUtils.parse("#112233")
        val expected = GeoJsonColorUtils.withOpacity(base, 0.5f)
        assertEquals(expected, style.fillColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_fillOpacityButNoFill_ignoresOpacity() {
        val feature = featureWithProps("fill-opacity" to "0.25")

        val style = mapper.styleFor(feature)

        assertNull(style.fillColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_fillInvalidColor_safeParseReturnsNull() {
        val feature = featureWithProps("fill" to "not-a-color")

        val style = mapper.styleFor(feature)

        assertNull(style.fillColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_fillOpacityInvalidNumber_doesNotApplyOpacity_usesBaseFill() {
        val feature = featureWithProps(
            "fill" to "#112233",
            "fill-opacity" to "abc"
        )

        val style = mapper.styleFor(feature)

        assertEquals(GeoJsonColorUtils.parse("#112233"), style.fillColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeOnly_parsesStrokeColor() {
        val feature = featureWithProps("stroke" to "#AABBCC")

        val style = mapper.styleFor(feature)

        assertNull(style.fillColor)
        assertEquals(GeoJsonColorUtils.parse("#AABBCC"), style.strokeColor)
        assertNull(style.strokeWidth)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeAndOpacity_appliesOpacity() {
        val feature = featureWithProps(
            "stroke" to "#AABBCC",
            "stroke-opacity" to "0.2"
        )

        val style = mapper.styleFor(feature)

        val base = GeoJsonColorUtils.parse("#AABBCC")
        val expected = GeoJsonColorUtils.withOpacity(base, 0.2f)
        assertEquals(expected, style.strokeColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeOpacityInvalidNumber_doesNotApplyOpacity_usesBaseStroke() {
        val feature = featureWithProps(
            "stroke" to "#AABBCC",
            "stroke-opacity" to "nope"
        )

        val style = mapper.styleFor(feature)

        assertEquals(GeoJsonColorUtils.parse("#AABBCC"), style.strokeColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeInvalidColor_safeParseReturnsNull() {
        val feature = featureWithProps("stroke" to "bad")

        val style = mapper.styleFor(feature)

        assertNull(style.strokeColor)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeWidth_parsesFloat() {
        val feature = featureWithProps("stroke-width" to "3.5")

        val style = mapper.styleFor(feature)

        assertEquals(3.5f, style.strokeWidth)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_strokeWidthInvalid_returnsNull() {
        val feature = featureWithProps("stroke-width" to "wide")

        val style = mapper.styleFor(feature)

        assertNull(style.strokeWidth)
        assertEquals(true, style.clickable)
    }

    @Test
    fun styleFor_allProperties_combinesCorrectly() {
        val feature = featureWithProps(
            "fill" to "#112233",
            "fill-opacity" to "0.75",
            "stroke" to "#445566",
            "stroke-opacity" to "0.25",
            "stroke-width" to "2.0"
        )

        val style = mapper.styleFor(feature)

        val fillBase = GeoJsonColorUtils.parse("#112233")
        val strokeBase = GeoJsonColorUtils.parse("#445566")

        assertEquals(GeoJsonColorUtils.withOpacity(fillBase, 0.75f), style.fillColor)
        assertEquals(GeoJsonColorUtils.withOpacity(strokeBase, 0.25f), style.strokeColor)
        assertEquals(2.0f, style.strokeWidth)
        assertEquals(true, style.clickable)
    }
}
