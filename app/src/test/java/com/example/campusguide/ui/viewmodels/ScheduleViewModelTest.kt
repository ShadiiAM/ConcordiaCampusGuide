package com.example.campusguide.ui.viewmodels

import com.example.campusguide.data.ConcordiaScheduleRepository
import com.example.campusguide.data.CourseSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ConcordiaScheduleRepository
    private lateinit var viewModel: ScheduleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = ScheduleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeSchedule() = CourseSchedule(
        courseID = "1234", termCode = "2241", subject = "COMP", catalog = "232",
        section = "AA", componentCode = "LEC", classStartTime = "08:45",
        classEndTime = "10:00", classDays = "MWF", buildingCode = "H",
        roomNumber = "851", campus = "SGW"
    )

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is ScheduleUiState.Idle)
    }

    @Test
    fun `loadSchedule emits Success with returned courses`() = runTest {
        val schedules = listOf(makeSchedule())
        whenever(repository.getSchedule(any(), any())).thenReturn(schedules)

        viewModel.loadSchedule("COMP", "2241")

        val state = viewModel.uiState.value
        assertTrue(state is ScheduleUiState.Success)
        assertEquals(schedules, (state as ScheduleUiState.Success).courses)
    }

    @Test
    fun `loadSchedule emits Error with network message on IOException`() = runTest {
        whenever(repository.getSchedule(any(), any())).thenAnswer { throw IOException("refused") }

        viewModel.loadSchedule("COMP", "2241")

        val state = viewModel.uiState.value
        assertTrue(state is ScheduleUiState.Error)
        assertEquals("Network unavailable", (state as ScheduleUiState.Error).message)
    }

    @Test
    fun `loadSchedule emits Error with server message on HttpException`() = runTest {
        val httpException = HttpException(
            Response.error<List<CourseSchedule>>(503, "error".toResponseBody())
        )
        whenever(repository.getSchedule(any(), any())).thenAnswer { throw httpException }

        viewModel.loadSchedule("COMP", "2241")

        val state = viewModel.uiState.value
        assertTrue(state is ScheduleUiState.Error)
        assertEquals("Server error (503)", (state as ScheduleUiState.Error).message)
    }

    @Test
    fun `loadSchedule emits Error with generic message on unexpected exception`() = runTest {
        whenever(repository.getSchedule(any(), any())).thenAnswer { throw RuntimeException("unexpected") }

        viewModel.loadSchedule("COMP", "2241")

        val state = viewModel.uiState.value
        assertTrue(state is ScheduleUiState.Error)
        assertEquals("An unexpected error occurred", (state as ScheduleUiState.Error).message)
    }
}
