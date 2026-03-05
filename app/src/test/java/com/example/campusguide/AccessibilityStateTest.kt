package com.example.campusguide

import androidx.compose.ui.unit.dp
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.ColorBlindMode
import junit.framework.TestCase.*
import org.junit.Assert
import org.junit.Test

class AccessibilityStateTest {


    @Test
    fun `text size increases up to maximum`() {
        val state = AccessibilityState(initialOffsetSp = 5f)

        state.increaseTextSize()
        Assert.assertEquals(6f, state.textSizeOffsetSp)

        repeat(3) { state.increaseTextSize() }
        Assert.assertEquals(6f, state.textSizeOffsetSp)
    }

    @Test
    fun `text size decreases down to minimum`() {
        val state = AccessibilityState(initialOffsetSp = -1f)

        state.decreaseTextSize()
        Assert.assertEquals(-2f, state.textSizeOffsetSp)

        repeat(3) { state.decreaseTextSize() }
        Assert.assertEquals(-2f, state.textSizeOffsetSp)
    }

    @Test
    fun `icon size increases up to maximum`() {
        val state = AccessibilityState(initialOffsetDp = 14.dp)

        state.increaseIconSize()
        Assert.assertEquals(16.dp, state.iconSizeOffsetDp)

        repeat(3) { state.increaseIconSize() }
        Assert.assertEquals(16.dp, state.iconSizeOffsetDp)
    }

    @Test
    fun `icon size decrease down to minimum`() {
        val state = AccessibilityState(initialOffsetDp = -8.dp)

        state.decreaseIconSize()
        Assert.assertEquals(-10.dp, state.iconSizeOffsetDp)

        repeat(3) { state.decreaseIconSize() }
        Assert.assertEquals(-10.dp, state.iconSizeOffsetDp)
    }
    @Test
    fun `cycleColorBlindMode cycles through all modes and back to none`() {
        val state = AccessibilityState(colorBlindMode = ColorBlindMode.NONE)

        state.cycleColorBlindMode()
        Assert.assertEquals(ColorBlindMode.PROTANOPIA, state.colorBlindMode)

        state.cycleColorBlindMode()
        Assert.assertEquals(ColorBlindMode.DEUTERANOPIA, state.colorBlindMode)

        state.cycleColorBlindMode()
        Assert.assertEquals(ColorBlindMode.TRITANOPIA, state.colorBlindMode)

        state.cycleColorBlindMode()
        Assert.assertEquals(ColorBlindMode.NONE, state.colorBlindMode)
    }

    @Test
    fun `setBold turns bold on and off`() {
        val state = AccessibilityState(initialBoldEnabled = false)
        state.setBold(true)
        assertTrue(state.isBoldEnabled)

        state.setBold(false)
        assertFalse(state.isBoldEnabled)
    }
}