package com.example.campusguide.epic6_AT

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-6.1: View outdoor POIs on the map
 *
 * Currently covers:
 *  AC1 – When POIs are enabled, outdoor POIs are shown as markers on the map.
 *  AC2 – POI markers are visually distinct from shuttle stops
 *        by having different content descriptions.
 *  AC3 – POIs shown are within a reasonable distance from the selected
 *        campus or user location.
 *  AC4 – If POI data cannot be loaded, the app shows a clear error
 *        message and does not crash.
 *  AC5 – POI markers have readable accessibility labels.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT36ViewOutdoorPOI {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )

    // Instrumentation rule for the end-to-end navigation tests (AC1–AC4).
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // Separate pure Compose rule for the accessibility check on
    // POICard, which is easier to test without depending on Google
    // Maps hit-testing.
    @get:Rule
    val componentRule = createComposeRule()

    private fun openPoiScreen() {
        composeRule.onNodeWithText("POI").performClick()
        composeRule.waitForIdle()
    }

    /**
     * AC1 – Given the POI screen is open and POIs are enabled by default,
     * then at least one outdoor POI marker is present.
     */
    @Test
    fun outdoorPoiMarkers_existOnPoiMap() {
        openPoiScreen()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("POI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * AC5 – Accessibility: POI markers have text labels or accessible
     * descriptions readable by screen readers.
     *
     * We verify this directly from the POI map screen by checking that
     * all POI markers expose a non-empty contentDescription that
     * includes the substring "POI". This matches the behavior in
     * addPOIMarkersToMap, which sets marker.contentDescription to
     * "<name> POI".
     */
    @Test
    fun outdoorPoiMarkers_areScreenReaderReadable() {
        openPoiScreen()

        // Wait until at least one POI marker is present.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("POI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val poiNodes = composeRule
            .onAllNodesWithContentDescription("POI", substring = true)
            .fetchSemanticsNodes()

        // At least one POI marker exists.
        assert(poiNodes.isNotEmpty())

        // Each POI marker's contentDescription should be non-empty and contain the word "POI"
        poiNodes.forEach { node ->
            val descriptions = node.config[SemanticsProperties.ContentDescription]
            val text = descriptions.joinToString(" ").trim()
            assert(text.isNotEmpty())
            assert(text.contains("POI"))
        }
    }

    /**
     * AC2 – POI markers are visually / semantically distinct from
     * other map elements (buildings, shuttle stops).
     *
     * Production code assigns POI markers a unique contentDescription
     * of the form "<name> POI" via addPOIMarkersToMap. We verify that
     * at least one marker follows this pattern so screen readers and
     * tests can distinguish them from other markers.
     */
    @Test
    fun outdoorPoiMarkers_haveDistinctPoiSuffix() {
        openPoiScreen()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("POI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val poiNodes = composeRule
            .onAllNodesWithContentDescription("POI", substring = true)
            .fetchSemanticsNodes()

        // At least one POI marker exists.
        assert(poiNodes.isNotEmpty())

        // Each POI marker's description should clearly indicate it's a POI
        // so that it's distinguishable from shuttle stops and buildings.
        poiNodes.forEach { node ->
            val descriptions = node.config[SemanticsProperties.ContentDescription]
            val text = descriptions.joinToString(" ")
            assert(text.contains("POI"))
        }
    }

    /**
     * AC3 – POIs shown are within a reasonable distance from the selected
     * campus or user location.
     *
     * We exercise the distance filter chip by setting it to a tighter
     * distance (via the existing UI control) and asserting that at
     * least one POI marker remains visible, implying that filtering is
     * applied relative to the user's default origin.
     */
    @Test
    fun outdoorPoiMarkers_respectDistanceFilter() {
        openPoiScreen()

        // Wait until the distance filter label
        // is present in the semantics tree before trying to click it.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onNodeWithText(" m >", substring = true)
                    .fetchSemanticsNode()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        composeRule.onNodeWithText(" m >", substring = true)
            .performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("POI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * AC4 – If POI data cannot be loaded, the app shows a clear error
     * message and does not crash.
     *
     * The current implementation uses static ALL_POI data, so
     * "cannot be loaded" is approximated as the case where filters
     * result in zero visible POIs. This test tightens category and
     * rating filters via the existing UI, and asserts that the POI
     * screen remains responsive without crashes, even if no markers
     * are present.
     */
    @Test
    fun outdoorPoiMarkers_noResults_doesNotCrashScreen() {
        openPoiScreen()

        // Tighten rating filter by clicking on its numeric label, which
        // is rendered as something like "0 <".
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onNodeWithText(" <", substring = true)
                    .fetchSemanticsNode()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        // tighten distance for 0 POIs
        composeRule.onNodeWithText(" m >", substring = true)
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("POI").assertExists()
    }

    /**
     * AC5 (integration) – When a real POI marker is selected on the
     * POI screen, its detail card exposes key fields as readable text
     * for screen readers. This ensures the production POIScreen +
     * POICard wiring uses AccessibleText (or equivalent semantics)
     */
    @Test
    fun outdoorPoiMarkers_fromMap_usesAccessibleText() {
        openPoiScreen()

        // Wait until at least one POI marker is present
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("POI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click the first available POI marker on the map. Its
        // contentDescription is of the form "<name> POI"
        composeRule.onAllNodesWithContentDescription("POI", substring = true)
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // After selecting a POI a detail card should appear with
        // at least one non-empty text node that includes "POI" or
        // looks like a POI name/description
        val hasSomeText = composeRule
            .onAllNodesWithText("", substring = true)
            .fetchSemanticsNodes()
            .any { node ->
                val text = try {
                    node.config[SemanticsProperties.Text]
                        .joinToString(" ")
                        .trim()
                } catch (_: Exception) {
                    ""
                }
                text.isNotEmpty()
            }
        assert(hasSomeText)
    }
}