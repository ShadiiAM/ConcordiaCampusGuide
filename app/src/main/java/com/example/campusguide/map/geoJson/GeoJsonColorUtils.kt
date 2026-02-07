package com.example.campusguide.map.geoJson

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

object GeoJsonColorUtils {

    @ColorInt
    fun parse(hex: String): Int = Color.parseColor(hex.trim())

    /** Multiply existing alpha by opacity (0..1). */
    @ColorInt
    fun withOpacity(@ColorInt color: Int, opacity: Float): Int {
        val op = opacity.coerceIn(0f, 1f)
        val a = (Color.alpha(color) * op).roundToInt().coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (a shl 24)
    }

    fun floatOrNull(any: Any?): Float? = when (any) {
        is Number -> any.toFloat()
        is String -> any.toFloatOrNull()
        else -> null
    }

    fun stringOrNull(any: Any?): String? = when (any) {
        is String -> any
        else -> any?.toString()
    }
}