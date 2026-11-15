package com.duasmartapp.ui.screens

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duasmartapp.model.AppUser
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import java.io.File
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.copyTo

@Composable
fun BackupRestoreScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val exportedPath by viewModel.exportedFilePath.collectAsStateWithLifecycle()

    var month by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))) }

    val pickDocument = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                val tempFile = File(context.cacheDir, "DuaSmartApp_Import_${System.currentTimeMillis()}.json")
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    tempFile.outputStream().use { out -> input.copyTo(out) }
                }
                viewModel.importBackupFile(tempFile.absolutePath)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "Backup / Restore")
        Spacer(modifier = Modifier.height(16.dp))

        // ---- Database backup ----
        Text("Database Backup", style = com.duasmartapp.ui.theme.Heading2, color = com.duasmartapp.ui.theme.Ink)
        Text(
            text = "Exports all students, fee rates, attendance, holidays, teachers and settings into one shareable file.",
            style = com.duasmartapp.ui.theme.BodyMuted,
            color = com.duasmartapp.ui.theme.Slate500,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Button(
                onClick = { scope.launch { viewModel.exportBackupFile() } },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Ink, contentColor = Color.White),
                enabled = exportedPath == null || true
            ) {
                Text(text = "Export / Share Database Backup")
            }
            exportedPath?.let { path ->
                Button(
                    onClick = { scope.launch { viewModel.shareFile(path) } },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Emerald, contentColor = Color.White)
                ) {
                    Text(text = "Share Backup via WhatsApp")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    pickDocument.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        setType("application/json")
                    })
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Slate500, contentColor = Color.White)
            ) {
                Text(text = "Import Database File")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "(replaces all current data)", style = com.duasmartapp.ui.theme.Caption, color = com.duasmartapp.ui.theme.Slate500)
        }
        Spacer(modifier = Modifier.height(28.dp))

        // ---- Excel report ----
        Text("Monthly Excel Report", style = com.duasmartapp.ui.theme.Heading2, color = com.duasmartapp.ui.theme.Ink)
        Text(
            text = "Register-style spreadsheet: students as rows, days 1\u201331 as columns (P / A / L / SUN / HOL) with attendance summaries.",
            style = com.duasmartapp.ui.theme.BodyMuted,
            color = com.duasmartapp.ui.theme.Slate500,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = month,
                onValueChange = { month = it },
                label = { Text("Month (yyyy-MM)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val parts = month.split("-")
                            if (parts.size == 2) {
                                val file = viewModel.generateExcelFile(parts[0].toInt(), parts[1].toInt())
                                viewModel.shareFile(file.absolutePath)
                            }
                        } catch (e: Exception) {
                            // surfaced by UI banner in AppShell via viewModel.message
                        }
                    }
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Amber, contentColor = Color.White)
            ) {
                Text(text = "Generate & Share")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}
