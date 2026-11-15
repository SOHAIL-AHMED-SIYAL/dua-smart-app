package com.duasmartapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duasmartapp.ui.components.BottomNav
import com.duasmartapp.ui.components.NavItem
import com.duasmartapp.ui.screens.*
import com.duasmartapp.ui.theme.DuaSmartAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DuaSmartAppTheme {
                val viewModel = viewModel<AppViewModel>()
                val screen by viewModel.screen.collectAsStateWithLifecycle()
                val user by viewModel.user.collectAsStateWithLifecycle()
                val message by viewModel.message.collectAsStateWithLifecycle()

                AppShell(screen = screen, user = user, message = message, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppShell(
    screen: Screen,
    user: com.duasmartapp.model.AppUser?,
    message: String?,
    viewModel: AppViewModel
) {
    // Role guard: side effect handled safely outside composition
    val roleRequired = Screen.roleRequired
    LaunchedEffect(screen, user) {
        if (roleRequired != null && (user?.role?.equals("ADMIN", ignoreCase = true) != true)) {
            viewModel.message.value = "Admin only feature."
            viewModel.navigateTo(Screen.Dashboard)
        }
    }

    val effectiveScreen = when {
        screen == Screen.Login -> Screen.Login
        roleRequired != null && (user?.role?.equals("ADMIN", ignoreCase = true) != true) -> Screen.Dashboard
        else -> screen
    }

    when (effectiveScreen) {
        Screen.Login -> LoginScreen(viewModel)
        else -> AppContent(screen = effectiveScreen, user = user!!, viewModel = viewModel)
    }

    MessageBanner(message = message, onDismiss = { viewModel.message.value = null })
}

@Composable
fun MessageBanner(message: String?, onDismiss: () -> Unit) {
    if (message.isNullOrBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, top = 4.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = androidx.compose.material3.Icons.Default.Info,
                    contentDescription = null,
                    tint = com.duasmartapp.ui.theme.Slate500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = message, style = com.duasmartapp.ui.theme.Body, color = com.duasmartapp.ui.theme.Ink)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDismiss)
            }
        }
    }
}

// Simple dismiss icon button reused locally
@Composable
private fun IconButton(onClick: () -> Unit) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Icon(
            painter = painterResource(id = com.duasmartapp.R.drawable.ic_arrow_right),
            contentDescription = "Dismiss",
            tint = com.duasmartapp.ui.theme.Slate500,
            modifier = androidx.compose.ui.Modifier.size(20.dp)
        )
    }
}

@Composable
fun AppContent(
    screen: Screen,
    user: com.duasmartapp.model.AppUser,
    viewModel: AppViewModel
) {
    Scaffold(
        topBar = {
            AppHeader(
                title = "DUA SMART APP",
                subtitle = "${Screen.title(screen)}   |   Signed in as ${user.username} (${user.role})"
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(imageVector = androidx.compose.material3.Icons.Default.Logout, contentDescription = "Logout") },
                text = { Text("Logout") },
                onClick = { viewModel.logout() },
                colors = ExtendedFloatingActionButtonDefaults.colors(
                    containerColor = com.duasmartapp.ui.theme.Crimson,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            val isAdmin = user.role.equals("ADMIN", ignoreCase = true)
            val items = buildList<NavItem> {
                add(NavItem("dashboard", "Dashboard", com.duasmartapp.R.drawable.ic_home))
                add(NavItem("attendance", "Attendance", com.duasmartapp.R.drawable.ic_attendance))
                add(NavItem("fees", "Fees", com.duasmartapp.R.drawable.ic_fees))
                if (isAdmin) add(NavItem("students", "Students", com.duasmartapp.R.drawable.ic_students))
                if (isAdmin) add(NavItem("holidays", "Holidays", com.duasmartapp.R.drawable.ic_alert_bell))
                add(NavItem("fees", "Fees", com.duasmartapp.R.drawable.ic_fees))
                if (isAdmin) add(NavItem("users", "Users", com.duasmartapp.R.drawable.ic_user_add))
                add(NavItem("backup", "Backup", com.duasmartapp.R.drawable.ic_backup))
            }.distinctBy { it.route }
            val selected = when (screen) {
                Screen.Dashboard -> "dashboard"
                Screen.Attendance -> "attendance"
                Screen.Students -> "students"
                Screen.Holidays -> "holidays"
                Screen.Fees -> "fees"
                Screen.Users -> "users"
                Screen.Backup -> "backup"
                else -> "dashboard"
            }
            BottomNav(
                items = items,
                selected = selected,
                onItemClick = { route ->
                    viewModel.navigateTo(when (route) {
                        "dashboard" -> Screen.Dashboard
                        "attendance" -> Screen.Attendance
                        "students" -> Screen.Students
                        "holidays" -> Screen.Holidays
                        "fees" -> Screen.Fees
                        "users" -> Screen.Users
                        else -> Screen.Backup
                    })
                }
            )
        },
        content = { paddingValues ->
            when (screen) {
                Screen.Dashboard -> DashboardScreen(viewModel, user, paddingValues)
                Screen.Attendance -> AttendanceScreen(viewModel, user, paddingValues)
                Screen.Students -> StudentCRUDScreen(viewModel, user, paddingValues)
                Screen.Holidays -> HolidayManagerScreen(viewModel, user, paddingValues)
                Screen.Fees -> FeeManagementScreen(viewModel, user, paddingValues)
                Screen.Users -> UserManagementScreen(viewModel, user, paddingValues)
                Screen.Backup -> BackupRestoreScreen(viewModel, user, paddingValues)
                Screen.Login -> LoginScreen(viewModel)
            }
        }
    )
}
