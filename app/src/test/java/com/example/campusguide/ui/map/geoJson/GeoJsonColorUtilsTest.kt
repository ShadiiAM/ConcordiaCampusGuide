package com.example.campusguide.ui.map.geoJson

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeoJsonColorUtilsTest {

    // -------------------------
    // parse()
    // -------------------------

    @Test
    fun parse_acceptsHashRRGGBB() {
        val c = GeoJsonColorUtils.parse("#112233")
        assertEquals(0xFF112233.toInt(), c)
    }

    @Test
    fun parse_acceptsHashAARRGGBB() {
        val c = GeoJsonColorUtils.parse("#80112233")
        assertEquals(0x80112233.toInt(), c)
    }

    @Test
    fun parse_trimsWhitespace() {
        val c = GeoJsonColorUtils.parse("   #FFFFFF   ")
        assertEquals(0xFFFFFFFF.toInt(), c)
    }

    @Test
    fun parse_namedColor_works() {
        // Color.parseColor supports named colors (case-insensitive)
        val c = GeoJsonColorUtils.parse("red")
        assertEquals(0xFFFF0000.toInt(), c)
    }

    @Test
    fun parse_withoutHashHex_throws() {
        // New behavior: Color.parseColor("AABBCC") throws (not a valid color string)
        try {
            GeoJsonColorUtils.parse("AABBCC")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // ok
        }
    }

    @Test
    fun parse_invalidString_throws() {
        try {
            GeoJsonColorUtils.parse("NOT_A_COLOR")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // ok
        }
    }

    // -------------------------
    // withOpacity()
    // -------------------------

    @Test
    fun withOpacity_zeroOpacity_makesAlphaZero_preservesRGB() {
        val color = 0x80112233.toInt() // alpha=0x80, rgb=0x112233
        val out = GeoJsonColorUtils.withOpacity(color, 0f)
        assertEquals(0x00112233, out)
    }

    @Test
    fun withOpacity_fullOpacity_keepsSameColor() {
        val color = 0x80112233.toInt()
        val out = GeoJsonColorUtils.withOpacity(color, 1f)
        assertEquals(color, out)
    }

    @Test
    fun withOpacity_halfOpacity_multipliesExistingAlpha_andRounds() {
        // alpha=255, 255*0.5=127.5 -> roundToInt() = 128
        val color = 0xFF112233.toInt()
        val out = GeoJsonColorUtils.withOpacity(color, 0.5f)
        assertEquals(0x80112233.toInt(), out)
    }

    @Test
    fun withOpacity_clampsOpacityBelowZero_toZero() {
        val color = 0x7F445566 // alpha 127
        val out = GeoJsonColorUtils.withOpacity(color, -10f)
        assertEquals(0x00445566, out)
    }

    @Test
    fun withOpacity_clampsOpacityAboveOne_toOne() {
        val color = 0x7F445566 // alpha 127
        val out = GeoJsonColorUtils.withOpacity(color, 10f)
        assertEquals(color, out)
    }

    // -------------------------
    // floatOrNull()
    // -------------------------

    @Test
    fun floatOrNull_string_invalid_returnsNull() {
        assertNull(GeoJsonColorUtils.floatOrNull("not-a-number"))
    }

    @Test
    fun floatOrNull_null_returnsNull() {
        assertNull(GeoJsonColorUtils.floatOrNull(null))
    }

    @Test
    fun floatOrNull_otherType_returnsNull() {
        assertNull(GeoJsonColorUtils.floatOrNull(listOf(1, 2, 3)))
    }

    // -------------------------
    // stringOrNull()
    // -------------------------

    @Test
    fun stringOrNull_string_returnsSame() {
        assertEquals("hello", GeoJsonColorUtils.stringOrNull("hello"))
    }

    @Test
    fun stringOrNull_nonString_returnsToString() {
        assertEquals("123", GeoJsonColorUtils.stringOrNull(123))
        assertEquals("true", GeoJsonColorUtils.stringOrNull(true))
    }

    @Test
    fun stringOrNull_null_returnsNull() {
        assertNull(GeoJsonColorUtils.stringOrNull(null))
    }
}
