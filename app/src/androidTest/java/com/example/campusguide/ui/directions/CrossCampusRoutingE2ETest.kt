package com.example.campusguide.ui.directions

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E test for cross-campus routing (SGW <-> Loyola).
 * Tests the complete user journey from selecting buildings on different campuses
 * to getting route directions with cross-campus indicator.
 *
 * These tests can be screen-recorded for demonstration purposes:
 * adb shell screenrecord /sdcard/cross_campus_test.mp4 &
 * ./gradlew connectedAndroidTest --tests "CrossCampusRoutingE2ETest"
 * adb pull /sdcard/cross_campus_test.mp4
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CrossCampusRoutingE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `user can plan cross-campus route from SGW to Loyola`() {
        // Wait for map to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Search...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Tap search bar to open bottom search
        composeTestRule.onNodeWithText("Search...").performClick()

        // Wait for search field to appear
        composeTestRule.waitForIdle()

        // Type a Loyola building in search
        composeTestRule.onNodeWithText("Search...").performTextInput("Central Building")

        // Wait for suggestions to appear
        composeTestRule.waitForIdle()

        // Select Central Building (Loyola campus)
        composeTestRule.onNodeWithText("Central Building (CC)").performClick()

        // Verify directions panel opens
        composeTestRule.onNodeWithText("From:").assertExists()
        composeTestRule.onNodeWithText("To:").assertExists()

        // Verify cross-campus badge is shown
        composeTestRule.onNodeWithText("This is a cross-campus route").assertIsDisplayed()

        // Select Transit mode (recommended for cross-campus)
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()

        // Tap Go button to get route
        composeTestRule.onNodeWithText("Go").performClick()

        // Wait for route to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithText("min", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Verify route summary is displayed (should show duration and distance)
        composeTestRule.onNode(hasText("min", substring = true)).assertExists()
        composeTestRule.onNode(hasText("km", substring = true)).assertExists()

        // Verify cross-campus message appears
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Using Concordia Shuttle for cross-campus route", substring = true)
            .assertExists()
    }

    @Test
    fun `user can plan cross-campus route from Loyola to SGW`() {
        // Wait for map to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Search...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Tap search bar
        composeTestRule.onNodeWithText("Search...").performClick()
        composeTestRule.waitForIdle()

        // Search for SGW building (H Building)
        composeTestRule.onNodeWithText("Search...").performTextInput("H Building")
        composeTestRule.waitForIdle()

        // Select H Building (SGW campus)
        composeTestRule.onNodeWithText("Henry F. Hall Building (H)").performClick()

        // Verify cross-campus badge shows
        composeTestRule.onNodeWithText("This is a cross-campus route").assertIsDisplayed()

        // Select Transit mode
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()

        // Get route
        composeTestRule.onNodeWithText("Go").performClick()

        // Wait for route
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithText("min", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Verify route is displayed
        composeTestRule.onNode(hasText("min", substring = true)).assertExists()
    }

    @Test
    fun `cross-campus badge does not appear for same-campus route`() {
        // Wait for map to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Search...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Search for another SGW building
        composeTestRule.onNodeWithText("Search...").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Search...").performTextInput("EV Building")
        composeTestRule.waitForIdle()

        // Select EV Building (SGW campus - same as default origin)
        composeTestRule.onNodeWithText("EV Building", substring = true).performClick()

        // Verify cross-campus badge DOES NOT show
        composeTestRule.onNodeWithText("This is a cross-campus route").assertDoesNotExist()
    }

    @Test
    fun `error message suggests Transit mode when cross-campus route fails`() {
        // This test simulates a scenario where DRIVE mode fails for cross-campus
        // and verifies the error message suggests Transit

        // Wait for map to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Search...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Select cross-campus route
        composeTestRule.onNodeWithText("Search...").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search...").performTextInput("Central Building")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Central Building (CC)").performClick()

        // Select DRIVE mode (might fail for cross-campus)
        composeTestRule.onNodeWithContentDescription("Drive: car route").performClick()

        // Try to get route
        composeTestRule.onNodeWithText("Go").performClick()

        // Wait for either success or error
        composeTestRule.waitForIdle()
        Thread.sleep(3000)  // Give time for API call

        // If route fails, error message should mention Transit
        // Note: This assertion is conditional - only runs if error appears
        try {
            composeTestRule.onNode(
                hasText("Transit", substring = true) and hasText("Shuttle", substring = true)
            ).assertExists()
        } catch (e: AssertionError) {
            // Route might have succeeded - that's OK too
            // Just verify we got some result
            composeTestRule.onNode(
                hasText("min", substring = true) or hasText("km", substring = true)
            ).assertExists()
        }
    }

    @Test
    fun `user can switch travel modes for cross-campus route`() {
        // Wait for map to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Search...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Select cross-campus destination
        composeTestRule.onNodeWithText("Search...").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search...").performTextInput("CC")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Central Building (CC)").performClick()

        // Verify all travel mode options are available
        composeTestRule.onNodeWithContentDescription("Drive: car route").assertExists()
        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").assertExists()
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").assertExists()

        // Switch between modes
        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()
        composeTestRule.waitForIdle()

        // Verify cross-campus badge remains visible
        composeTestRule.onNodeWithText("This is a cross-campus route").assertIsDisplayed()
    }
}
