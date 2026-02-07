package com.example.campusguide.ui.map.geoJson

import com.example.campusguide.map.geoJson.GeoJsonColorUtils
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

    // -------------------------
    // Additional floatOrNull() tests
    // -------------------------

    @Test
    fun floatOrNull_intNumber_returnsFloat() {
        assertEquals(42f, GeoJsonColorUtils.floatOrNull(42)!!, 0.001f)
    }

    @Test
    fun floatOrNull_doubleNumber_returnsFloat() {
        assertEquals(3.14f, GeoJsonColorUtils.floatOrNull(3.14)!!, 0.001f)
    }

    @Test
    fun floatOrNull_longNumber_returnsFloat() {
        assertEquals(1000000f, GeoJsonColorUtils.floatOrNull(1000000L)!!, 0.001f)
    }

    @Test
    fun floatOrNull_validStringNumber_returnsFloat() {
        assertEquals(2.5f, GeoJsonColorUtils.floatOrNull("2.5")!!, 0.001f)
    }

    @Test
    fun floatOrNull_stringInteger_returnsFloat() {
        assertEquals(100f, GeoJsonColorUtils.floatOrNull("100")!!, 0.001f)
    }

    @Test
    fun floatOrNull_emptyString_returnsNull() {
        assertNull(GeoJsonColorUtils.floatOrNull(""))
    }

    @Test
    fun floatOrNull_whitespaceString_returnsNull() {
        assertNull(GeoJsonColorUtils.floatOrNull("   "))
    }

    // -------------------------
    // Additional withOpacity() tests
    // -------------------------

    @Test
    fun withOpacity_transparentColor_staysTransparent() {
        val transparent = 0x00112233
        val out = GeoJsonColorUtils.withOpacity(transparent, 1f)
        assertEquals(transparent, out)
    }

    @Test
    fun withOpacity_quarterOpacity_calculatesCorrectly() {
        val color = 0xFF112233.toInt()
        val out = GeoJsonColorUtils.withOpacity(color, 0.25f)
        // 255 * 0.25 = 63.75 -> rounds to 64 (0x40)
        assertEquals(0x40112233, out)
    }

    // -------------------------
    // Additional parse() tests
    // -------------------------

    @Test
    fun parse_blackColor_works() {
        val c = GeoJsonColorUtils.parse("#000000")
        assertEquals(0xFF000000.toInt(), c)
    }

    @Test
    fun parse_whiteColor_works() {
        val c = GeoJsonColorUtils.parse("#FFFFFF")
        assertEquals(0xFFFFFFFF.toInt(), c)
    }

    @Test
    fun parse_lowercaseHex_works() {
        val c = GeoJsonColorUtils.parse("#aabbcc")
        assertEquals(0xFFAABBCC.toInt(), c)
    }

    @Test
    fun parse_mixedCaseHex_works() {
        val c = GeoJsonColorUtils.parse("#AaBbCc")
        assertEquals(0xFFAABBCC.toInt(), c)
    }

    @Test
    fun parse_namedColor_blue_works() {
        val c = GeoJsonColorUtils.parse("blue")
        assertEquals(0xFF0000FF.toInt(), c)
    }

    @Test
    fun parse_yellowColor_works() {
        val c = GeoJsonColorUtils.parse("#FFFF00")
        assertEquals(0xFFFFFF00.toInt(), c)
    }

    @Test
    fun parse_cyanColor_works() {
        val c = GeoJsonColorUtils.parse("#00FFFF")
        assertEquals(0xFF00FFFF.toInt(), c)
    }

    // -------------------------
    // Additional stringOrNull() tests
    // -------------------------

    @Test
    fun stringOrNull_floatNumber_returnsToString() {
        assertEquals("3.14", GeoJsonColorUtils.stringOrNull(3.14))
    }

    @Test
    fun stringOrNull_emptyString_returnsEmpty() {
        assertEquals("", GeoJsonColorUtils.stringOrNull(""))
    }
}
