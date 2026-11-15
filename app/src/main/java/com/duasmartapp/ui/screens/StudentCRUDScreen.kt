package com.duasmartapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duasmartapp.model.Student
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*

@Composable
fun StudentCRUDScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val students by viewModel.students.collectAsStateWithLifecycle()
    var showingDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Student?>(null) }

    if (user.role.equals("ADMIN", ignoreCase = true) == false) {
        Text("Admin only.", style = Body, color = Crimson)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Student Records")
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { editing = null; showingDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Student", style = ButtonText.copy(color = Color.White))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (students.isEmpty()) {
            Text("No students yet. Add your first student.", style = BodyMuted, color = Slate500)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(students, key = { it.id }) { student ->
                    StudentRow(
                        student = student,
                        onEdit = { editing = student; showingDialog = true },
                        onDelete = { viewModel.deleteStudent(student.id) },
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showingDialog) {
            StudentDialog(
                student = editing,
                onDismiss = { showingDialog = false; editing = null },
                onSave = { s ->
                    scope.launch {
                        viewModel.saveStudent(s)
                        showingDialog = false
                        editing = null
                    }
                }
            )
        }
    }
}

@Composable
private fun StudentRow(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: AppViewModel
) {
    val scope = rememberCoroutineScope()
    GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.fullName, style = Body, color = Ink)
                Text(
                    text = "${student.rollNo}  •  ${student.classGrade}  •  ${student.monthlyFeeRate} ${currencySymbol(viewModel)} /month",
                    style = Caption,
                    color = Slate500
                )
                Text(
                    text = "Guardian: ${student.guardianName}  •  ${student.whatsAppNumber}",
                    style = Caption,
                    color = Slate500
                )
            }
            Row {
                IconButton(
                    onClick = onEdit,
                    painter = painterResource(com.duasmartapp.R.drawable.ic_edit),
                    tint = Slate500
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteStudent(student.id)
                        }
                    },
                    painter = painterResource(com.duasmartapp.R.drawable.ic_delete),
                    tint = Crimson
                )
            }
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    painter: androidx.compose.ui.graphics.painter.Painter,
    tint: Color
) {
    Box(modifier = Modifier.clickable(onClick = onClick).size(40.dp)) {
        DuaIcon(painter = painter, tint = tint, size = 22.dp, contentDescription = null)
    }
}

@Composable
private fun StudentDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    val scope = rememberCoroutineScope()
    var s by remember(student) {
        mutableStateOf(
            student ?: Student(
                rollNo = "", fullName = "", guardianName = "", classGrade = "",
                whatsAppNumber = "", monthlyFeeRate = 0.0, admissionDate = ""
            )
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .width(minConstraints = 320.dp)
                .padding(20.dp)
                .shadow(24.dp, shape = RoundedCornerShape(20.dp), clip = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = if (student == null) "Add Student" else "Edit Student", style = Heading2, color = Ink)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = s.rollNo, onValueChange = { s = s.copy(rollNo = it) },
                    label = { Text("Roll No. / ID") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.fullName, onValueChange = { s = s.copy(fullName = it) },
                    label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.guardianName, onValueChange = { s = s.copy(guardianName = it) },
                    label = { Text("Father / Guardian") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.classGrade, onValueChange = { s = s.copy(classGrade = it) },
                    label = { Text("Class / Grade") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.whatsAppNumber, onValueChange = { s = s.copy(whatsAppNumber = it) },
                    label = { Text("Parent WhatsApp (+92...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.monthlyFeeRate.toString(), onValueChange = {
                        s = s.copy(monthlyFeeRate = it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Monthly Fee Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = s.admissionDate, onValueChange = { s = s.copy(admissionDate = it) },
                    label = { Text("Admission Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Crimson)) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    onSave(s)
                                } catch (e: Exception) {
                                    // validation errors surfaced by viewModel message banner
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}

private fun currencySymbol(viewModel: AppViewModel): String {
    // synchronous best-effort; settings may not be loaded yet
    return "Rs."
}
