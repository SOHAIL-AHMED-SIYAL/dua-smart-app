package com.duasmartapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duasmartapp.model.AppUser
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(viewModel: AppViewModel, user: AppUser, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val users by viewModel.users.collectAsStateWithLifecycle()

    if (user.role.equals("ADMIN", ignoreCase = true) == false) {
        Text("Admin only.", style = com.duasmartapp.ui.theme.Body, color = com.duasmartapp.ui.theme.Crimson)
        return
    }

    var showingDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AppUser?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(com.duasmartapp.ui.theme.BackgroundBrush)
    ) {
        AppHeader(title = "DUA SMART APP", subtitle = "User Management (Admin)")
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { editing = null; showingDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Ink, contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material3.Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Teacher", style = com.duasmartapp.ui.theme.ButtonText.copy(color = Color.White))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (users.isEmpty()) {
            Text("No users.", style = com.duasmartapp.ui.theme.BodyMuted, color = com.duasmartapp.ui.theme.Slate500)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(users, key = { it.id }) { u ->
                    UserRow(user = u, currentUser = user, viewModel = viewModel, scope = scope)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showingDialog) {
            UserDialog(
                user = editing,
                onDismiss = { showingDialog = false; editing = null },
                onSave = { u ->
                    scope.launch { viewModel.saveUser(u); showingDialog = false; editing = null }
                }
            )
        }
    }
}

@Composable
private fun UserRow(
    user: AppUser,
    currentUser: AppUser,
    viewModel: AppViewModel,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val isAdmin = user.role.equals("ADMIN", ignoreCase = true)
    var enabled by remember { mutableStateOf(user.isEnabled) }
    GlassCard(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.username, style = com.duasmartapp.ui.theme.Body, color = com.duasmartapp.ui.theme.Ink)
                Text(
                    text = "${user.role}  •  ${if (user.isEnabled) "Enabled" else "Disabled"}",
                    style = com.duasmartapp.ui.theme.Caption,
                    color = com.duasmartapp.ui.theme.Slate500
                )
            }
            Row {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        scope.launch { viewModel.toggleUser(user.id, !user.isEnabled) }
                    }
                )
                if (!isAdmin && user.id != currentUser.id) {
                    androidx.compose.material3.IconButton(onClick = {
                        scope.launch { viewModel.deleteUser(user.id) }
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
    }
}

@Composable
private fun UserDialog(
    user: AppUser?,
    onDismiss: () -> Unit,
    onSave: (AppUser) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf(if (user == null) "teacher123" else "") }
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.width(minConstraints = 300.dp).padding(20.dp).shadow(24.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), clip = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = if (user == null) "Add Teacher" else "Edit Teacher", style = com.duasmartapp.ui.theme.Heading2, color = com.duasmartapp.ui.theme.Ink)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                    Button(onClick = onDismiss) { Text(text = "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(AppUser(id = user?.id ?: 0, username = username, password = password, role = "TEACHER", isEnabled = true))
                            onDismiss()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.duasmartapp.ui.theme.Ink, contentColor = Color.White)
                    ) { Text(text = "Save") }
                }
            }
        }
    }
}
