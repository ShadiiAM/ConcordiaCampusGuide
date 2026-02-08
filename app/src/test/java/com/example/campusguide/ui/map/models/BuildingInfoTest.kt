package com.example.campusguide.ui.map.models

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for BuildingInfo data class and JSON parsing.
 * Tests data validation, null safety, and proper parsing from GeoJSON properties.
 */
class BuildingInfoTest {

    @Test
    fun fromJson_withAllFields_returnsCompleteBuilding() {
        val json = JSONObject("""
            {
                "buildingCode": "H",
                "buildingName": "Henry F. Hall Building",
                "address": "1455 De Maisonneuve Blvd. W",
                "campus": "SGW",
                "departments": "Economics, Political Science",
                "services": "Campus Safety, Student Union",
                "venues": "Concordia Theatre, Reggies",
                "accessibility": "Accessible entrance, Accessible elevator",
                "notes": "Main academic building",
                "hours": "Monday-Friday: 7:00 AM - 11:00 PM"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("H", result?.buildingCode)
        assertEquals("Henry F. Hall Building", result?.buildingName)
        assertEquals("1455 De Maisonneuve Blvd. W", result?.address)
        assertEquals("SGW", result?.campus)
        assertEquals("Economics, Political Science", result?.departments)
        assertEquals("Campus Safety, Student Union", result?.services)
        assertEquals("Concordia Theatre, Reggies", result?.venues)
        assertEquals("Accessible entrance, Accessible elevator", result?.accessibility)
        assertEquals("Main academic building", result?.notes)
        assertEquals("Monday-Friday: 7:00 AM - 11:00 PM", result?.hours)
    }

    @Test
    fun fromJson_withMinimalFields_returnsBuilding() {
        val json = JSONObject("""
            {
                "buildingCode": "MB"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("MB", result?.buildingCode)
        assertNull(result?.buildingName)
        assertNull(result?.address)
        assertNull(result?.campus)
        assertNull(result?.departments)
        assertNull(result?.services)
        assertNull(result?.venues)
        assertNull(result?.accessibility)
        assertNull(result?.notes)
        assertNull(result?.hours)
    }

    @Test
    fun fromJson_withEmptyStrings_returnsNullForEmptyFields() {
        val json = JSONObject("""
            {
                "buildingCode": "EV",
                "buildingName": "",
                "address": "   ",
                "campus": "SGW",
                "departments": "",
                "hours": ""
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("EV", result?.buildingCode)
        assertNull(result?.buildingName) // Empty string becomes null
        assertNull(result?.address) // Whitespace-only becomes null
        assertEquals("SGW", result?.campus)
        assertNull(result?.departments)
        assertNull(result?.hours)
    }

    @Test
    fun fromJson_withWhitespace_trimsValues() {
        val json = JSONObject("""
            {
                "buildingCode": "  LB  ",
                "buildingName": "  J.W. McConnell Building  ",
                "campus": "  SGW  "
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("LB", result?.buildingCode) // Trimmed
        assertEquals("J.W. McConnell Building", result?.buildingName) // Trimmed
        assertEquals("SGW", result?.campus) // Trimmed
    }

    @Test
    fun fromJson_nullJson_returnsNull() {
        val result = BuildingInfo.fromJson(null)
        assertNull(result)
    }

    @Test
    fun fromJson_missingBuildingCode_returnsNull() {
        val json = JSONObject("""
            {
                "buildingName": "Some Building",
                "address": "123 Main St"
            }
        """)

        val result = BuildingInfo.fromJson(json)
        assertNull(result)
    }

    @Test
    fun fromJson_emptyBuildingCode_returnsNull() {
        val json = JSONObject("""
            {
                "buildingCode": "",
                "buildingName": "Some Building"
            }
        """)

        val result = BuildingInfo.fromJson(json)
        assertNull(result)
    }

    @Test
    fun fromJson_whitespaceOnlyBuildingCode_returnsNull() {
        val json = JSONObject("""
            {
                "buildingCode": "   ",
                "buildingName": "Some Building"
            }
        """)

        val result = BuildingInfo.fromJson(json)
        assertNull(result)
    }

    @Test
    fun fromJson_withHours_parsesHoursCorrectly() {
        val json = JSONObject("""
            {
                "buildingCode": "PC",
                "buildingName": "PERFORM Centre",
                "hours": "Monday-Friday: 6:00 AM - 11:00 PM\nSaturday-Sunday: 8:00 AM - 10:00 PM\nCheck concordia.ca/recreation for current schedules"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("PC", result?.buildingCode)
        assertTrue(result?.hours?.contains("Monday-Friday") == true)
        assertTrue(result?.hours?.contains("concordia.ca/recreation") == true)
    }

    @Test
    fun fromJson_with24HourAccess_parsesCorrectly() {
        val json = JSONObject("""
            {
                "buildingCode": "GN",
                "buildingName": "Grey Nuns Building",
                "hours": "24/7 access for residents\nVisitor access restrictions apply"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("GN", result?.buildingCode)
        assertTrue(result?.hours?.contains("24/7") == true)
        assertTrue(result?.hours?.contains("residents") == true)
    }

    @Test
    fun fromJson_realWorldExample_SGW() {
        val json = JSONObject("""
            {
                "buildingCode": "H",
                "buildingName": "Henry F. Hall Building",
                "address": "1455 De Maisonneuve Blvd. W.",
                "campus": "SGW",
                "departments": "Economics,\n    Geography, Planning and Environment,\n    Political Science",
                "services": "Campus Safety and Prevention Services,\n    Concordia Student Union (CSU)",
                "venues": "Concordia Theatre,\n    Reggies",
                "accessibility": "Accessible entrance, Accessible building elevator",
                "hours": "Monday-Friday: 7:00 AM - 11:00 PM\nSaturday-Sunday: 7:00 AM - 9:00 PM\nAfter-hours access for authorized personnel"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("H", result?.buildingCode)
        assertEquals("Henry F. Hall Building", result?.buildingName)
        assertTrue(result?.departments?.contains("Economics") == true)
        assertTrue(result?.services?.contains("Campus Safety") == true)
        assertTrue(result?.venues?.contains("Reggies") == true)
        assertTrue(result?.hours?.contains("7:00 AM") == true)
    }

    @Test
    fun fromJson_realWorldExample_Loyola() {
        val json = JSONObject("""
            {
                "buildingCode": "SP",
                "buildingName": "Richard J. Renaud Science Complex",
                "address": "7141 Sherbrooke St. W.",
                "campus": "Loyola",
                "departments": "Biology,\n    Chemistry and Biochemistry,\n    Physics",
                "services": "Cafe,\n    Campus Safety and Prevention Services",
                "accessibility": "Accessibility ramp, Accessible entrance, Accessible building elevator",
                "hours": "Monday-Friday: 7:00 AM - 11:00 PM\nSaturday-Sunday: 7:00 AM - 9:00 PM\nAfter-hours access for authorized personnel"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("SP", result?.buildingCode)
        assertEquals("Richard J. Renaud Science Complex", result?.buildingName)
        assertEquals("Loyola", result?.campus)
        assertTrue(result?.departments?.contains("Biology") == true)
        assertTrue(result?.hours?.contains("Monday-Friday") == true)
    }

    @Test
    fun fromJson_multiAddressBuilding_parsesCorrectly() {
        val json = JSONObject("""
            {
                "buildingCode": "FB",
                "buildingName": "Faubourg Building",
                "address": "1250 Guy St., 1600 Ste-Catherine St. W.",
                "campus": "SGW"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertEquals("FB", result?.buildingCode)
        assertTrue(result?.address?.contains("1250 Guy St.") == true)
        assertTrue(result?.address?.contains("1600 Ste-Catherine St. W.") == true)
    }

    @Test
    fun fromJson_withMultilineFields_preservesNewlines() {
        val json = JSONObject("""
            {
                "buildingCode": "EV",
                "departments": "Art Education,\n    Art History,\n    Studio Arts",
                "services": "Le Gym,\n    Zen Den"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertTrue(result?.departments?.contains("\n") == true)
        assertTrue(result?.services?.contains("\n") == true)
    }

    @Test
    fun fromJson_libraryHours_withWebsiteLink() {
        val json = JSONObject("""
            {
                "buildingCode": "LB",
                "buildingName": "J.W. McConnell Building",
                "hours": "Monday-Friday: 8:00 AM - 10:00 PM\nSaturday-Sunday: 10:00 AM - 6:00 PM\nCheck library.concordia.ca for current hours"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertTrue(result?.hours?.contains("library.concordia.ca") == true)
    }

    @Test
    fun fromJson_administrativeHours_closedWeekends() {
        val json = JSONObject("""
            {
                "buildingCode": "AD",
                "buildingName": "Administration Building",
                "hours": "Monday-Friday: 9:00 AM - 5:00 PM\nClosed weekends and holidays"
            }
        """)

        val result = BuildingInfo.fromJson(json)

        assertNotNull(result)
        assertTrue(result?.hours?.contains("Closed weekends") == true)
    }
}
