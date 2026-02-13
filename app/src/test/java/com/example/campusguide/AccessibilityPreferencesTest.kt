package com.example.campusguide

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityPreferences
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.ColorBlindMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AccessibilityPreferencesTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun accessibilityPreferences_loadsDefaultsWhenEmpty() = runBlocking {
        // When nothing has been explicitly saved, we at least expect offset and mode defaults
        val loaded = AccessibilityPreferences.load(context)

        assertEquals(0f, loaded.textSizeOffsetSp)
        // We don't assert bold/textColor here because prior tests or app runs may have written them.
        assertEquals(ColorBlindMode.NONE, loaded.colorBlindMode)
    }

    @Test
    fun accessibilityPreferences_savesAndLoadsFullState() = runBlocking {
        val original = AccessibilityState(
            initialOffsetSp = 3.5f,
            initialBoldEnabled = true,
            initialTextColor = Color(0xFF123456.toInt()),
            colorBlindMode = ColorBlindMode.DEUTERANOPIA
        )

        AccessibilityPreferences.saveFromState(context, original)
        val loaded = AccessibilityPreferences.load(context)

        assertEquals(original.textSizeOffsetSp, loaded.textSizeOffsetSp)
        assertEquals(original.isBoldEnabled, loaded.isBoldEnabled)
        assertEquals(original.textColor.value.toInt(), loaded.textColor.value.toInt())
        assertEquals(original.colorBlindMode, loaded.colorBlindMode)
    }

    @Test
    fun accessibilityPreferences_persistsBoldToggle() = runBlocking {
        val boldState = AccessibilityState(
            initialOffsetSp = 0f,
            initialBoldEnabled = true,
            initialTextColor = Color.Black,
            colorBlindMode = ColorBlindMode.NONE
        )

        AccessibilityPreferences.saveFromState(context, boldState)
        val loaded = AccessibilityPreferences.load(context)

        assertEquals(true, loaded.isBoldEnabled)
        assertEquals(0f, loaded.textSizeOffsetSp)
        // After saving black explicitly we expect to get black back
        assertEquals(boldState.textColor.value.toInt(), loaded.textColor.value.toInt())
        assertEquals(ColorBlindMode.NONE, loaded.colorBlindMode)
    }
}
