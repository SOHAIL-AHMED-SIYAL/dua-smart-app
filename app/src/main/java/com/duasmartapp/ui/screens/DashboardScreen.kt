package com.duasmartapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.Cells
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.lifecycle.collectAsStateWithLifecycle
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
import com.duasmartapp.ui.Screen
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val today = LocalDate.now()
    val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val displayDate = today.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))

    val students by viewModel.students.collectAsStateWithLifecycle()
    val absentConsecutive by viewModel.absentConsecutive.collectAsStateWithLifecycle()
    val arrears by viewModel.arrears.collectAsStateWithLifecycle()
    val feeRecords by viewModel.feeRecordsFor(yearMonth).collectAsStateWithLifecycle()
    val attendanceMonth by viewModel.attendanceMonth.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAttendanceForMonth(today.year, today.monthValue)
        viewModel.loadAbsentConsecutiveDays(today)
        viewModel.loadArrears(yearMonth, today)
    }

    val todayRecords = attendanceMonth[todayString] ?: emptyList()
    val presentToday = todayRecords.count { it.status == "P" }
    val absentToday = todayRecords.count { it.status == "A" }
    val feesCollected = feeRecords.sumOf { it.amountPaid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Dashboard  •  $displayDate")
        Spacer(modifier = Modifier.height(16.dp))

        // Summary tiles
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            summaryTile("Total Students", students.size.toString(), Slate500)
            summaryTile("Present Today", presentToday.toString(), Emerald)
            summaryTile("Absent Today", absentToday.toString(), Crimson)
            summaryTile("Fees Collected", "₹${feesCollected}", Amber)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Alerts
        Text("Alerts", style = Heading2, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))

        val alertItems = absentConsecutive.map { it to "absent" } + arrears.map { it.first to "arrears" }
        if (alertItems.isEmpty()) {
            Text("No alerts at the moment.", style = BodyMuted, color = Slate500)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(250.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(alertItems, key = { it.first.id + (if (it.second == "absent") "a" else "f") }) { (student, kind) ->
                    AlertRow(
                        student = student,
                        kind = kind,
                        arrears = if (kind == "arrears") arrears.find { it.first.id == student.id }!!.second else 0.0,
                        viewModel = viewModel
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Quick actions
        Text("Quick Actions", style = Heading2, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(150.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(actions(user), key = { it.route }) { action ->
                ActionChip(
                    iconId = action.iconId,
                    label = action.label,
                    onClick = { viewModel.navigateTo(action.screen) },
                    enabled = action.enabled
                )
            }
        }
    }
}

private fun summaryTile(label: String, value: String, color: Color) {
    GlassCard(
        modifier = Modifier.weight(1f).padding(4.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = Heading1, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = Caption, color = Slate500)
        }
    }
}

@Composable
private fun AlertRow(
    student: Student,
    kind: String,
    arrears: Double,
    viewModel: AppViewModel
) {
    val scope = rememberCoroutineScope()
    GlassPanel(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.fullName, style = Body, color = Ink)
                Text(
                    text = "${student.rollNo}  •  ${student.classGrade}",
                    style = Caption,
                    color = Slate500
                )
            }
            when (kind) {
                "absent" -> StatusPill(StatusType.Absent, "Absent 2 days")
                "arrears" -> StatusPill(StatusType.Absent, "Arrears ${arrears.toString()}")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val text = if (kind == "absent") {
                            viewModel.absentMessageText(student)
                        } else {
                            viewModel.feeMessageText(student, currentMonthLabel(), arrears)
                        }
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

private fun currentMonthLabel(): String =
    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

private data class ActionItem(
    val route: String,
    val screen: Screen,
    val label: String,
    val iconId: Int,
    val enabled: Boolean = true
)

private fun actions(user: AppUser): List<ActionItem> = buildList {
    add(ActionItem("attendance", Screen.Attendance, "Attendance", R.drawable.ic_attendance))
    add(ActionItem("fees", Screen.Fees, "Fees", R.drawable.ic_fees))
    add(ActionItem("students", Screen.Students, "Students", R.drawable.ic_students))
    add(ActionItem("holidays", Screen.Holidays, "Holidays", R.drawable.ic_alert_bell))
    if (user.role.equals("ADMIN", ignoreCase = true)) {
        add(ActionItem("users", Screen.Users, "Users", R.drawable.ic_user_add))
    }
    add(ActionItem("backup", Screen.Backup, "Backup", R.drawable.ic_backup))
}

@Composable
private fun ActionChip(
    iconId: Int,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    GlassCard(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .clickable(onClick = onClick, enabled = enabled),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DuaIcon(
                painter = painterResource(iconId),
                tint = if (enabled) Ink else Slate300,
                size = 32.dp,
                contentDescription = label
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = Caption, color = Ink, textAlign = androidx.compose.ui.text.TextAlign.Center)
        }
    }
}
