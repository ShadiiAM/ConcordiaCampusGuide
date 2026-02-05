package com.example.campusguide.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Small holder for the global text-size offset
data class AccessibilityState(
    var initialOffsetSp: Float = 0f,
    var initialBoldEnabled: Boolean = false,
    var initialTextColor: Color = Color.Unspecified,
    var colorBlindMode: ColorBlindMode = ColorBlindMode.NONE

) {
    var textSizeOffsetSp by mutableFloatStateOf(initialOffsetSp)
        private set

    var isBoldEnabled by mutableStateOf(initialBoldEnabled)
        private set

    var textColor by mutableStateOf(initialTextColor)
        private set

    fun increaseTextSize() {
        textSizeOffsetSp += 1f
    }

    fun decreaseTextSize() {
        if (textSizeOffsetSp > 0f) {
            textSizeOffsetSp -= 1f
        }
    }

    fun setBold(enabled: Boolean) {
        isBoldEnabled = enabled
    }

    fun updateTextColor(color: Color) { textColor = color }

    fun isColorFilterEnabled(): Boolean {
        return colorBlindMode != ColorBlindMode.NONE
    }

    fun changeColorBlindMode() {
        colorBlindMode = if (colorBlindMode == ColorBlindMode.NONE){
            ColorBlindMode.HIGH_CONTRAST
        } else {
            ColorBlindMode.NONE
        }
    }
}

// CompositionLocal to access it from any composable
val LocalAccessibilityState = staticCompositionLocalOf<AccessibilityState> {
    error("AccessibilityState not provided")
}

// Helper to create it once at the app root
@Composable
fun rememberAccessibilityState(
    initialOffsetSp: Float = 0f,
    initialBoldEnabled: Boolean = false,
    initialTextColor: Color = Color.Unspecified
): AccessibilityState = remember {
    AccessibilityState(initialOffsetSp, initialBoldEnabled, initialTextColor)
}

enum class ColorBlindMode {
    NONE,
    HIGH_CONTRAST
}