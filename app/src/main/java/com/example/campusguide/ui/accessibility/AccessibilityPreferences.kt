package com.example.campusguide.ui.accessibility

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val ACCESSIBILITY_PREFS_NAME = "accessibility_prefs"

private val Context.accessibilityDataStore by preferencesDataStore(name = ACCESSIBILITY_PREFS_NAME)

private object Keys {
    val TEXT_OFFSET = floatPreferencesKey("text_offset_sp")
    val BOLD_ENABLED = booleanPreferencesKey("bold_enabled")
    val TEXT_COLOR = intPreferencesKey("text_color_argb")
    val COLOR_BLIND_MODE = intPreferencesKey("color_blind_mode")
}

object AccessibilityPreferences {

    suspend fun load(context: Context): AccessibilityState {
        val prefs = context.accessibilityDataStore.data.first()

        val offset: Float = prefs[Keys.TEXT_OFFSET] ?: 0f
        val bold: Boolean = prefs[Keys.BOLD_ENABLED] ?: false
        val colorInt: Int? = prefs[Keys.TEXT_COLOR]
        val color: Color = if (colorInt != null) Color(colorInt) else Color.Unspecified
        val modeOrdinal: Int = prefs[Keys.COLOR_BLIND_MODE] ?: ColorBlindMode.NONE.ordinal
        val mode: ColorBlindMode = ColorBlindMode.entries.getOrElse(modeOrdinal) { ColorBlindMode.NONE }

        return AccessibilityState(
            initialOffsetSp = offset,
            initialBoldEnabled = bold,
            initialTextColor = color,
            colorBlindMode = mode
        )
    }

    suspend fun saveFromState(context: Context, state: AccessibilityState) {
        context.accessibilityDataStore.edit { prefs: MutablePreferences ->
            prefs[Keys.TEXT_OFFSET] = state.textSizeOffsetSp
            prefs[Keys.BOLD_ENABLED] = state.isBoldEnabled

            if (state.textColor != Color.Unspecified) {
                prefs[Keys.TEXT_COLOR] = state.textColor.value.toInt()
            }

            prefs[Keys.COLOR_BLIND_MODE] = state.colorBlindMode.ordinal
        }
    }
}
