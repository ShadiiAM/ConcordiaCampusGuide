package com.example.campusguide

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MainActivity
 */
class MainActivityTest {

    @Test
    fun mainActivity_className_isCorrect() {
        // Verify activity class name
        val activityName = MainActivity::class.simpleName
        assertEquals("MainActivity", activityName)
    }

    @Test
    fun mainActivity_packageName_isCorrect() {
        // Verify package name
        val packageName = MainActivity::class.java.`package`?.name
        assertEquals("com.example.campusguide", packageName)
    }
}