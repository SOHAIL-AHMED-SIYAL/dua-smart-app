package com.duasmartapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.Cells
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.lifecycle.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.Student
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var monthDisplay by remember { mutableStateOf("") }

    val students by viewModel.students.collectAsStateWithLifecycle()
    val absentConsecutive by viewModel.absentConsecutive.collectAsStateWithLifecycle()
    val attendanceMonth by viewModel.attendanceMonth.collectAsStateWithLifecycle()

    var holiday by remember { mutableStateOf<HolidayInfo?>(null) }
    LaunchedEffect(selectedDate) {
        monthDisplay = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
        viewModel.loadAttendanceForMonth(selectedDate.year, selectedDate.monthValue)
        val isHol = viewModel.isHoliday(selectedDate)
        val title = viewModel.getHolidayTitle(selectedDate)
        holiday = HolidayInfo(isHoliday = isHol, title = title)
    }
    LaunchedEffect(Unit) {
        viewModel.loadAbsentConsecutiveDays(LocalDate.now())
    }

    val todayString = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val dayRecords = attendanceMonth[todayString] ?: emptyList()
    fun statusFor(student: Student): String? =
        dayRecords.find { it.studentId == student.id }?.status

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Daily Attendance")
        Spacer(modifier = Modifier.height(12.dp))

        // ---- Date picker ----
        GlassPanel(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedDate = selectedDate.minusMonths(1) }) {
                        Icon(
                            imageVector = androidx.compose.material3.Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(text = monthDisplay, style = Heading2, color = Ink, modifier = Modifier.weight(1f))
                    TextButton(onClick = { selectedDate = selectedDate.plusMonths(1) }) {
                        Icon(
                            imageVector = androidx.compose.material3.Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(40.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    gridItems(
                        daysInMonth(selectedDate),
                        key = { it }
                    ) { day ->
                        val date = LocalDate.of(selectedDate.year, selectedDate.month, day)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable(onClick = { selectedDate = date })
                                .background(
                                    if (isSelected) GlassFillLight else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    if (isSelected) 1.dp else 0.dp,
                                    Ink,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = Body,
                                color = if (isSelected) Color.White else Ink,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                holiday?.let { h ->
                    if (h.isHoliday) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material3.Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Amber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Holiday: ${h.title ?: "Sunday"} \u2013 attendance locked",
                                style = Caption,
                                color = Amber
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---- Lock notice ----
        if (holiday != null && holiday!!.isHoliday) {
            GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 0.dp) {
                Text(
                    text = "Attendance is locked for this holiday. No changes can be recorded.",
                    style = BodyMuted,
                    color = Crimson,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ---- Pinned: absent 2+ consecutive days ----
        if (absentConsecutive.isNotEmpty() && selectedDate == LocalDate.now()) {
            Text("Needs attention \u2013 absent 2 consecutive days", style = Heading2, color = Crimson)
            Spacer(modifier = Modifier.height(8.dp))
            absentConsecutive.forEach { student ->
                PinnedStudentRow(student = student, viewModel = viewModel, user = user)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ---- Class roll ----
        Text("Class Roll", style = Heading2, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(students, key = { it.id }) { student ->
                StudentAttendanceRow(
                    student = student,
                    selectedDate = selectedDate,
                    status = statusFor(student),
                    isLocked = holiday != null && holiday!!.isHoliday,
                    onStatusChange = { status ->
                        scope.launch {
                            if (status == statusFor(student)) {
                                viewModel.clearAttendance(student.id, selectedDate)
                            } else {
                                viewModel.markAttendance(student.id, selectedDate, status, user.username)
                            }
                        }
                    },
                    viewModel = viewModel,
                    user = user
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private data class HolidayInfo(val isHoliday: Boolean, val title: String?)

private fun daysInMonth(month: LocalDate): List<Int> =
    (1..month.lengthOfMonth()).toList()

@Composable
private fun PinnedStudentRow(student: Student, viewModel: AppViewModel, user: AppUser) {
    val scope = rememberCoroutineScope()
    GlassPanel(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.fullName, style = Body, color = Ink)
                Text(
                    text = "${student.rollNo} \u2022 ${student.classGrade} \u2022 ${student.whatsAppNumber}",
                    style = Caption,
                    color = Slate500
                )
            }
            StatusPill(StatusType.Absent, "Absent 2 days \u2022 Pinned")
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val text = viewModel.absentMessageText(student)
                        viewModel.openWhatsApp(student.whatsAppNumber, text)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Crimson, contentColor = White)
            ) {
                Text(text = "Send Message", style = ButtonText.copy(color = White), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StudentAttendanceRow(
    student: Student,
    selectedDate: LocalDate,
    status: String?,
    isLocked: Boolean,
    onStatusChange: (String) -> Unit,
    viewModel: AppViewModel,
    user: AppUser
) {
    val scope = rememberCoroutineScope()
    GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.fullName, style = Body, color = Ink)
                Text(
                    text = "${student.rollNo}  •  ${student.classGrade}",
                    style = Caption,
                    color = Slate500
                )
            }
            when (status) {
                "P" -> StatusPill(StatusType.Present, "P")
                "A" -> StatusPill(StatusType.Absent, "A")
                "L" -> StatusPill(StatusType.Leave, "L")
                null -> StatusPill(StatusType.Leave, "\u2014")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                statusButton("P", status == "P", Emerald, enabled = !isLocked) {
                    onStatusChange("P")
                }
                statusButton("A", status == "A", Crimson, enabled = !isLocked) {
                    onStatusChange("A")
                }
                statusButton("L", status == "L", Amber, enabled = !isLocked) {
                    onStatusChange("L")
                }
            }
            if (status == null && !isLocked) {
                Text("tap P / A / L", style = Caption.copy(fontSize = 10.sp), color = Slate500)
            }
        }
    }
}

@Composable
private fun statusButton(
    text: String,
    selected: Boolean,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) Color(0xFF0F172A) else Color(0xFF94A3B8)
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = Modifier.width(34.dp).height(34.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text, style = ButtonText.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}
