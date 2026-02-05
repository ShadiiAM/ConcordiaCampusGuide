package com.example.campusguide.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for Type.kt to verify Typography configuration
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TypeTest {

    @Test
    fun typography_isNotNull() {
        assertNotNull(Typography)
    }

    @Test
    fun bodyLarge_hasCorrectFontFamily() {
        assertEquals(FontFamily.Default, Typography.bodyLarge.fontFamily)
    }

    @Test
    fun bodyLarge_hasCorrectFontWeight() {
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
    }

    @Test
    fun bodyLarge_hasCorrectFontSize() {
        assertEquals(16.sp, Typography.bodyLarge.fontSize)
    }

    @Test
    fun bodyLarge_hasCorrectLineHeight() {
        assertEquals(24.sp, Typography.bodyLarge.lineHeight)
    }

    @Test
    fun bodyLarge_hasCorrectLetterSpacing() {
        assertEquals(0.5.sp, Typography.bodyLarge.letterSpacing)
    }

    @Test
    fun typography_bodyLarge_isAccessible() {
        val bodyLarge = Typography.bodyLarge
        assertNotNull(bodyLarge)
        assertNotNull(bodyLarge.fontFamily)
        assertNotNull(bodyLarge.fontWeight)
    }

    @Test
    fun typography_defaultStyles_areAvailable() {
        // Verify default Material3 typography styles are available
        assertNotNull(Typography.displayLarge)
        assertNotNull(Typography.displayMedium)
        assertNotNull(Typography.displaySmall)
        assertNotNull(Typography.headlineLarge)
        assertNotNull(Typography.headlineMedium)
        assertNotNull(Typography.headlineSmall)
        assertNotNull(Typography.titleLarge)
        assertNotNull(Typography.titleMedium)
        assertNotNull(Typography.titleSmall)
        assertNotNull(Typography.bodyLarge)
        assertNotNull(Typography.bodyMedium)
        assertNotNull(Typography.bodySmall)
        assertNotNull(Typography.labelLarge)
        assertNotNull(Typography.labelMedium)
        assertNotNull(Typography.labelSmall)
    }
}
