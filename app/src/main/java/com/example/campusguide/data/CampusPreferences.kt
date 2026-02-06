package com.example.campusguide.data

import android.content.Context
import android.content.SharedPreferences
import com.example.campusguide.ui.components.Campus

/**
 * Manages campus selection persistence using SharedPreferences.
 * Extracted from MapsActivity for easier testing.
 */
class CampusPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save the selected campus to preferences
     */
    fun saveCampus(campus: Campus) {
        prefs.edit().putString(KEY_SELECTED_CAMPUS, campus.name).apply()
    }

    /**
     * Get the saved campus, defaults to SGW if none saved
     */
    fun getSavedCampus(): Campus {
        val savedName = prefs.getString(KEY_SELECTED_CAMPUS, Campus.SGW.name)
        return try {
            Campus.valueOf(savedName ?: Campus.SGW.name)
        } catch (e: IllegalArgumentException) {
            Campus.SGW
        }
    }

    companion object {
        private const val PREFS_NAME = "campus_preferences"
        private const val KEY_SELECTED_CAMPUS = "selected_campus"
    }
}
