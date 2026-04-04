package com.example.campusguide.ui.viewmodels

import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.Course
import com.example.campusguide.ui.screens.CalendarTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: CalendarRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = CalendarViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeCourse(
        classNumber: String = "1234",
        section: String = "AA",
        termCode: String = "2241",
        mondays: String = "N",
        tuesdays: String = "N",
        wednesdays: String = "N",
        thursdays: String = "N",
        fridays: String = "N",
        saturdays: String = "N",
        sundays: String = "N",
        startTime: String = "08:45:00"
    ) = Course(
        subject = "COMP", termCode = termCode, courseID = "1", catalog = "232",
        section = section, componentCode = "LEC", classNumber = classNumber,
        courseTitle = "Algorithms", locationCode = "SGW", buildingCode = "H", room = "851",
        startTime = startTime, endTime = "10:00:00",
        startDate = "2024-01-01", endDate = "2024-04-30",
        mondays = mondays, tuesdays = tuesdays, wednesdays = wednesdays,
        thursdays = thursdays, fridays = fridays, saturdays = saturdays, sundays = sundays
    )

    private suspend fun seedCourse(course: Course) {
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(listOf(course))
        viewModel.updateInput(course.termCode, course.subject, course.catalog, course.section)
        viewModel.addCourse()
    }

    // ---------- updateInput ----------

    @Test
    fun `updateInput sets all four fields`() {
        viewModel.updateInput("2241", "COMP", "232", "AA")
        assertEquals("2241", viewModel.uiState.termCode)
        assertEquals("COMP", viewModel.uiState.subject)
        assertEquals("232", viewModel.uiState.catalog)
        assertEquals("AA", viewModel.uiState.section)
    }

    @Test
    fun `updateInput partial change preserves other fields`() {
        viewModel.updateInput("2241", "COMP", "232", "AA")
        viewModel.updateInput(subject = "ENGR")
        assertEquals("ENGR", viewModel.uiState.subject)
        assertEquals("2241", viewModel.uiState.termCode)
        assertEquals("232", viewModel.uiState.catalog)
        assertEquals("AA", viewModel.uiState.section)
    }

    // ---------- incrementDate ----------

    @Test
    fun `incrementDate advances date by given number of days`() {
        val before = viewModel.selectedDate[Calendar.DAY_OF_YEAR]
        viewModel.incrementDate(3)
        assertEquals(before + 3, viewModel.selectedDate[Calendar.DAY_OF_YEAR])
    }

    @Test
    fun `incrementDate can move date backward`() {
        val before = viewModel.selectedDate[Calendar.DAY_OF_YEAR]
        viewModel.incrementDate(-2)
        assertEquals(before - 2, viewModel.selectedDate[Calendar.DAY_OF_YEAR])
    }

    // ---------- selectedTab setter ----------

    @Test
    fun `switching to different tab clears error and lastAddedCourses`() = runTest {
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(emptyList())
        viewModel.updateInput("2241", "COMP", "232", "AA")
        viewModel.addCourse()
        assertEquals(CalendarError.NotFound, viewModel.uiState.error)

        viewModel.selectedTab = CalendarTab.ADD_COURSE

        assertNull(viewModel.uiState.error)
        assertNull(viewModel.uiState.lastAddedCourses)
    }

    @Test
    fun `switching to same tab does not reset state`() = runTest {
        val course = makeCourse()
        seedCourse(course)
        assertNotNull(viewModel.uiState.lastAddedCourses)

        viewModel.selectedTab = CalendarTab.DAILY_SCHEDULE
        assertNotNull(viewModel.uiState.lastAddedCourses)
    }

    // ---------- addCourse ----------

    @Test
    fun `addCourse success stores course and clears input fields`() = runTest {
        val course = makeCourse()
        whenever(repository.fetchAndFilterCourse("COMP", "232", "2241", "AA")).thenReturn(listOf(course))
        viewModel.updateInput("2241", "COMP", "232", "AA")

        viewModel.addCourse()

        assertFalse(viewModel.uiState.isLoading)
        assertEquals(listOf(course), viewModel.uiState.trackedCourses)
        assertEquals(listOf(course), viewModel.uiState.lastAddedCourses)
        assertEquals("", viewModel.uiState.subject)
        assertEquals("", viewModel.uiState.termCode)
    }

    @Test
    fun `addCourse sets AlreadyTracked error when course already in list`() = runTest {
        val course = makeCourse()
        seedCourse(course)

        viewModel.updateInput("2241", "COMP", "232", "AA")
        viewModel.addCourse()

        assertEquals(CalendarError.AlreadyTracked, viewModel.uiState.error)
    }

    @Test
    fun `addCourse sets NotFound error when repository returns empty list`() = runTest {
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(emptyList())
        viewModel.updateInput("2241", "COMP", "999", "ZZ")

        viewModel.addCourse()

        assertEquals(CalendarError.NotFound, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `addCourse sets Network error on IOException`() = runTest {
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any()))
            .thenAnswer { throw IOException("timeout") }
        viewModel.updateInput("2241", "COMP", "232", "AA")

        viewModel.addCourse()

        assertEquals(CalendarError.Network, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `addCourse sets Unknown error on unexpected exception`() = runTest {
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any()))
            .thenAnswer { throw RuntimeException("crash") }
        viewModel.updateInput("2241", "COMP", "232", "AA")

        viewModel.addCourse()

        assertTrue(viewModel.uiState.error is CalendarError.Unknown)
        assertFalse(viewModel.uiState.isLoading)
    }

    // ---------- removeCourse ----------

    @Test
    fun `removeCourse removes the specified course`() = runTest {
        val course = makeCourse()
        seedCourse(course)

        viewModel.removeCourse(course)

        assertTrue(viewModel.uiState.trackedCourses.isEmpty())
    }

    @Test
    fun `removeCourse keeps other courses intact`() = runTest {
        val c1 = makeCourse(classNumber = "1", section = "AA")
        val c2 = makeCourse(classNumber = "2", section = "BB")
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(listOf(c1))
        viewModel.updateInput("2241", "COMP", "232", "AA")
        viewModel.addCourse()
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(listOf(c2))
        viewModel.updateInput("2241", "COMP", "232", "BB")
        viewModel.addCourse()

        viewModel.removeCourse(c1)

        assertEquals(listOf(c2), viewModel.uiState.trackedCourses)
    }

    // ---------- refreshSchedules ----------

    @Test
    fun `refreshSchedules does nothing when no courses are tracked`() {
        viewModel.refreshSchedules()
        assertFalse(viewModel.uiState.isRefreshing)
    }

    @Test
    fun `refreshSchedules updates courses on successful refresh`() = runTest {
        val course = makeCourse()
        seedCourse(course)
        val fresh = makeCourse(startTime = "09:15:00")
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(listOf(fresh))

        viewModel.refreshSchedules()

        assertFalse(viewModel.uiState.isRefreshing)
        assertFalse(viewModel.uiState.refreshError)
        assertTrue(viewModel.uiState.trackedCourses.contains(fresh))
    }

    @Test
    fun `refreshSchedules keeps original and flags error on IOException`() = runTest {
        val course = makeCourse()
        seedCourse(course)
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any()))
            .thenAnswer { throw IOException("network") }

        viewModel.refreshSchedules()

        assertTrue(viewModel.uiState.refreshError)
        assertFalse(viewModel.uiState.isRefreshing)
        assertEquals(listOf(course), viewModel.uiState.trackedCourses)
    }

    @Test
    fun `refreshSchedules flags error when fresh result is empty`() = runTest {
        val course = makeCourse()
        seedCourse(course)
        whenever(repository.fetchAndFilterCourse(any(), any(), any(), any())).thenReturn(emptyList())

        viewModel.refreshSchedules()

        assertTrue(viewModel.uiState.refreshError)
        assertEquals(listOf(course), viewModel.uiState.trackedCourses)
    }

    // ---------- coursesForSelectedDay ----------

    @Test
    fun `coursesForSelectedDay returns courses that meet on the selected day`() = runTest {
        val dayOfWeek = viewModel.selectedDate[Calendar.DAY_OF_WEEK]
        val course = when (dayOfWeek) {
            Calendar.MONDAY -> makeCourse(mondays = "Y")
            Calendar.TUESDAY -> makeCourse(tuesdays = "Y")
            Calendar.WEDNESDAY -> makeCourse(wednesdays = "Y")
            Calendar.THURSDAY -> makeCourse(thursdays = "Y")
            Calendar.FRIDAY -> makeCourse(fridays = "Y")
            Calendar.SATURDAY -> makeCourse(saturdays = "Y")
            else -> makeCourse(sundays = "Y")
        }
        seedCourse(course)

        assertTrue(viewModel.coursesForSelectedDay.contains(course))
    }

    @Test
    fun `coursesForSelectedDay excludes courses not meeting on selected day`() = runTest {
        val dayOfWeek = viewModel.selectedDate[Calendar.DAY_OF_WEEK]
        val course = if (dayOfWeek == Calendar.MONDAY) makeCourse(tuesdays = "Y") else makeCourse(mondays = "Y")
        seedCourse(course)

        assertTrue(viewModel.coursesForSelectedDay.isEmpty())
    }

    // ---------- jumpToNextClass ----------

    @Test
    fun `jumpToNextClass does nothing when no courses are tracked`() {
        val dayBefore = viewModel.selectedDate[Calendar.DAY_OF_YEAR]
        viewModel.jumpToNextClass()
        assertEquals(dayBefore, viewModel.selectedDate[Calendar.DAY_OF_YEAR])
    }

    @Test
    fun `jumpToNextClass switches to DAILY_SCHEDULE when upcoming course found`() = runTest {
        viewModel.selectedTab = CalendarTab.ADD_COURSE
        val course = makeCourse(
            mondays = "Y", tuesdays = "Y", wednesdays = "Y",
            thursdays = "Y", fridays = "Y", saturdays = "Y", sundays = "Y",
            startTime = "23:59:59"
        )
        seedCourse(course)

        viewModel.jumpToNextClass()

        assertEquals(CalendarTab.DAILY_SCHEDULE, viewModel.selectedTab)
    }

    @Test
    fun `nextUpcomingCourse is null when no courses are tracked`() {
        assertNull(viewModel.nextUpcomingCourse)
    }

    @Test
    fun `nextUpcomingCourse returns today course that has not started yet`() = runTest {
        val course = makeCourse(
            mondays = "Y", tuesdays = "Y", wednesdays = "Y",
            thursdays = "Y", fridays = "Y", saturdays = "Y", sundays = "Y",
            startTime = "23:59:59"
        )
        seedCourse(course)

        val result = viewModel.nextUpcomingCourse

        assertNotNull(result)
        assertEquals(course, result!!.course)
        val today = Calendar.getInstance()
        assertEquals(today[Calendar.DAY_OF_YEAR], result.date[Calendar.DAY_OF_YEAR])
        assertEquals(today[Calendar.YEAR], result.date[Calendar.YEAR])
    }
}
