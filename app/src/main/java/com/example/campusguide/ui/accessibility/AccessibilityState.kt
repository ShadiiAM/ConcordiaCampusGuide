package com.example.campusguide.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Small holder for the global text-size offset
class AccessibilityState(
    initialOffsetSp: Float = 0f,
    initialOffsetDp: Dp = 0.dp,
    initialBoldEnabled: Boolean = false,
    initialTextColor: Color = Color.Unspecified,
    colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    // Callback invoked whenever the state changes; default is no-op
    private val onStateChanged: (AccessibilityState) -> Unit = {}
) {
    var textSizeOffsetSp by mutableFloatStateOf(initialOffsetSp)
        private set
    var iconSizeOffsetDp by mutableStateOf(initialOffsetDp)
        private set

    var isBoldEnabled by mutableStateOf(initialBoldEnabled)
        private set

    var textColor by mutableStateOf(initialTextColor)
        private set

    var colorBlindMode by mutableStateOf(colorBlindMode)
        private set

    private fun notifyChanged() {
        onStateChanged(this)
    }

    fun increaseTextSize() {
        if (textSizeOffsetSp < 6f) {
            textSizeOffsetSp += 1f
            notifyChanged()
        }
    }

    fun decreaseTextSize() {
        if (textSizeOffsetSp > -2f) {
            textSizeOffsetSp -= 1f
            notifyChanged()
        }
    }

    fun increaseIconSize() {
        if (iconSizeOffsetDp < 16.dp) {
            iconSizeOffsetDp += 2.dp
            notifyChanged()
        }
    }

    fun decreaseIconSize() {
        if (iconSizeOffsetDp > -10.dp) {
            iconSizeOffsetDp -= 2.dp
            notifyChanged()
        }
    }

    fun setBold(enabled: Boolean) {
        if (isBoldEnabled != enabled) {
            isBoldEnabled = enabled
            notifyChanged()
        }
    }

    fun cycleColorBlindMode() {
        colorBlindMode = when (colorBlindMode) {
            ColorBlindMode.NONE -> ColorBlindMode.PROTANOPIA
            ColorBlindMode.PROTANOPIA -> ColorBlindMode.DEUTERANOPIA
            ColorBlindMode.DEUTERANOPIA -> ColorBlindMode.TRITANOPIA
            ColorBlindMode.TRITANOPIA -> ColorBlindMode.NONE
        }
        notifyChanged()
    }

    fun setFrom(other: AccessibilityState) {
        textSizeOffsetSp = other.textSizeOffsetSp
        isBoldEnabled = other.isBoldEnabled
        textColor = other.textColor
        colorBlindMode = other.colorBlindMode
        notifyChanged()
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
    initialTextColor: Color = Color.Unspecified,
    colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    onStateChanged: (AccessibilityState) -> Unit = {}
): AccessibilityState {
    return remember {
        AccessibilityState(
            initialOffsetSp = initialOffsetSp,
            initialBoldEnabled = initialBoldEnabled,
            initialTextColor = initialTextColor,
            colorBlindMode = colorBlindMode,
            onStateChanged = onStateChanged
        )
    }
}

enum class ColorBlindMode {
    NONE,          // normal vision
    PROTANOPIA,    // red-weak
    DEUTERANOPIA,  // green-weak
    TRITANOPIA     // blue-weak
}
