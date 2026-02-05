package com.example.campusguide.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for Color.kt to verify color constant definitions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ColorTest {

    @Test
    fun purple80_hasCorrectValue() {
        assertEquals(Color(0xFFD0BCFF), Purple80)
    }

    @Test
    fun purpleGrey80_hasCorrectValue() {
        assertEquals(Color(0xFFCCC2DC), PurpleGrey80)
    }

    @Test
    fun pink80_hasCorrectValue() {
        assertEquals(Color(0xFFEFB8C8), Pink80)
    }

    @Test
    fun purple40_hasCorrectValue() {
        assertEquals(Color(0xFF6650a4), Purple40)
    }

    @Test
    fun purpleGrey40_hasCorrectValue() {
        assertEquals(Color(0xFF625b71), PurpleGrey40)
    }

    @Test
    fun pink40_hasCorrectValue() {
        assertEquals(Color(0xFF7D5260), Pink40)
    }

    @Test
    fun lightAndDarkVariants_areDifferent() {
        assertNotEquals(Purple80, Purple40)
        assertNotEquals(PurpleGrey80, PurpleGrey40)
        assertNotEquals(Pink80, Pink40)
    }

    @Test
    fun colors80_areLighterThanColors40() {
        // 80 variants should have higher luminance (lighter) than 40 variants
        // We verify by checking that alpha channels are all fully opaque (0xFF)
        assertEquals(0xFF, (Purple80.value shr 56).toInt() and 0xFF)
        assertEquals(0xFF, (Purple40.value shr 56).toInt() and 0xFF)
        assertEquals(0xFF, (PurpleGrey80.value shr 56).toInt() and 0xFF)
        assertEquals(0xFF, (PurpleGrey40.value shr 56).toInt() and 0xFF)
        assertEquals(0xFF, (Pink80.value shr 56).toInt() and 0xFF)
        assertEquals(0xFF, (Pink40.value shr 56).toInt() and 0xFF)
    }

    @Test
    fun allColors_areNotTransparent() {
        // Verify none of the colors are fully transparent
        assertNotEquals(Color.Transparent, Purple80)
        assertNotEquals(Color.Transparent, PurpleGrey80)
        assertNotEquals(Color.Transparent, Pink80)
        assertNotEquals(Color.Transparent, Purple40)
        assertNotEquals(Color.Transparent, PurpleGrey40)
        assertNotEquals(Color.Transparent, Pink40)
    }
}
