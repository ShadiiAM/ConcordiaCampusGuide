package com.example.campusguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.R
import com.example.campusguide.ServiceLocator
import com.example.campusguide.data.Course
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.theme.success
import com.example.campusguide.ui.viewmodels.CalendarError
import com.example.campusguide.ui.viewmodels.CalendarViewModel
import java.util.Calendar
import java.util.Locale

enum class CalendarTab(val labelResId: Int) {
    DAILY_SCHEDULE(R.string.calendar_daily_schedule),
    COURSE_LIST(R.string.calendar_course_list),
    ADD_COURSE(R.string.calendar_add_course)
}

@Composable
fun CalendarScreen() {
    val viewModel: CalendarViewModel = viewModel {
        CalendarViewModel(ServiceLocator.calendarRepository)
    }

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    onIncrementDate = { viewModel.incrementDate(it) }
                )
                CalendarTab.COURSE_LIST -> CourseListView(
                    courses = uiState.trackedCourses,
                    onRemoveCourse = { viewModel.removeCourse(it) }
                )
                CalendarTab.ADD_COURSE -> AddCourseView(
                    successCourses = uiState.lastAddedCourses,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onAddCourse = { sub, cat, term, sec ->
                        viewModel.addCourse(sub, cat, term, sec)
                    }
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
    onIncrementDate: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DateSelector(date, onIncrementDate)

        if (coursesForDay.isEmpty()) {
            EmptyStateMessage(stringResource(R.string.calendar_empty_schedule))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(coursesForDay) { course -> CourseCard(course = course) }
            }
        }
    }
}

@Composable
private fun CourseListView(courses: List<Course>, onRemoveCourse: (Course) -> Unit) {
    if (courses.isEmpty()) {
        EmptyStateMessage(stringResource(R.string.calendar_empty_course_list))
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(courses) { course ->
                CourseCard(course = course, showRemoveAction = true, onActionClick = { onRemoveCourse(course) })
            }
        }
    }
}

@Composable
private fun AddCourseView(
    successCourses: List<Course>? = null,
    isLoading: Boolean = false,
    error: CalendarError? = null,
    onAddCourse: (String, String, String, String) -> Unit
) {
    var termCode by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var catalog by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledTextField(
            label = stringResource(R.string.calendar_input_term_label),
            placeholder = stringResource(R.string.calendar_input_term_placeholder),
            value = termCode,
            onValueChange = { termCode = it }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_subject_label),
            placeholder = stringResource(R.string.calendar_input_subject_placeholder),
            value = subject,
            onValueChange = { subject = it }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_catalog_label),
            placeholder = stringResource(R.string.calendar_input_catalog_placeholder),
            value = catalog,
            onValueChange = { catalog = it }
        )
        LabeledTextField(
            label = stringResource(R.string.calendar_input_section_label),
            placeholder = stringResource(R.string.calendar_input_section_placeholder),
            value = section,
            onValueChange = { section = it }
        )
        
        Spacer(modifier = Modifier.weight(1.0f))

        successCourses?.let { SuccessFeedback(courses = it) }
        
        error?.let { errorType ->
            val errorMsg = when(errorType) {
                is CalendarError.NotFound -> stringResource(R.string.calendar_error_not_found)
                is CalendarError.Network -> stringResource(R.string.calendar_error_network)
                is CalendarError.Unknown -> errorType.message
            }
            AccessibleText(text = errorMsg, fallbackColor = MaterialTheme.colorScheme.error, baseFontSizeSp = 14f)
        }

        Button(
            onClick = { onAddCourse(subject, catalog, termCode, section) },
            enabled = !isLoading,
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
fun CourseCard(course: Course, isUpcoming: Boolean = false, isPast: Boolean = false, showRemoveAction: Boolean = false, onActionClick: () -> Unit = {}) {
    val alpha = if (isPast) 0.5f else 1f
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isUpcoming) 3.dp else 1.dp,
                color = colorScheme.outlineVariant.copy(alpha = alpha),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = alpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val header = stringResource(R.string.calendar_course_header, course.subject, course.catalog, course.section)
            AccessibleText(text = header, baseFontSizeSp = 18f, forceFontWeight = FontWeight.Bold)
            AccessibleText(text = course.courseTitle, baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)
            
            val fullLoc = "${course.locationCode}, ${course.buildingCode} ${course.room}"
            AccessibleText(text = stringResource(R.string.calendar_location_label, fullLoc), baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)
            
            AccessibleText(text = "${course.startTime} - ${course.endTime}", baseFontSizeSp = 14f, fallbackColor = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onActionClick, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer, contentColor = colorScheme.onSecondaryContainer), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                val buttonText = if (showRemoveAction) stringResource(R.string.calendar_remove_course) else stringResource(R.string.calendar_directions)
                AccessibleText(text = buttonText, baseFontSizeSp = 12f)
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
            CourseCard(course = course)
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
            val label = "${date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${date.get(Calendar.DAY_OF_MONTH)}th"
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

@Composable
private fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AccessibleText(text = message, fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant, baseFontSizeSp = 16f, modifier = Modifier.padding(32.dp))
    }
}
