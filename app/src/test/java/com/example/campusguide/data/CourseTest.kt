package com.example.campusguide.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CourseTest {

    private fun makeCourse(
        mondays: String = "N", tuesdays: String = "N", wednesdays: String = "N",
        thursdays: String = "N", fridays: String = "N",
        saturdays: String = "N", sundays: String = "N"
    ) = Course(
        subject = "COMP", termCode = "2241", courseID = "1", catalog = "232",
        section = "AA", componentCode = "LEC", classNumber = "1234", courseTitle = "Test",
        locationCode = "SGW", buildingCode = "H", room = "851",
        startTime = "08:45:00", endTime = "10:00:00",
        startDate = "2024-01-01", endDate = "2024-04-30",
        mondays = mondays, tuesdays = tuesdays, wednesdays = wednesdays,
        thursdays = thursdays, fridays = fridays, saturdays = saturdays, sundays = sundays
    )

    @Test
    fun `meetsOn returns true for Monday`() {
        assertTrue(makeCourse(mondays = "Y").meetsOn(Calendar.MONDAY))
    }

    @Test
    fun `meetsOn returns false for Monday when not scheduled`() {
        assertFalse(makeCourse().meetsOn(Calendar.MONDAY))
    }

    @Test
    fun `meetsOn returns true for Tuesday`() {
        assertTrue(makeCourse(tuesdays = "Y").meetsOn(Calendar.TUESDAY))
    }

    @Test
    fun `meetsOn returns false for Tuesday when not scheduled`() {
        assertFalse(makeCourse().meetsOn(Calendar.TUESDAY))
    }

    @Test
    fun `meetsOn returns true for Wednesday`() {
        assertTrue(makeCourse(wednesdays = "Y").meetsOn(Calendar.WEDNESDAY))
    }

    @Test
    fun `meetsOn returns true for Thursday`() {
        assertTrue(makeCourse(thursdays = "Y").meetsOn(Calendar.THURSDAY))
    }

    @Test
    fun `meetsOn returns true for Friday`() {
        assertTrue(makeCourse(fridays = "Y").meetsOn(Calendar.FRIDAY))
    }

    @Test
    fun `meetsOn returns true for Saturday`() {
        assertTrue(makeCourse(saturdays = "Y").meetsOn(Calendar.SATURDAY))
    }

    @Test
    fun `meetsOn returns true for Sunday`() {
        assertTrue(makeCourse(sundays = "Y").meetsOn(Calendar.SUNDAY))
    }

    @Test
    fun `meetsOn returns false for unknown day`() {
        assertFalse(makeCourse(mondays = "Y").meetsOn(99))
    }
}
