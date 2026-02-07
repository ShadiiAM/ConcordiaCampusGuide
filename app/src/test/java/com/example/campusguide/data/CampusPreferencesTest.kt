package com.example.campusguide.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.Campus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Minimal Robolectric test for CampusPreferences.
 * Only tests persistence logic, not full activity lifecycle.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class CampusPreferencesTest {

    private lateinit var prefs: CampusPreferences
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any existing preferences
        context.getSharedPreferences("campus_preferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
        prefs = CampusPreferences(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("campus_preferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun getSavedCampus_defaultsToSGW() {
        assertEquals(Campus.SGW, prefs.getSavedCampus())
    }

    @Test
    fun saveCampus_SGW_thenGet_returnsSGW() {
        prefs.saveCampus(Campus.SGW)
        assertEquals(Campus.SGW, prefs.getSavedCampus())
    }

    @Test
    fun saveCampus_LOYOLA_thenGet_returnsLOYOLA() {
        prefs.saveCampus(Campus.LOYOLA)
        assertEquals(Campus.LOYOLA, prefs.getSavedCampus())
    }

    @Test
    fun saveCampus_overwrite_lastValueWins() {
        prefs.saveCampus(Campus.SGW)
        prefs.saveCampus(Campus.LOYOLA)
        assertEquals(Campus.LOYOLA, prefs.getSavedCampus())
    }

    @Test
    fun getSavedCampus_persistsAcrossInstances() {
        prefs.saveCampus(Campus.LOYOLA)

        // Create new instance
        val newPrefs = CampusPreferences(context)
        assertEquals(Campus.LOYOLA, newPrefs.getSavedCampus())
    }
}
