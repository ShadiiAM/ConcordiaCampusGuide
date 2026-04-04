package com.example.campusguide.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CourseScheduleTest {

    @Test
    fun `CourseSchedule stores all fields correctly`() {
        val schedule = CourseSchedule(
            courseID = "1234",
            termCode = "2241",
            subject = "COMP",
            catalog = "232",
            section = "AA",
            componentCode = "LEC",
            classStartTime = "08:45",
            classEndTime = "10:00",
            classDays = "MWF",
            buildingCode = "H",
            roomNumber = "851",
            campus = "SGW"
        )

        assertEquals("1234", schedule.courseID)
        assertEquals("2241", schedule.termCode)
        assertEquals("COMP", schedule.subject)
        assertEquals("232", schedule.catalog)
        assertEquals("AA", schedule.section)
        assertEquals("LEC", schedule.componentCode)
        assertEquals("08:45", schedule.classStartTime)
        assertEquals("10:00", schedule.classEndTime)
        assertEquals("MWF", schedule.classDays)
        assertEquals("H", schedule.buildingCode)
        assertEquals("851", schedule.roomNumber)
        assertEquals("SGW", schedule.campus)
    }

    @Test
    fun `CourseSchedule allows null fields`() {
        val schedule = CourseSchedule(
            courseID = null, termCode = null, subject = null, catalog = null,
            section = null, componentCode = null, classStartTime = null,
            classEndTime = null, classDays = null, buildingCode = null,
            roomNumber = null, campus = null
        )

        assertNull(schedule.courseID)
        assertNull(schedule.termCode)
        assertNull(schedule.buildingCode)
        assertNull(schedule.campus)
    }

    @Test
    fun `CourseSchedule equality holds for identical instances`() {
        val s1 = CourseSchedule(
            "1234", "2241", "COMP", "232", "AA", "LEC",
            "08:45", "10:00", "MWF", "H", "851", "SGW"
        )
        val s2 = CourseSchedule(
            "1234", "2241", "COMP", "232", "AA", "LEC",
            "08:45", "10:00", "MWF", "H", "851", "SGW"
        )

        assertEquals(s1, s2)
    }
}
