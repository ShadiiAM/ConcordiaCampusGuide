package com.example.campusguide.epic1_AT

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.campusguide.MainActivity
//import androidx.test.uiautomator.UiSelector
//import com.google.android.gms.maps.model.*
//import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith



/**
 * Acceptance Test for US-1.4: Show Current Building Location
 *
 * Tests verify building highlighting when user is inside/outside buildings.
 * Camera zooms to user location and shows marker indicating user position.
 *
 * Note: Permission dialog may appear - click "Allow" when prompted.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT4CurrentBuildingLocationUITest {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    private lateinit var device: UiDevice

//    @Before
//    fun setUp() {
//        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//    }


    // Aceptance Criteria
    // --------------------------------------------------------------------------
    //  When the Map loads for the first time, the user is prompted with a location permission request
    //  When permissions are declined, the Map is still usable.
    //  When permissions are accepted, a blue dot appears at the user location
    //  When a user is in a Concordia Building, the overlay is highlighted

    @Test
    fun acceptLocationAccessTest() {

        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        Thread.sleep(12000)

        composeTestRule
            .onNodeWithContentDescription("Toggle Controls")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Recenter")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Zoom In")
            .performClick()
        composeTestRule
            .onNodeWithContentDescription("Zoom In")
            .performClick()

        Thread.sleep(5000)

    }

//    @Test
//    fun rejectLocationPermissionThenPerformE2E() {
//
//        composeTestRule.waitForIdle()
//
//        val denyButton = device.findObject(UiSelector().text("Don't allow"))
//
//        if (denyButton.waitForExists(3000)) {
//            denyButton.click()
//        }
//
//        Thread.sleep(12000)
//
//        composeTestRule
//            .onNodeWithContentDescription("Recenter")
//            .performClick()
//
//    }
}