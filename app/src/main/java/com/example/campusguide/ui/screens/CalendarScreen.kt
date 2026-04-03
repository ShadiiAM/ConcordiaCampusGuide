package com.example.campusguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.R
import com.example.campusguide.ServiceLocator
import com.example.campusguide.data.Course
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.components.ignoreFocusClearOnTouch
import com.example.campusguide.ui.theme.success
import com.example.campusguide.ui.viewmodels.CalendarError
import com.example.campusguide.ui.viewmodels.CalendarUiState
import com.example.campusguide.ui.viewmodels.CalendarViewModel
import com.example.campusguide.ui.viewmodels.NextCourseResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CalendarTab(val labelResId: Int) {
    DAILY_SCHEDULE(R.string.calendar_daily_schedule),
    COURSE_LIST(R.string.calendar_course_list),
    ADD_COURSE(R.string.calendar_add_course)
}

@Composable
fun CalendarScreen(onDirectionsToCourse: (Course) -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel {
        CalendarViewModel(
            ServiceLocator.calendarRepository,
            ServiceLocator.getCalendarStorage(context)
        )
    }

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CalendarHeader(
            selectedTab = viewModel.selectedTab,
            onTabSelected = { viewModel.selectedTab = it }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (viewModel.selectedTab) {
                CalendarTab.DAILY_SCHEDULE -> DailyScheduleView(
                    date = viewModel.selectedDate,
                    coursesForDay = viewModel.coursesForSelectedDay,
                    nextUpcoming = viewModel.nextUpcomingCourse,
                    onIncrementDate = { viewModel.incrementDate(it) },
                    onFindNextClass = { viewModel.jumpToNextClass() },
                    onDirectionsToCourse = onDirectionsToCourse
                )
                CalendarTab.COURSE_LIST -> CourseListView(
                    courses = uiState.trackedCourses,
                    isRefreshing = uiState.isRefreshing,
                    refreshError = uiState.refreshError,
                    onRemoveCourse = { viewModel.removeCourse(it) },
                    onRefresh = { viewModel.refreshSchedules() }
                )
                CalendarTab.ADD_COURSE -> AddCourseView(
                    uiState = uiState,
                    onInputUpdate = { term, sub, cat, sec ->
                        viewModel.updateInput(term, sub, cat, sec)
                    },
                    onAddCourse = { viewModel.addCourse() }
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun DailyScheduleView(
    date: Calendar,
    coursesForDay: List<Course>,
    nextUpcoming: NextCourseResult?,
    onIncrementDate: (Int) -> Unit,
    onFindNextClass: () -> Unit,
    onDirectionsToCourse: (Course) -> Unit = {}
) {
    val sortedCourses = remember(coursesForDay) {
        coursesForDay.sortedBy { it.startTime }
    }

    val isShowingNextDay = remember(date, nextUpcoming) {
        nextUpcoming != null &&
        date.get(Calendar.YEAR) == nextUpcoming.date.get(Calendar.YEAR) &&
        date.get(Calendar.DAY_OF_YEAR) == nextUpcoming.date.get(Calendar.DAY_OF_YEAR)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DateSelector(date, onIncrementDate)

        Box(modifier = Modifier.weight(1f)) {
            if (sortedCourses.isEmpty()) {
                EmptyStateMessage(stringResource(R.string.calendar_empty_schedule))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(sortedCourses) { course ->
                        val isHighlighted = isShowingNextDay && course == nextUpcoming?.course
                        CourseCard(
                            course = course,
                            isUpcoming = isHighlighted,
                            onActionClick = { onDirectionsToCourse(course) }
                        )
                    }
                }
            }
        }

        // Find Next Class Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (nextUpcoming == null) {
                    AccessibleText(
                        text = stringResource(R.string.calendar_no_upcoming_classes),
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        baseFontSizeSp = 14f,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = onFindNextClass,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    AccessibleText(
                        text = stringResource(R.string.calendar_find_next_class),
                        fallbackColor = Color.White,
                        baseFontSizeSp = 16f
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseListView(
    courses: List<Course>,
    isRefreshing: Boolean,
    refreshError: Boolean,
    onRemoveCourse: (Course) -> Unit,
    onRefresh: () -> Unit
) {
    val confirmingCourseId = remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // if user does not confirm removal then it resets
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            confirmingCourseId.value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccessibleText(
                text = if (isRefreshing) stringResource(R.string.calendar_refreshing) else "",
                baseFontSizeSp = 14f,
                fallbackColor = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onRefresh, enabled = !isRefreshing && courses.isNotEmpty()) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Schedules")
                }
            }
        }

        if (refreshError) {
            AccessibleText(
                text = stringResource(R.string.calendar_refresh_failed),
                baseFontSizeSp = 14f,
                fallbackColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (courses.isEmpty()) {
            EmptyStateMessage(stringResource(R.string.calendar_empty_course_list))
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses, key = { it.subject + it.catalog + it.section + it.termCode + it.componentCode }) { course ->
                    val courseId = "${course.subject}-${course.catalog}-${course.section}-${course.termCode}-${course.componentCode}"
                    val isConfirming = confirmingCourseId.value == courseId
                    CourseCard(
                        course = course,
                        showRemoveAction = true,
                        isConfirmingAction = isConfirming,
                        onActionClick = {
                            if (isConfirming) {
                                onRemoveCourse(course)
                            } else {
                                confirmingCourseId.value = courseId
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCourseView(
    uiState: CalendarUiState,
    onInputUpdate: (String, String, String, String) -> Unit,
    onAddCourse: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .ignoreFocusClearOnTouch(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledTextField(
            label = stringResource(R.string.calendar_input_term_label),
            placeholder = stringResource(R.string.calendar_input_term_placeholder),
            value = uiState.termCode,
            onValueChange = { onInputUpdate(it, uiState.subject, uiState.catalog, uiState.section) }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_subject_label),
            placeholder = stringResource(R.string.calendar_input_subject_placeholder),
            value = uiState.subject,
            onValueChange = { onInputUpdate(uiState.termCode, it, uiState.catalog, uiState.section) }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_catalog_label),
            placeholder = stringResource(R.string.calendar_input_catalog_placeholder),
            value = uiState.catalog,
            onValueChange = { onInputUpdate(uiState.termCode, uiState.subject, it, uiState.section) }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_section_label),
            placeholder = stringResource(R.string.calendar_input_section_placeholder),
            value = uiState.section,
            onValueChange = { onInputUpdate(uiState.termCode, uiState.subject, uiState.catalog, it) }
        )
        
        Spacer(modifier = Modifier.weight(1.0f))

        uiState.lastAddedCourses?.let { SuccessFeedback(courses = it) }
        
        uiState.error?.let { errorType ->
            val errorMsg = when(errorType) {
                is CalendarError.NotFound -> stringResource(R.string.calendar_error_not_found)
                is CalendarError.Network -> stringResource(R.string.calendar_error_network)
                is CalendarError.AlreadyTracked -> stringResource(R.string.calendar_error_already_tracked)
                is CalendarError.Unknown -> errorType.message
            }
            AccessibleText(text = errorMsg, fallbackColor = MaterialTheme.colorScheme.error, baseFontSizeSp = 14f)
        }

        Button(
            onClick = { onAddCourse() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.scrim),
            shape = RoundedCornerShape(8.dp)
        ) {
            AccessibleText(text = stringResource(R.string.calendar_add_course_button), baseFontSizeSp = 16f, fallbackColor = Color.White)
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    isUpcoming: Boolean = false,
    isPast: Boolean = false,
    showRemoveAction: Boolean = false,
    isConfirmingAction: Boolean = false,
    showActionButton: Boolean = true,
    onActionClick: () -> Unit = {}
) {
    val alpha = if (isPast) 0.5f else 1f
    val colorScheme = MaterialTheme.colorScheme

    // Highlight color for next class
    val highlightColor = Color.Red

    // Button colors based on confirmation state
    val buttonContainerColor = if (showRemoveAction && isConfirmingAction) {
        colorScheme.error
    } else {
        colorScheme.secondaryContainer
    }

    val buttonContentColor = if (showRemoveAction && isConfirmingAction) {
        colorScheme.onError
    } else {
        colorScheme.onSecondaryContainer
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isUpcoming) {
            AccessibleText(
                text = "NEXT CLASS",
                baseFontSizeSp = 12f,
                forceFontWeight = FontWeight.Bold,
                fallbackColor = highlightColor,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isUpcoming) 3.dp else 1.dp,
                    color = if (isUpcoming) highlightColor else colorScheme.outlineVariant.copy(alpha = alpha),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = alpha))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val header = stringResource(R.string.calendar_course_header, course.subject, course.catalog, course.componentCode, course.section)
                AccessibleText(text = header, baseFontSizeSp = 18f, forceFontWeight = FontWeight.Bold)
                AccessibleText(text = course.courseTitle, baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)

                val locationText = if (course.buildingCode.isBlank() || course.room.isBlank()) {
                    stringResource(R.string.calendar_location_unknown)
                } else {
                    "${course.locationCode}, ${course.buildingCode} ${course.room}"
                }
                AccessibleText(text = stringResource(R.string.calendar_location_label, locationText), baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)

                AccessibleText(text = "${course.startTime} - ${course.endTime}", baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)

                if (showActionButton) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonContainerColor,
                            contentColor = buttonContentColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        val buttonText = when {
                            !showRemoveAction -> stringResource(R.string.calendar_directions)
                            isConfirmingAction -> stringResource(R.string.calendar_confirm_removal)
                            else -> stringResource(R.string.calendar_remove_course)
                        }
                        AccessibleText(text = buttonText, baseFontSizeSp = 12f)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessFeedback(courses: List<Course>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AccessibleText(
            text = stringResource(R.string.calendar_success_added), 
            fallbackColor = success,
            forceFontWeight = FontWeight.Bold, 
            baseFontSizeSp = 14f
        )
        courses.forEach { course ->
            CourseCard(
                course = course,
                showActionButton = false
            )
        }
    }
}

@Composable
fun LabeledTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AccessibleText(text = label, baseFontSizeSp = 14f, forceFontWeight = FontWeight.Medium)
        OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
    }
}

@Composable
private fun CalendarHeader(selectedTab: CalendarTab, onTabSelected: (CalendarTab) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            CalendarTab.entries.forEach { tab ->
                TabButton(text = stringResource(tab.labelResId), isSelected = selectedTab == tab, onClick = { onTabSelected(tab) })
            }
        }
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = if (isSelected) colorScheme.secondary else colorScheme.secondaryContainer, modifier = Modifier.height(32.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            AccessibleText(text = text, baseFontSizeSp = 11f, fallbackColor = if (isSelected) Color.White else colorScheme.secondary)
        }
    }
}

@Composable
private fun DateSelector(date: Calendar, onIncrementDate: (Int) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onIncrementDate(-1) }) { 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Day") 
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val dayOfWeek = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            val month = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)

            val label = "$dayOfWeek, $month ${dayOfMonth}${getDayOfMonthSuffix(dayOfMonth)}"

            AccessibleText(text = label, baseFontSizeSp = 18f, forceFontWeight = FontWeight.Bold)
            Box(Modifier
                .width(100.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface))
        }
        IconButton(onClick = { onIncrementDate(1) }) { 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day") 
        }
    }
}

private fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) return "th"
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AccessibleText(text = message, fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant, baseFontSizeSp = 16f, modifier = Modifier.padding(32.dp))
    }
}
