package com.example.campusguide.ui.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    baseFontSizeSp: Float,
    fallbackColor: Color = MaterialTheme.colorScheme.onSurface,
    forceFontWeight: FontWeight? = null
) {
    val accessibilityState = LocalAccessibilityState.current

    val finalSize = (baseFontSizeSp + accessibilityState.textSizeOffsetSp)
        .coerceAtLeast(15f)
        .coerceAtMost(23f)
        .sp

    val effectiveWeight = forceFontWeight ?: if (accessibilityState.isBoldEnabled) {
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
        fontWeight = effectiveWeight,
        modifier = modifier,
        color = color
    )
}