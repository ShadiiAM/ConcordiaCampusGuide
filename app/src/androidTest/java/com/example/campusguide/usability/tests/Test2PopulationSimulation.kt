package com.example.campusguide.usability.tests

import android.Manifest
import android.util.Log
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UsabilityTracker
import com.example.campusguide.usability.UserProfile
import org.junit.Rule
import org.junit.Test



class Test2PopulationSimulation {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun runBatch(range: IntRange) {
        range.forEach { iteration ->

            runSimulatedAccessibilityChangeTest2(SimulatedUser(UserProfile.entries.random()), composeTestRule)
            composeTestRule.activityRule.scenario.recreate()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodes(isRoot()).fetchSemanticsNodes()


            val rt = Runtime.getRuntime()
            val usedMB = (rt.totalMemory() - rt.freeMemory()) / 1048576L
            val maxMB = rt.maxMemory() / 1048576L
            Log.d("MEM", "Iteration $iteration: ${usedMB}MB / ${maxMB}MB")


            Thread.sleep(2000)
            Runtime.getRuntime().gc()
            Runtime.getRuntime().gc()

            val usedAfterMB = (rt.totalMemory() - rt.freeMemory()) / 1048576L
            Log.d("MEM", "Iteration $iteration AFTER GC: ${usedAfterMB}MB / ${maxMB}MB")

        }
        Log.d("UsabilityTracker", "About to dump results")
        UsabilityTracker.dumpToFile()
    }

    @Test fun runPopulationSimulation_batch1() = runBatch(0..24)

    @Test fun runPopulationSimulation_batch2() = runBatch(25..49)
}