package com.example.campusguide.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class PoiUtilsTest {

    @Test
    fun poiDisplayName_bathroomM_returnsMensWashroom() {
        assertEquals("Men's Washroom", poiDisplayName("BATHROOM-M"))
    }

    @Test
    fun poiDisplayName_bathroomF_returnsWomensWashroom() {
        assertEquals("Women's Washroom", poiDisplayName("BATHROOM-F"))
    }

    @Test
    fun poiDisplayName_bathroomWC_returnsAllGenderWashroom() {
        assertEquals("All-Gender Washroom", poiDisplayName("BATHROOM-WC"))
    }

    @Test
    fun poiDisplayName_bathroomMWC_returnsMensAccessibleWashroom() {
        assertEquals("Men's Accessible Washroom", poiDisplayName("BATHROOM-MWC"))
    }

    @Test
    fun poiDisplayName_bathroomFWC_returnsWomensAccessibleWashroom() {
        assertEquals("Women's Accessible Washroom", poiDisplayName("BATHROOM-FWC"))
    }

    @Test
    fun poiDisplayName_waterFountain_returnsWaterFountain() {
        assertEquals("Water Fountain", poiDisplayName("WATER-FOUNTAIN"))
    }

    @Test
    fun poiDisplayName_emergencyStairWithNumber_returnsStaircaseWithNumber() {
        assertEquals("Emergency Staircase 1", poiDisplayName("EMERGENCY-STAIR-1"))
    }

    @Test
    fun poiDisplayName_emergencyStairWithHighNumber_returnsStaircaseWithNumber() {
        assertEquals("Emergency Staircase 12", poiDisplayName("EMERGENCY-STAIR-12"))
    }

    @Test
    fun poiDisplayName_emergencyStairNoNumber_returnsStaircaseOnly() {
        assertEquals("Emergency Staircase", poiDisplayName("EMERGENCY-STAIR"))
    }

    @Test
    fun poiDisplayName_unknownLabel_returnsCapitalisedFallback() {
        assertEquals("Some poi", poiDisplayName("SOME-POI"))
    }

    @Test
    fun poiInferredDescription_bathroomM_returnsMensWashroom() {
        assertEquals("Men's washroom", poiInferredDescription("BATHROOM-M"))
    }

    @Test
    fun poiInferredDescription_bathroomF_returnsWomensWashroom() {
        assertEquals("Women's washroom", poiInferredDescription("BATHROOM-F"))
    }

    @Test
    fun poiInferredDescription_bathroomWC_returnsAllGenderAccessible() {
        assertEquals("All-gender washroom · wheelchair accessible", poiInferredDescription("BATHROOM-WC"))
    }

    @Test
    fun poiInferredDescription_bathroomMWC_returnsMensAccessibleStall() {
        assertEquals("Men's washroom · wheelchair accessible stall available", poiInferredDescription("BATHROOM-MWC"))
    }

    @Test
    fun poiInferredDescription_bathroomFWC_returnsWomensAccessibleStall() {
        assertEquals("Women's washroom · wheelchair accessible stall available", poiInferredDescription("BATHROOM-FWC"))
    }

    @Test
    fun poiInferredDescription_waterFountain_returnsDrinkingWater() {
        assertEquals("Drinking water available", poiInferredDescription("WATER-FOUNTAIN"))
    }

    @Test
    fun poiInferredDescription_emergencyStair_returnsEmergencyExitDescription() {
        assertEquals(
            "Emergency exit staircase · not for regular use",
            poiInferredDescription("EMERGENCY-STAIR-1")
        )
    }

    @Test
    fun poiInferredDescription_emergencyStairNoNumber_returnsEmergencyExitDescription() {
        assertEquals(
            "Emergency exit staircase · not for regular use",
            poiInferredDescription("EMERGENCY-STAIR")
        )
    }

    @Test
    fun poiInferredDescription_unknownLabel_returnsCapitalisedFallback() {
        assertEquals("Some poi", poiInferredDescription("SOME-POI"))
    }
}
