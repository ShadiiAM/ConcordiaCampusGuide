package com.example.campusguide

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.data.POIFilterValues
import com.example.campusguide.data.POIType
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.POIFilterTags
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class POIFiltersTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setFilters(
        filters: POIFilterValues = POIFilterValues(),
        onTagSelect: (POIType) -> Unit = {},
        onTagDismiss: (POIType) -> Unit = {},
        onRatingClick: (Double) -> Unit = {},
        onDistanceClick: (Float) -> Unit = {}
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides AccessibilityState()) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    POIFilterTags(
                        poiFilters = filters,
                        onPOITagSelect = onTagSelect,
                        onPOITagDismiss = onTagDismiss,
                        onPOIRatingClick = onRatingClick,
                        onPOIDistanceClick = onDistanceClick
                    )
                }
            }
        }
    }

    // ── category icon filters ─────────────────────────────────────────────────

    @Test
    fun `POIFilterTags renders an icon for every POIType`() {
        setFilters()
        for (type in POIType.entries) {
            composeTestRule
                .onNodeWithContentDescription(type.toString() + "_icon_filter")
                .assertIsDisplayed()
        }
    }

    @Test
    fun `POIFilterTags calls onPOITagSelect when unselected category is clicked`() {
        var selected: POIType? = null
        setFilters(onTagSelect = { selected = it })

        composeTestRule
            .onNodeWithContentDescription("Cafe_icon_filter")
            .performClick()

        assertEquals(POIType.Cafe, selected)
    }

    @Test
    fun `POIFilterTags calls onPOITagDismiss when already-selected category is clicked`() {
        var dismissed: POIType? = null
        val filters = POIFilterValues(categoriesIncluded = setOf(POIType.Metro))
        setFilters(filters = filters, onTagDismiss = { dismissed = it })

        composeTestRule
            .onNodeWithContentDescription("Metro_icon_filter")
            .performClick()

        assertEquals(POIType.Metro, dismissed)
    }

    @Test
    fun `POIFilterTags does not call onPOITagSelect when clicking already-selected tag`() {
        var selectCount = 0
        val filters = POIFilterValues(categoriesIncluded = setOf(POIType.Park))
        setFilters(filters = filters, onTagSelect = { selectCount++ })

        composeTestRule
            .onNodeWithContentDescription("Park_icon_filter")
            .performClick()

        assertEquals(0, selectCount)
    }

    // ── rating filter ─────────────────────────────────────────────────────────

    @Test
    fun `POIFilterTags renders rating filter chip`() {
        setFilters()
        composeTestRule.onNodeWithContentDescription("rating_POI_filter").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags shows 0 rating when inactive`() {
        setFilters(filters = POIFilterValues(rating = 0.0))
        composeTestRule.onNodeWithText("0 <").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags shows active rating value`() {
        setFilters(filters = POIFilterValues(rating = 3.0))
        composeTestRule.onNodeWithText("3 <").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags calls onPOIRatingClick with current rating when rating chip clicked`() {
        var capturedRating: Double? = null
        setFilters(
            filters = POIFilterValues(rating = 4.0),
            onRatingClick = { capturedRating = it }
        )

        composeTestRule.onNodeWithContentDescription("rating_POI_filter").performClick()

        assertEquals(4.0, capturedRating)
    }

    // ── distance filter ───────────────────────────────────────────────────────

    @Test
    fun `POIFilterTags renders distance filter chip`() {
        setFilters()
        composeTestRule.onNodeWithContentDescription("distance_POI_filter").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags shows distance in metres when less than 1000m`() {
        setFilters(filters = POIFilterValues(distanceLimit = 500.0f))
        composeTestRule.onNodeWithText("500 m >").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags shows distance in km when exactly divisible by 1000`() {
        setFilters(filters = POIFilterValues(distanceLimit = 2000.0f))
        composeTestRule.onNodeWithText("2 km >").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags calls onPOIDistanceClick with current distanceLimit when chip clicked`() {
        var capturedDistance: Float? = null
        setFilters(
            filters = POIFilterValues(distanceLimit = 750.0f),
            onDistanceClick = { capturedDistance = it }
        )

        composeTestRule.onNodeWithContentDescription("distance_POI_filter").performClick()

        assertEquals(750.0f, capturedDistance)
    }

    // ── POIFilterValues state updates reflected in UI ─────────────────────────

    @Test
    fun `POIFilterTags reflects updated rating in UI after recomposition`() {
        var filters by mutableStateOf(POIFilterValues(rating = 0.0))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides AccessibilityState()) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    POIFilterTags(
                        poiFilters = filters,
                        onPOITagSelect = {},
                        onPOITagDismiss = {},
                        onPOIRatingClick = {},
                        onPOIDistanceClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("0 <").assertIsDisplayed()

        filters = POIFilterValues(rating = 4.0)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("4 <").assertIsDisplayed()
    }

    @Test
    fun `POIFilterTags reflects updated distance in UI after recomposition`() {
        var filters by mutableStateOf(POIFilterValues(distanceLimit = 0.0f))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides AccessibilityState()) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    POIFilterTags(
                        poiFilters = filters,
                        onPOITagSelect = {},
                        onPOITagDismiss = {},
                        onPOIRatingClick = {},
                        onPOIDistanceClick = {}
                    )
                }
            }
        }

        filters = POIFilterValues(distanceLimit = 1000.0f)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 km >").assertIsDisplayed()
    }
}
