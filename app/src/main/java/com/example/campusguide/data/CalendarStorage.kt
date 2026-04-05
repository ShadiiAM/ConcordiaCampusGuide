package com.example.campusguide.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.calendarDataStore: DataStore<Preferences> by preferencesDataStore(name = "calendar_prefs")

class CalendarStorage(private val dataStore: DataStore<Preferences>) {
    private val COURSES_KEY = stringPreferencesKey("tracked_courses")
    private val json = Json { ignoreUnknownKeys = true }

    val trackedCourses: Flow<List<Course>> = dataStore.data.map { prefs ->
        val jsonString = prefs[COURSES_KEY] ?: "[]"
        try {
            json.decodeFromString<List<Course>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCourses(courses: List<Course>) {
        dataStore.edit { prefs ->
            prefs[COURSES_KEY] = json.encodeToString(courses)
        }
    }
}
