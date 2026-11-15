package com.duasmartapp.ui

import com.duasmartapp.model.AppUser
import java.time.LocalDate

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    data class Attendance(val date: LocalDate = LocalDate.now()) : Screen()
    object Students : Screen()
    object Holidays : Screen()
    object Fees : Screen()
    object Users : Screen()
    object Backup : Screen()
}

val Screen.roleRequired: String?
    get() = when (this) {
        Screen.Users, Screen.Students, Screen.Holidays -> "ADMIN"
        else -> null
    }

fun Screen.title(currentUser: AppUser?): String = when (this) {
    Screen.Login -> "DUA SMART APP"
    Screen.Dashboard -> "Dashboard"
    Screen.Attendance -> "Daily Attendance"
    Screen.Students -> "Student Records"
    Screen.Holidays -> "Holiday Manager"
    Screen.Fees -> "Monthly Fees"
    Screen.Users -> "User Management"
    Screen.Backup -> "Backup / Restore"
}
