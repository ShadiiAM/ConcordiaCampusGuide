package com.example.campusguide.ui.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AccessibleText(
    text: String,
    baseFontSizeSp: Float,
    fallbackColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val accessibilityState = LocalAccessibilityState.current

    val finalSize = (baseFontSizeSp + accessibilityState.textSizeOffsetSp)
        .coerceAtLeast(8f)
        .sp
    val weight = if (accessibilityState.isBoldEnabled) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }
    val color = if (accessibilityState.textColor != Color.Unspecified) {
        accessibilityState.textColor
    } else {
        fallbackColor
    }

    Text(
        text = text,
        fontSize = finalSize,
        fontWeight = weight,
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}