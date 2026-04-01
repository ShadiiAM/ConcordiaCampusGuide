package com.example.campusguide

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.data.OutsidePOI
import com.example.campusguide.data.POIType
import com.example.campusguide.data.WorkingHours
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.DayOfWeek
import com.example.campusguide.ui.components.POICard
import com.example.campusguide.ui.components.getPOIColorAndDrawable
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class POICardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makePOI(
        name: String = "Test Cafe",
        description: String = "A nice place",
        category: POIType = POIType.Cafe,
        rating: Double = 4.2,
        address: String = "123 Test St",
        openingHour: Double = 8.0,
        closingHour: Double = 18.0,
        closedDays: List<DayOfWeek> = emptyList()
    ) = OutsidePOI(
        name = name,
        description = description,
        category = category,
        workingHours = WorkingHours(openingHour, closingHour, closedDays),
        rating = rating,
        latLng = LatLng(45.497, -73.578),
        address = address
    )

    private fun setCard(poi: OutsidePOI, onDismiss: () -> Unit = {}, onDirections: (() -> Unit)? = null) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides AccessibilityState()) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    POICard(poi = poi, onDismiss = onDismiss, onDirectionsClick = onDirections)
                }
            }
        }
    }

    // ── content display ───────────────────────────────────────────────────────

    @Test
    fun `POICard displays the POI name`() {
        setCard(makePOI(name = "Grand Bistro"))
        composeTestRule.onNodeWithText("Grand Bistro").assertIsDisplayed()
    }

    @Test
    fun `POICard displays the POI category`() {
        setCard(makePOI(category = POIType.Restaurant))
        composeTestRule.onNodeWithText("Restaurant").assertIsDisplayed()
    }

    @Test
    fun `POICard displays the POI rating`() {
        setCard(makePOI(rating = 4.7))
        composeTestRule.onNodeWithText("4.7").assertIsDisplayed()
    }

    @Test
    fun `POICard displays the POI description`() {
        setCard(makePOI(description = "Cozy basement cafe"))
        composeTestRule.onNodeWithText("Cozy basement cafe").assertIsDisplayed()
    }

    @Test
    fun `POICard displays the POI address`() {
        setCard(makePOI(address = "1234 Sherbrooke St W"))
        composeTestRule.onNodeWithText("1234 Sherbrooke St W").assertIsDisplayed()
    }

    @Test
    fun `POICard displays category icon`() {
        setCard(makePOI(category = POIType.Cafe))
        composeTestRule.onNodeWithContentDescription("poiCardIcon").assertIsDisplayed()
    }

    // ── working hours ─────────────────────────────────────────────────────────

    @Test
    fun `POICard displays am for morning opening hour`() {
        setCard(makePOI(openingHour = 8.0, closingHour = 18.0))
        composeTestRule.onNodeWithText("Working Hours: 8:00 am-18:00 pm. \nOpen all week").assertIsDisplayed()
    }

    @Test
    fun `POICard displays half-hour minutes correctly`() {
        setCard(makePOI(openingHour = 7.5, closingHour = 17.5))
        composeTestRule.onNodeWithText("Working Hours: 7:30 am-17:30 pm. \nOpen all week").assertIsDisplayed()
    }

    @Test
    fun `POICard shows Open all week when no closed days`() {
        setCard(makePOI(closedDays = emptyList()))
        composeTestRule.onNodeWithText("Open all week", substring = true).assertIsDisplayed()
    }

    @Test
    fun `POICard shows closed days when present`() {
        setCard(makePOI(closedDays = listOf(DayOfWeek.Monday, DayOfWeek.Sunday)))
        composeTestRule.onNodeWithText("Closed on: Monday, Sunday", substring = true).assertIsDisplayed()
    }

    // ── directions button ─────────────────────────────────────────────────────

    @Test
    fun `POICard shows Directions button when onDirectionsClick is provided`() {
        setCard(makePOI(), onDirections = {})
        composeTestRule.onNodeWithContentDescription("POICardDirections").assertIsDisplayed()
    }

    @Test
    fun `POICard hides Directions button when onDirectionsClick is null`() {
        setCard(makePOI(), onDirections = null)
        composeTestRule.onNodeWithContentDescription("POICardDirections").assertDoesNotExist()
    }

    @Test
    fun `POICard Directions button invokes callback on click`() {
        var clicked = false
        setCard(makePOI(), onDirections = { clicked = true })
        composeTestRule.onNodeWithContentDescription("POICardDirections").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `POICard Directions button calls onDismiss on click`() {
        var dismissed = false
        setCard(makePOI(), onDismiss = { dismissed = true }, onDirections = {})
        composeTestRule.onNodeWithContentDescription("POICardDirections").performClick()
        assertTrue(dismissed)
    }

    // ── getPOIColorAndDrawable ────────────────────────────────────────────────

    @Test
    fun `getPOIColorAndDrawable returns distinct drawables for each type`() {
        val drawables = POIType.entries.map { getPOIColorAndDrawable(it).second }
        assertEquals("Each POIType should have a unique drawable", drawables.size, drawables.toSet().size)
    }

    @Test
    fun `getPOIColorAndDrawable returns distinct colors for each type`() {
        val colors = POIType.entries.map { getPOIColorAndDrawable(it).first }
        assertEquals("Each POIType should have a unique color", colors.size, colors.toSet().size)
    }

    @Test
    fun `getPOIColorAndDrawable returns a result for every POIType`() {
        for (type in POIType.entries) {
            val result = getPOIColorAndDrawable(type)
            assertNotNull(result)
        }
    }
}
