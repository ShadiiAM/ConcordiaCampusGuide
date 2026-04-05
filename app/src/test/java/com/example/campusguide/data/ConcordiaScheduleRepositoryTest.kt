package com.example.campusguide.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ConcordiaScheduleRepositoryTest {

    private val apiService: ConcordiaApiService = mock()
    private val repository = ConcordiaScheduleRepository(apiService)

    private fun makeSchedule(subject: String = "COMP", termCode: String = "2241") = CourseSchedule(
        courseID = "1234", termCode = termCode, subject = subject, catalog = "232",
        section = "AA", componentCode = "LEC", classStartTime = "08:45",
        classEndTime = "10:00", classDays = "MWF", buildingCode = "H",
        roomNumber = "851", campus = "SGW"
    )

    @Test
    fun `getSchedule returns result from API on cache miss`() = runTest {
        val schedules = listOf(makeSchedule())
        whenever(apiService.getSchedule("COMP", "2241")).thenReturn(schedules)

        val result = repository.getSchedule("COMP", "2241")

        assertEquals(schedules, result)
    }

    @Test
    fun `getSchedule returns cached result and calls API only once`() = runTest {
        val schedules = listOf(makeSchedule())
        whenever(apiService.getSchedule(any(), any())).thenReturn(schedules)

        repository.getSchedule("COMP", "2241")
        val result = repository.getSchedule("COMP", "2241")

        assertEquals(schedules, result)
        verify(apiService, times(1)).getSchedule("COMP", "2241")
    }

    @Test
    fun `getSchedule uses separate cache entries for different subject and term combinations`() = runTest {
        val compSchedules = listOf(makeSchedule("COMP", "2241"))
        val engrSchedules = listOf(makeSchedule("ENGR", "2241"))
        whenever(apiService.getSchedule("COMP", "2241")).thenReturn(compSchedules)
        whenever(apiService.getSchedule("ENGR", "2241")).thenReturn(engrSchedules)

        val comp = repository.getSchedule("COMP", "2241")
        val engr = repository.getSchedule("ENGR", "2241")

        assertEquals(compSchedules, comp)
        assertEquals(engrSchedules, engr)
    }
}
