package com.example.campusguide.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.map.models.BuildingInfo
import java.util.Calendar

/**
 * Clean text by removing surrounding quotes, escaped quotes, and normalizing whitespace.
 * Handles CSV-style escaped quotes where "" becomes a single quote.
 */
private fun String.cleanText(): String {
    var cleaned = this.trim()

    // Handle CSV-escaped quotes: "" becomes a single "
    // (Some entries were marked with extra quotes as they had commas in their name)
    // This is done before removing surrounding quotes
    cleaned = cleaned.replace("\"\"", "TEMP_QUOTE_MARKER")

    // Remove surrounding quotes (both single and double) iteratively
    while ((cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length > 1) ||
           (cleaned.startsWith("'") && cleaned.endsWith("'") && cleaned.length > 1)) {
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }
    }

    // Now remove the temporary markers (the escaped quotes within the text)
    cleaned = cleaned.replace("TEMP_QUOTE_MARKER", "")

    // Also remove any remaining standalone quotes
    cleaned = cleaned.replace("\"", "").replace("'", "")

    // Split by lines, trim each line, remove empty lines
    return cleaned
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailsBottomSheet(
    buildingInfo: BuildingInfo,
    onDismiss: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics {
            contentDescription = "Building details for ${buildingInfo.buildingName ?: buildingInfo.buildingCode}"
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Compact Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Building name
                    AccessibleText(
                        text = (buildingInfo.buildingName ?: buildingInfo.buildingCode).cleanText(),
                        baseFontSizeSp = 24f,
                        forceFontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            contentDescription = "Building name: ${buildingInfo.buildingName ?: buildingInfo.buildingCode}"
                        }
                    )

                    // Building code as subtitle
                    if (buildingInfo.buildingName != null) {
                        AccessibleText(
                            text = buildingInfo.buildingCode,
                            baseFontSizeSp = 18f,
                            fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .semantics {
                                    contentDescription = "Building code: ${buildingInfo.buildingCode}"
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compact info preview (address, accessibility, and hours)
                    buildingInfo.address?.let { address ->
                        CompactInfoRow(
                            label = "Address",
                            value = address.cleanText()
                        )
                    }

                    buildingInfo.accessibility?.let { accessibility ->
                        CompactInfoRow(
                            label = "Accessibility",
                            value = accessibility.cleanText()
                        )
                    }

                    buildingInfo.hours?.let { hours ->
                        CompactHoursDisplay(hours = hours.cleanText())
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics {
                        contentDescription = "Close building details"
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expand/Collapse Button
            Button(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (isExpanded) "Collapse details" else "View more details"
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                AccessibleText(
                    text = if (isExpanded) "Show Less" else "View More Details",
                    baseFontSizeSp = 15f,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            // Expanded Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Departments
                    buildingInfo.departments?.let { departments ->
                        InfoSection(
                            title = "Departments",
                            content = departments.cleanText(),
                            contentDescription = "Departments: $departments"
                        )
                    }

                    // Services
                    buildingInfo.services?.let { services ->
                        InfoSection(
                            title = "Services",
                            content = services.cleanText(),
                            contentDescription = "Services: $services"
                        )
                    }

                    // Venues
                    buildingInfo.venues?.let { venues ->
                        InfoSection(
                            title = "Venues",
                            content = venues.cleanText(),
                            contentDescription = "Venues: $venues"
                        )
                    }

                    // Notes
                    buildingInfo.notes?.let { notes ->
                        InfoSection(
                            title = "Notes",
                            content = notes.cleanText(),
                            contentDescription = "Additional notes: $notes"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        AccessibleText(
            text = "$label:",
            baseFontSizeSp = 14f,
            forceFontWeight = FontWeight.SemiBold,
            fallbackColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        AccessibleText(
            text = value,
            baseFontSizeSp = 14f,
            fallbackColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompactHoursDisplay(hours: String) {
    val currentTime = remember { getCurrentTime() }
    val openStatus = remember(hours) { checkIfOpen(hours, currentTime) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AccessibleText(
                text = "Hours:",
                baseFontSizeSp = 14f,
                forceFontWeight = FontWeight.SemiBold,
                fallbackColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                hours.lines().take(2).forEach { line ->
                    val trimmedLine = line.trim()
                    if (trimmedLine.isNotEmpty() && !trimmedLine.contains("Check", ignoreCase = true) &&
                        !trimmedLine.contains("After-hours", ignoreCase = true)) {

                        val isCurrentDay = isLineForCurrentDay(trimmedLine, currentTime.dayOfWeek)
                        val backgroundColor = when {
                            !isCurrentDay -> Color.Transparent
                            openStatus == OpenStatus.OPEN -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            openStatus == OpenStatus.CLOSED -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                        val textColor = when {
                            !isCurrentDay -> MaterialTheme.colorScheme.onSurface
                            openStatus == OpenStatus.OPEN -> Color(0xFF2E7D32)
                            openStatus == OpenStatus.CLOSED -> Color(0xFFC62828)
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = backgroundColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            AccessibleText(
                                text = trimmedLine,
                                baseFontSizeSp = 13f,
                                forceFontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                                fallbackColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HoursLine(line: String, currentTime: CurrentTime) {
    val uriHandler = LocalUriHandler.current

    // Check if this line contains days or is additional info
    val isHoursLine = line.contains(":", ignoreCase = false) &&
                     (line.contains("Monday", ignoreCase = true) ||
                      line.contains("Saturday", ignoreCase = true) ||
                      line.contains("Sunday", ignoreCase = true) ||
                      line.contains("Daily", ignoreCase = true))

    if (isHoursLine) {
        val isCurrentDay = isLineForCurrentDay(line, currentTime.dayOfWeek)
        val openStatus = if (isCurrentDay) checkIfOpen(line, currentTime) else OpenStatus.NOT_TODAY

        val backgroundColor = when {
            !isCurrentDay -> Color.Transparent
            openStatus == OpenStatus.OPEN -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            openStatus == OpenStatus.CLOSED -> Color(0xFFF44336).copy(alpha = 0.2f)
            else -> Color.Transparent
        }
        val textColor = when {
            !isCurrentDay -> MaterialTheme.colorScheme.onSurface
            openStatus == OpenStatus.OPEN -> Color(0xFF2E7D32)
            openStatus == OpenStatus.CLOSED -> Color(0xFFC62828)
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            AccessibleText(
                text = line,
                baseFontSizeSp = 14f,
                forceFontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                fallbackColor = textColor
            )
        }
    } else {
        // Additional info lines (like "Check website for details") with clickable links
        val annotatedString = buildAnnotatedString {
            val urlPattern = Regex("(https?://[^\\s]+|[a-z]+\\.[a-z]+(?:\\.[a-z]+)?(?:/[^\\s]*)?)", RegexOption.IGNORE_CASE)
            var lastIndex = 0

            urlPattern.findAll(line).forEach { matchResult ->
                // Add text before the URL
                append(line.substring(lastIndex, matchResult.range.first))

                // Add the URL as clickable
                pushStringAnnotation(tag = "URL", annotation = matchResult.value)
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(matchResult.value)
                }
                pop()

                lastIndex = matchResult.range.last + 1
            }

            // Add remaining text
            if (lastIndex < line.length) {
                append(line.substring(lastIndex))
            }
        }

        ClickableText(
            text = annotatedString,
            style = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            ),
            modifier = Modifier.padding(vertical = 2.dp),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val url = annotation.item
                        val fullUrl = if (!url.startsWith("http")) "https://$url" else url
                        try {
                            uriHandler.openUri(fullUrl)
                        } catch (e: Exception) {
                            // Handle error silently
                        }
                    }
            }
        )
    }
}

data class CurrentTime(
    val dayOfWeek: DayOfWeek,
    val hour: Int,
    val minute: Int
)

enum class OpenStatus {
    OPEN, CLOSED, NOT_TODAY, ALWAYS_OPEN
}

private fun getCurrentTime(): CurrentTime {
    val calendar = Calendar.getInstance()
    val day = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> DayOfWeek.SUNDAY
        Calendar.MONDAY -> DayOfWeek.MONDAY
        Calendar.TUESDAY -> DayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> DayOfWeek.THURSDAY
        Calendar.FRIDAY -> DayOfWeek.FRIDAY
        Calendar.SATURDAY -> DayOfWeek.SATURDAY
        else -> DayOfWeek.MONDAY
    }
    return CurrentTime(
        dayOfWeek = day,
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE)
    )
}

private fun isLineForCurrentDay(line: String, currentDay: DayOfWeek): Boolean {
    return when {
        line.contains("Monday-Friday", ignoreCase = true) ->
            currentDay in listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        line.contains("Saturday-Sunday", ignoreCase = true) || line.contains("Saturday and Sunday", ignoreCase = true) ->
            currentDay in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        line.contains("Daily", ignoreCase = true) || line.contains("24/7", ignoreCase = true) ->
            true
        else -> false
    }
}

private fun checkIfOpen(hours: String, currentTime: CurrentTime): OpenStatus {
    // Check for 24/7 access
    if (hours.contains("24/7", ignoreCase = true) || hours.contains("24 hours", ignoreCase = true)) {
        return OpenStatus.ALWAYS_OPEN
    }

    // Find the line for current day
    val relevantLine = hours.lines().firstOrNull { line ->
        isLineForCurrentDay(line, currentTime.dayOfWeek)
    } ?: return OpenStatus.NOT_TODAY

    // Check if it says "Closed"
    if (relevantLine.contains("Closed", ignoreCase = true)) {
        return OpenStatus.CLOSED
    }

    // Parse time range (e.g., "7:00 AM - 11:00 PM")
    val timePattern = Regex("(\\d{1,2}):(\\d{2})\\s*(AM|PM)?\\s*-\\s*(\\d{1,2}):(\\d{2})\\s*(AM|PM)?", RegexOption.IGNORE_CASE)
    val match = timePattern.find(relevantLine) ?: return OpenStatus.NOT_TODAY

    try {
        val (openHourStr, openMinuteStr, openAmPm, closeHourStr, closeMinuteStr, closeAmPm) = match.destructured

        var openHour = openHourStr.toInt()
        val openMinute = openMinuteStr.toInt()
        var closeHour = closeHourStr.toInt()
        val closeMinute = closeMinuteStr.toInt()

        // Convert to 24-hour format
        if (openAmPm.equals("PM", ignoreCase = true) && openHour != 12) openHour += 12
        if (openAmPm.equals("AM", ignoreCase = true) && openHour == 12) openHour = 0
        if (closeAmPm.equals("PM", ignoreCase = true) && closeHour != 12) closeHour += 12
        if (closeAmPm.equals("AM", ignoreCase = true) && closeHour == 12) closeHour = 0

        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val openMinutes = openHour * 60 + openMinute
        val closeMinutes = closeHour * 60 + closeMinute

        return if (currentMinutes in openMinutes until closeMinutes) {
            OpenStatus.OPEN
        } else {
            OpenStatus.CLOSED
        }
    } catch (e: Exception) {
        return OpenStatus.NOT_TODAY
    }
}

enum class DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
}

@Composable
fun InfoSection(
    title: String,
    content: String,
    contentDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        AccessibleText(
            text = title,
            baseFontSizeSp = 16f,
            forceFontWeight = FontWeight.SemiBold,
            fallbackColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        AccessibleText(
            text = content,
            baseFontSizeSp = 14f,
            modifier = Modifier.semantics {
                this.contentDescription = contentDescription
            }
        )
    }
}
