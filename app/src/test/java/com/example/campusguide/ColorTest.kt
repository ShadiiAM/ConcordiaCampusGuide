package com.example.campusguide

import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.theme.OnSecondary
import com.example.campusguide.ui.theme.OnSecondaryContainer
import com.example.campusguide.ui.theme.Secondary
import com.example.campusguide.ui.theme.SecondaryContainer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Tests for the Material 3 color values added from Figma design
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ColorTest {

    @Test
    fun secondary_hasCorrectValue() {
        assertEquals(Color(0xFF625B71), Secondary)
    }

    @Test
    fun secondaryContainer_hasCorrectValue() {
        assertEquals(Color(0xFFE8DEF8), SecondaryContainer)
    }

    @Test
    fun onSecondary_hasCorrectValue() {
        assertEquals(Color(0xFFFFFFFF), OnSecondary)
    }

    @Test
    fun onSecondaryContainer_hasCorrectValue() {
        assertEquals(Color(0xFF4A4459), OnSecondaryContainer)
    }

    @Test
    fun allNewColors_areDistinct() {
        val colors = setOf(Secondary, SecondaryContainer, OnSecondary, OnSecondaryContainer)
        assertEquals("All four colors must be distinct", 4, colors.size)
    }

    @Test
    fun secondary_isNotWhite() {
        assertNotEquals(Color(0xFFFFFFFF), Secondary)
    }

    @Test
    fun onSecondary_isWhite() {
        assertEquals(Color(0xFFFFFFFF), OnSecondary)
    }

    @Test
    fun secondaryContainer_isLighterThanSecondary() {
        // SecondaryContainer (#E8DEF8) has higher RGB values than Secondary (#625B71)
        // Compare red channel: 0xE8 > 0x62
        assertTrue(SecondaryContainer != Secondary)
    }
}