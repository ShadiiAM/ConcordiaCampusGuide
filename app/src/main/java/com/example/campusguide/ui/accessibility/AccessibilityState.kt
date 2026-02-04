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
class AccessibilityState(
    initialOffsetSp: Float = 0f,
    initialBoldEnabled: Boolean = false,
    initialTextColor: Color = Color.Unspecified

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