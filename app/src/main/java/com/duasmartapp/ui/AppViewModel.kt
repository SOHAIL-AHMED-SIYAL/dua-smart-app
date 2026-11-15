package com.duasmartapp.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asStateFlow
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.viewModelScope
import com.duasmartapp.di.Repository
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.AttendanceRecord
import com.duasmartapp.model.FeeRecord
import com.duasmartapp.model.Holiday
import com.duasmartapp.model.Student
import com.duasmartapp.utils.XlsxWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    private val _screen = MutableStateFlow<Screen>(Screen.Login)
    val screen: androidx.lifecycle.StateFlow<Screen> = _screen.asStateFlow()

    private val _user = MutableStateFlow<AppUser?>(null)
    val user: androidx.lifecycle.StateFlow<AppUser?> = _user.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: androidx.lifecycle.StateFlow<String?> = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: androidx.lifecycle.StateFlow<Boolean> = _loading.asStateFlow()

    private val _exportedFile = MutableStateFlow<String?>(null)
    val exportedFilePath: androidx.lifecycle.StateFlow<String?> = _exportedFile.asStateFlow()

    val students: androidx.lifecycle.StateFlow<List<Student>> =
        repository.getStudentsFlow().asStateFlow(emptyList())

    val users: androidx.lifecycle.StateFlow<List<AppUser>> =
        repository.getUsersFlow().asStateFlow(emptyList())

    val holidays: androidx.lifecycle.StateFlow<List<Holiday>> =
        repository.getHolidaysFlow().asStateFlow(emptyList())

    private val _attendanceMonth = MutableStateFlow<Map<String, List<AttendanceRecord>>>(emptyMap())
    val attendanceMonth: androidx.lifecycle.StateFlow<Map<String, List<AttendanceRecord>>> =
        _attendanceMonth.asStateFlow()

    private val _absentConsecutive = MutableStateFlow<List<Student>>(emptyList())
    val absentConsecutive: androidx.lifecycle.StateFlow<List<Student>> =
        _absentConsecutive.asStateFlow()

    private val _arrears = MutableStateFlow<List<Pair<Student, Double>>>(emptyList())
    val arrears: androidx.lifecycle.StateFlow<List<Pair<Student, Double>>> =
        _arrears.asStateFlow()

    fun feeRecordsFor(month: String): androidx.lifecycle.StateFlow<List<FeeRecord>> =
        repository.getFeeRecordsFlowForMonth(month).asStateFlow(emptyList())

    // ------------------------------------------------------------------
    // Navigation
    // ------------------------------------------------------------------
    fun navigateTo(screen: Screen) {
        _screen.value = screen
        _message.value = null
    }

    fun login(username: String, password: String, role: String) = viewModelScope.launch {
        _loading.value = true
        try {
            val account = repository.authenticate(username, password, role)
            if (account != null) {
                _user.value = account
                _screen.value = Screen.Dashboard
            } else {
                _message.value = "Invalid credentials or disabled account."
            }
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun logout() {
        _user.value = null
        _screen.value = Screen.Login
    }

    // ------------------------------------------------------------------
    // Students
    // ------------------------------------------------------------------
    fun saveStudent(student: Student) = viewModelScope.launch {
        _loading.value = true
        try {
            if (student.id == 0) {
                repository.addStudent(student)
            } else {
                repository.updateStudent(student)
            }
            _message.value = "Student saved."
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun deleteStudent(id: Int) = viewModelScope.launch {
        try {
            repository.deleteStudent(id)
            _message.value = "Student deleted."
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    // ------------------------------------------------------------------
    // Users
    // ------------------------------------------------------------------
    fun saveUser(user: AppUser) = viewModelScope.launch {
        _loading.value = true
        try {
            if (user.id == 0) {
                repository.addUser(user)
            } else {
                repository.updateUser(user)
            }
            _message.value = "User saved."
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun deleteUser(id: Int) = viewModelScope.launch {
        try {
            repository.deleteUser(id)
            _message.value = "User removed."
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    fun toggleUser(id: Int, disabled: Boolean) = viewModelScope.launch {
        try {
            repository.disableUser(id, disabled)
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    // ------------------------------------------------------------------
    // Holidays
    // ------------------------------------------------------------------
    fun saveHoliday(title: String, start: LocalDate, end: LocalDate, type: String) = viewModelScope.launch {
        _loading.value = true
        try {
            repository.addHoliday(title, start, end, type)
            _message.value = "Holiday saved."
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun deleteHoliday(id: Int) = viewModelScope.launch {
        try {
            repository.deleteHoliday(id)
            _message.value = "Holiday removed."
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    // ------------------------------------------------------------------
    // Holidays (view wrappers)
    // ------------------------------------------------------------------
    suspend fun isHoliday(date: LocalDate): Boolean = repository.isHoliday(date)

    suspend fun getHolidayTitle(date: LocalDate): String? = repository.getHolidayTitle(date)

    // ------------------------------------------------------------------
    // Attendance
    // ------------------------------------------------------------------
    fun loadAttendanceForMonth(year: Int, month: Int) = viewModelScope.launch {
        _attendanceMonth.value = repository.getAttendanceForMonth(year, month)
    }

    fun loadAbsentConsecutiveDays(referenceDate: LocalDate) = viewModelScope.launch {
        _absentConsecutive.value = repository.getStudentsAbsentConsecutiveDays(referenceDate)
    }

    fun markAttendance(studentId: Int, date: LocalDate, status: String, markedBy: String) = viewModelScope.launch {
        try {
            repository.markAttendance(studentId, date, status, markedBy)
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    fun clearAttendance(studentId: Int, date: LocalDate) = viewModelScope.launch {
        try {
            repository.unmarkAttendance(studentId, date)
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    // ------------------------------------------------------------------
    // Fees
    // ------------------------------------------------------------------
    fun loadArrears(yearMonth: String, today: LocalDate) = viewModelScope.launch {
        _arrears.value = repository.getArrearsStudents(yearMonth, today)
    }

    fun ensureFeeRecord(studentId: Int, month: String) = viewModelScope.launch {
        repository.ensureFeeRecord(studentId, month)
    }

    fun markFeePaid(studentId: Int, month: String, amountPaid: Double, paidDate: LocalDate) = viewModelScope.launch {
        try {
            repository.markFeePaid(studentId, month, amountPaid, paidDate)
            _message.value = "Fee recorded."
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    fun unmarkFeePaid(studentId: Int, month: String) = viewModelScope.launch {
        try {
            repository.unmarkFeePaid(studentId, month)
            _message.value = "Fee marked unpaid."
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    // ------------------------------------------------------------------
    // WhatsApp
    // ------------------------------------------------------------------
    suspend fun absentMessageText(student: Student): String = repository.absentMessage(student)

    suspend fun feeMessageText(student: Student, monthLabel: String, arrears: Double): String =
        repository.feeMessage(student, monthLabel, arrears)

    fun openWhatsApp(phoneNumber: String, message: String) = viewModelScope.launch {
        val intent = repository.whatsAppIntent(phoneNumber, message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }

    // ------------------------------------------------------------------
    // Backup / restore
    // ------------------------------------------------------------------
    fun exportBackupFile() = viewModelScope.launch {
        _loading.value = true
        try {
            val file = repository.exportBackupFile()
            _exportedFile.value = file.absolutePath
            _message.value = "Backup exported: ${file.name}"
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun shareFile(path: String) = viewModelScope.launch {
        val file = File(path)
        val uri = repository.shareFileUri(file)
        val mime = when {
            path.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/json"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            setType(mime)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }

    suspend fun generateExcelFile(year: Int, month: Int): File = withContext(Dispatchers.IO) {
        val students = repository.getAllStudents()
        val holidayRows = repository.getAllHolidays()
        val holidays = mutableSetOf<LocalDate>()
        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())
        var d = monthStart
        while (d.compareTo(monthEnd) <= 0) {
            if (d.dayOfWeek == DayOfWeek.SUNDAY) holidays.add(d)
            d = d.plusDays(1)
        }
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (h in holidayRows) {
            var hd = LocalDate.parse(h.startDate, dateFormatter)
            val end = LocalDate.parse(h.endDate, dateFormatter)
            while (!hd.isAfter(end)) {
                holidays.add(hd)
                hd = hd.plusDays(1)
            }
        }
        val raw = repository.getAttendanceMonthMap(year, month)
        val attendance = raw.flatMap { (date, records) ->
            records.map { it.studentId to date to it.status }
        }.toMap()
        XlsxWriter.generateRegister(application, year, month, students, attendance, holidays)
    }



    fun importBackupFile(path: String) = viewModelScope.launch {
        _loading.value = true
        try {
            val msg = repository.importBackupFile(File(path))
            _message.value = msg
        } catch (e: Exception) {
            _message.value = e.message
        } finally {
            _loading.value = false
        }
    }
}
