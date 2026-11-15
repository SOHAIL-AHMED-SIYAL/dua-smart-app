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
import androidx.compose.ui.unit.dp
import com.duasmartapp.model.Holiday
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HolidayManagerScreen(viewModel: AppViewModel, user: com.duasmartapp.model.AppUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val holidays by viewModel.holidays.collectAsStateWithLifecycle()

    if (user.role.equals("ADMIN", ignoreCase = true) == false) {
        Text("Admin only.", style = com.duasmartapp.ui.theme.Body, color = com.duasmartapp.ui.theme.Crimson)
        return
    }

    var showingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(com.duasmartapp.ui.theme.BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Holiday Manager")
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { showingDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Ink, contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material3.Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Holiday", style = com.duasmartapp.ui.theme.ButtonText.copy(color = Color.White))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("All Sundays are holidays automatically.", style = com.duasmartapp.ui.theme.Caption, color = com.duasmartapp.ui.theme.Slate500)
        Spacer(modifier = Modifier.height(12.dp))

        if (holidays.isEmpty()) {
            Text("No custom holidays yet.", style = com.duasmartapp.ui.theme.BodyMuted, color = com.duasmartapp.ui.theme.Slate500)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(holidays, key = { it.id }) { h ->
                    HolidayRow(holiday = h, viewModel = viewModel, scope = scope)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showingDialog) {
            HolidayDialog(
                onDismiss = { showingDialog = false },
                onSave = { title, start, end, type ->
                    scope.launch { viewModel.saveHoliday(title, start, end, type); showingDialog = false }
                }
            )
        }
    }
}

@Composable
private fun HolidayRow(holiday: Holiday, viewModel: AppViewModel, scope: kotlinx.coroutines.CoroutineScope) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = holiday.title, style = com.duasmartapp.ui.theme.Body, color = com.duasmartapp.ui.theme.Ink)
                Text(
                    text = "${holiday.startDate} to ${holiday.endDate}  (${holiday.type})",
                    style = com.duasmartapp.ui.theme.Caption,
                    color = com.duasmartapp.ui.theme.Slate500
                )
            }
            androidx.compose.material3.IconButton(onClick = {
                scope.launch { viewModel.deleteHoliday(holiday.id) }
            }) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material3.Icons.Default.Delete,
                    contentDescription = null,
                    tint = com.duasmartapp.ui.theme.Crimson
                )
            }
        }
    }
}

@Composable
private fun HolidayDialog(
    onDismiss: () -> Unit,
    onSave: (String, LocalDate, LocalDate, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("HOL") }
    var isRange by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.width(minConstraints = 300.dp).padding(20.dp).shadow(24.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), clip = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Add Holiday", style = com.duasmartapp.ui.theme.Heading2, color = com.duasmartapp.ui.theme.Ink)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Holiday / Event Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Date range", style = com.duasmartapp.ui.theme.Caption, color = com.duasmartapp.ui.theme.Slate500)
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Switch(checked = isRange, onCheckedChange = { isRange = it })
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (isRange) {
                    OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                    Button(onClick = onDismiss) { Text(text = "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            try {
                                val s = LocalDate.parse(start)
                                val e = if (isRange) LocalDate.parse(end) else s
                                onSave(title, s, e, type)
                                onDismiss()
                            } catch (e: Exception) {
                                // invalid date format - silently ignore
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Ink, contentColor = Color.White)
                    ) { Text(text = "Save") }
                }
            }
        }
    }
}
