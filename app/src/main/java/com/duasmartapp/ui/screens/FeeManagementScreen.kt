package com.duasmartapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.lifecycle.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.keyboard.KeyboardOptions
import androidx.compose.ui.input.keyboard.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.Student
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FeeManagementScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val today = LocalDate.now()
    val yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val monthLabel = today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))

    val students by viewModel.students.collectAsStateWithLifecycle()
    val records by viewModel.feeRecordsFor(yearMonth).collectAsStateWithLifecycle()
    val arrears by viewModel.arrears.collectAsStateWithLifecycle()

    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialized) {
            viewModel.loadArrears(yearMonth, today)
            students.forEach { viewModel.ensureFeeRecord(it.id, yearMonth) }
            initialized = true
        }
    }

    val recordMap = records.associateBy { it.studentId }
    val arrearMap = arrears.associateBy { it.first.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Monthly Fees  •  $yearMonth")
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "On/after the 4th of each month, unpaid fees move to Arrears.",
            style = Caption,
            color = Slate500,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (students.isEmpty()) {
            Text("No students yet.", style = BodyMuted, color = Slate500)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(students, key = { it.id }) { student ->
                    val record = recordMap[student.id]
                    val isArrear = arrearMap.containsKey(student.id)
                    val due = record?.amountDue ?: student.monthlyFeeRate
                    val paid = record?.amountPaid ?: 0.0
                    val status = record?.status ?: "UNPAID"
                    FeeRow(
                        student = student,
                        due = due,
                        paid = paid,
                        status = status,
                        isArrear = isArrear,
                        arrears = arrearMap[student.id] ?: 0.0,
                        monthLabel = monthLabel,
                        yearMonth = yearMonth,
                        today = today,
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FeeRow(
    student: Student,
    due: Double,
    paid: Double,
    status: String,
    isArrear: Boolean,
    arrears: Double,
    monthLabel: String,
    yearMonth: String,
    today: LocalDate,
    viewModel: AppViewModel
) {
    val scope = rememberCoroutineScope()
    var showPay by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.fullName, style = Body, color = Ink)
                Text(
                    text = "${student.rollNo}  •  ${student.classGrade}  •  Rate: ${student.monthlyFeeRate}",
                    style = Caption,
                    color = Slate500
                )
            }
            when {
                status == "PAID" -> StatusPill(StatusType.Present, "Paid")
                isArrear -> StatusPill(StatusType.Absent, "Arrears")
                else -> StatusPill(StatusType.Leave, "Unpaid")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status != "PAID") {
                    Button(
                        onClick = { showPay = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isArrear) Crimson else Ink,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = if (isArrear) "Clear Arrears" else "Mark Paid", style = ButtonText.copy(color = Color.White), fontSize = 12.sp)
                    }
                    if (isArrear) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val text = viewModel.feeMessageText(student, monthLabel, arrears)
                                    viewModel.openWhatsApp(student.whatsAppNumber, text)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Crimson)
                        ) {
                            Text(text = "Send Message", style = ButtonText.copy(color = Crimson, fontWeight = FontWeight.Bold), fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { scope.launch { viewModel.unmarkFeePaid(student.id, yearMonth) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Slate500)
                    ) {
                        Icon(imageVector = Icons.Default.Cached, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (showPay) {
            PayDialog(
                student = student,
                due = due,
                alreadyPaid = paid,
                onDismiss = { showPay = false },
                onSave = { amount ->
                    scope.launch {
                        viewModel.markFeePaid(student.id, yearMonth, amount, today)
                        showPay = false
                    }
                }
            )
        }
    }
}

@Composable
private fun PayDialog(
    student: Student,
    due: Double,
    alreadyPaid: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(alreadyPaid.toString()) }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.width(minConstraints = 300.dp).padding(20.dp).shadow(24.dp, shape = RoundedCornerShape(20.dp), clip = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Record Fee Payment", style = Heading2, color = Ink)
                Text(text = "${student.fullName} (${student.rollNo})", style = Caption, color = Slate500)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Amount due: $due  •  Already paid: $alreadyPaid", style = Body, color = Ink)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Amount to pay now") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text(text = "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) onSave(amt)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                    ) { Text(text = "Save") }
                }
            }
        }
    }
}


