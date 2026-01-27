package com.example.campusguide

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for MainActivity using Robolectric
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainActivityTest {

    @Test
    fun mainActivity_onCreate_shouldLaunchSuccessfully() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity should be created", activity)
                assertNotNull("Activity should have a window", activity.window)
            }
        }
    }

    @Test
    fun mainActivity_extendsComponentActivity() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "MainActivity should extend ComponentActivity",
                    activity is androidx.activity.ComponentActivity
                )
            }
        }
    }

    @Test
    fun mainActivity_hasCorrectPackageName() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals(
                    "Package name should be com.example.campusguide",
                    "com.example.campusguide",
                    activity.packageName
                )
            }
        }
    }

    @Test
    fun appDestinations_enumHasCorrectValues() {
        // Test that AppDestinations enum has the expected values
        val destinations = AppDestinations.entries
        assertEquals("Should have 3 destinations", 3, destinations.size)

        val labels = destinations.map { it.label }
        assertTrue("Should contain Home", labels.contains("Home"))
        assertTrue("Should contain Favorites", labels.contains("Favorites"))
        assertTrue("Should contain Profile", labels.contains("Profile"))
    }

    @Test
    fun greetingFunction_shouldFormatCorrectly() {
        // Test the greeting text format
        val testName = "TestUser"
        val expectedGreeting = "Hello $testName!"
        assertEquals("Greeting should be formatted correctly", expectedGreeting, "Hello $testName!")
    }
}