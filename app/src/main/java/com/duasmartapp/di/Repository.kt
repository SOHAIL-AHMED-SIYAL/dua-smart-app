package com.duasmartapp.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.duasmartapp.BuildConfig
import com.duasmartapp.R
import com.duasmartapp.model.AppSetting
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.AttendanceRecord
import com.duasmartapp.model.FeeRecord
import com.duasmartapp.model.Holiday
import com.duasmartapp.model.Student
import com.duasmartapp.model.dao.AttendanceDao
import com.duasmartapp.model.dao.FeeDao
import com.duasmartapp.model.dao.HolidayDao
import com.duasmartapp.model.dao.SettingDao
import com.duasmartapp.model.dao.StudentDao
import com.duasmartapp.model.dao.UserDao
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val BACKUP_FILE_PREFIX = "DuaSmartApp_Backup_"
const val AUTHORITY_FILE_PROVIDER = "${BuildConfig.APPLICATION_ID}.fileprovider"

class Repository(private val context: Context, private val db: RoomDatabase = AppDatabase.getDatabase(context)) {

    private val studentDao: StudentDao = db.studentDao()
    private val userDao: UserDao = db.userDao()
    private val attendanceDao: AttendanceDao = db.attendanceDao()
    private val holidayDao: HolidayDao = db.holidayDao()
    private val feeDao: FeeDao = db.feeDao()
    private val settingDao: SettingDao = db.settingDao()
    private val json = Json { ignoreUnknownKeys = true }

    private val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------
    suspend fun authenticate(username: String, password: String, role: String): AppUser? = withContext(Dispatchers.IO) {
        val user = userDao.getByUsername(username) ?: return@withContext null
        if (user.password == password && user.isEnabled && user.role.equals(role, ignoreCase = true)) user else null
    }

    // ------------------------------------------------------------------
    // School settings
    // ------------------------------------------------------------------
    suspend fun getSetting(key: String): String? = withContext(Dispatchers.IO) { settingDao.get(key) }

    suspend fun putSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingDao.put(AppSetting(key, value))
    }

    // ------------------------------------------------------------------
    // Students
    // ------------------------------------------------------------------
    fun getStudentsFlow() = studentDao.getAll()

    suspend fun getStudent(id: Int): Student? = withContext(Dispatchers.IO) { studentDao.getById(id) }

    suspend fun addStudent(student: Student): Long = withContext(Dispatchers.IO) {
        if (student.rollNo.isBlank()) throw IllegalArgumentException(context.getString(R.string.err_roll_required))
        if (student.fullName.isBlank()) throw IllegalArgumentException(context.getString(R.string.err_name_required))
        if (student.monthlyFeeRate < 0) throw IllegalArgumentException(context.getString(R.string.err_fee_required))
        if (studentDao.getByRollNo(student.rollNo) != null) throw IllegalArgumentException(context.getString(R.string.err_dup_roll))
        studentDao.insert(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        if (student.fullName.isBlank()) throw IllegalArgumentException(context.getString(R.string.err_name_required))
        studentDao.update(student)
    }

    suspend fun deleteStudent(id: Int) = withContext(Dispatchers.IO) { studentDao.deleteById(id) }

    suspend fun countActiveStudents(): Int = withContext(Dispatchers.IO) { studentDao.countActive() }

    // ------------------------------------------------------------------
    // Users (teachers)
    // ------------------------------------------------------------------
    fun getUsersFlow() = userDao.getAll()
    fun getEnabledTeachersFlow() = userDao.getEnabledTeachers()

    suspend fun addUser(user: AppUser): Long = withContext(Dispatchers.IO) {
        if (user.username.isBlank() || user.password.isBlank()) throw IllegalArgumentException(context.getString(R.string.err_fill_fields))
        if (userDao.getByUsername(user.username) != null) throw IllegalArgumentException(context.getString(R.string.err_dup_user))
        userDao.insert(user)
    }

    suspend fun updateUser(user: AppUser) = withContext(Dispatchers.IO) { userDao.update(user) }

    suspend fun deleteUser(id: Int) = withContext(Dispatchers.IO) {
        val u = userDao.getById(id) ?: return@withContext
        if (u.role.equals("ADMIN", ignoreCase = true)) throw IllegalArgumentException(context.getString(R.string.err_cannot_disable_admin))
        userDao.deleteById(id)
    }

    suspend fun disableUser(id: Int, disabled: Boolean) = withContext(Dispatchers.IO) {
        val u = userDao.getById(id) ?: return@withContext
        if (u.role.equals("ADMIN", ignoreCase = true)) throw IllegalArgumentException(context.getString(R.string.err_cannot_disable_admin))
        userDao.update(u.copy(isEnabled = disabled))
    }

    // ------------------------------------------------------------------
    // Holidays
    // ------------------------------------------------------------------
    fun getHolidaysFlow() = holidayDao.getAll()

    suspend fun isHoliday(date: LocalDate): Boolean = withContext(Dispatchers.IO) {
        val ds = date.format(df)
        if (date.dayOfWeek == DayOfWeek.SUNDAY) true
        else holidayDao.getByDate(ds).isNotEmpty()
    }

    suspend fun getHolidayTitle(date: LocalDate): String? = withContext(Dispatchers.IO) {
        val ds = date.format(df)
        val h = holidayDao.getByDate(ds)
        if (h.isNotEmpty()) h.first().title else null
    }

    suspend fun addHoliday(title: String, start: LocalDate, end: LocalDate, type: String) = withContext(Dispatchers.IO) {
        holidayDao.insert(Holiday(title = title, startDate = start.format(df), endDate = end.format(df), type = type))
    }

    suspend fun deleteHoliday(id: Int) = withContext(Dispatchers.IO) {
        val all = holidayDao.getAll().first()
        val h = all.firstOrNull { it.id == id } ?: return@withContext
        holidayDao.delete(h)
    }

    // ------------------------------------------------------------------
    // Attendance
    // ------------------------------------------------------------------
    fun getAttendanceFlowForDate(date: String) = attendanceDao.getByDate(date)

    suspend fun getAttendanceForMonth(year: Int, month: Int): Map<String, List<AttendanceRecord>> = withContext(Dispatchers.IO) {
        val y = "%04d".format(java.util.Locale.ROOT, year)
        val m = "%02d".format(java.util.Locale.ROOT, month)
        attendanceDao.getMonth(y, m).groupBy { it.date }
    }

    suspend fun markAttendance(studentId: Int, date: LocalDate, status: String, markedBy: String) = withContext(Dispatchers.IO) {
        attendanceDao.insert(
            AttendanceRecord(studentId = studentId, date = date.format(df), status = status, markedBy = markedBy)
        )
    }

    suspend fun unmarkAttendance(studentId: Int, date: LocalDate) = withContext(Dispatchers.IO) {
        val rec = attendanceDao.getByStudentAndDate(studentId, date.format(df))
        if (rec != null) attendanceDao.delete(rec)
    }

    /**
     * Consecutive 'A' streak ending on the day before [upToDate], ignoring SUNDAY/HOLIDAY and 'L' days.
     * A missing record for a school day breaks the streak.
     */
    suspend fun getConsecutiveAbsenceCount(studentId: Int, upToDate: LocalDate): Int = withContext(Dispatchers.IO) {
        var count = 0
        var d = upToDate.minusDays(1)
        val from = d.minusDays(45).format(df)
        val to = d.format(df)
        val records = attendanceDao.getBetween(from, to).associateBy { it.date }
        while (count < 2) {
            val ds = d.format(df)
            val holiday = holidayDao.getByDate(ds)
            if (d.dayOfWeek == DayOfWeek.SUNDAY || holiday.isNotEmpty()) {
                d = d.minusDays(1)
                continue
            }
            val rec = records[ds]
            if (rec == null || rec.status != "A") break
            count++
            d = d.minusDays(1)
        }
        count
    }

    /**
     * Students absent for 2+ consecutive school days (pinned to top of attendance list).
     */
    suspend fun getStudentsAbsentConsecutiveDays(referenceDate: LocalDate): List<Student> = withContext(Dispatchers.IO) {
        val students = studentDao.getAll().first().filter { it.isActive }
        students.filter { getConsecutiveAbsenceCount(it.id, referenceDate) >= 2 }
    }

    // ------------------------------------------------------------------
    // Fees
    // ------------------------------------------------------------------
    fun getFeeRecordsFlowForMonth(month: String) = feeDao.getByMonth(month)

    /**
     * Ensures a fee record exists for [studentId] for [month] (yyyy-MM), seeded with the student's
     * admin-assigned monthly fee rate.
     */
    suspend fun ensureFeeRecord(studentId: Int, month: String): FeeRecord = withContext(Dispatchers.IO) {
        val existing = feeDao.getByStudentMonth(studentId, month)
        if (existing != null) return@withContext existing
        val student = studentDao.getById(studentId)!!
        val record = FeeRecord(studentId = studentId, month = month, amountDue = student.monthlyFeeRate, amountPaid = 0.0, status = "UNPAID")
        feeDao.insert(record)
    }

    suspend fun markFeePaid(studentId: Int, month: String, amountPaid: Double, paidDate: LocalDate) = withContext(Dispatchers.IO) {
        val record = ensureFeeRecord(studentId, month)
        val newPaid = record.amountPaid + amountPaid
        val status = if (newPaid >= record.amountDue) "PAID" else "UNPAID"
        feeDao.update(record.copy(amountPaid = newPaid, status = status, paidDate = paidDate.format(df)))
    }

    suspend fun unmarkFeePaid(studentId: Int, month: String) = withContext(Dispatchers.IO) {
        val record = feeDao.getByStudentMonth(studentId, month)
        if (record != null) feeDao.update(record.copy(amountPaid = 0.0, status = "UNPAID", paidDate = null))
    }

    /**
     * Students in arrears for [yearMonth] (yyyy-MM): current month unpaid (and today >= 4th of month),
     * total balance = current due + any unpaid previous months.
     */
    suspend fun getArrearsStudents(yearMonth: String, today: LocalDate): List<Pair<Student, Double>> = withContext(Dispatchers.IO) {
        val allStudents = studentDao.getAll().first().filter { it.isActive }
        val records = feeDao.getAll().first().filter { it.month <= yearMonth }
        val byStudent = records.groupBy { it.studentId }
        allStudents.mapNotNull { student ->
            val studentRecords = byStudent[student.id] ?: emptyList()
            val unpaidTotal = studentRecords.sumOf { it.amountDue - it.amountPaid }
            val current = studentRecords.find { it.month == yearMonth }
            val currentUnpaid = current == null || current.amountPaid < current.amountDue
            val afterFourth = today.dayOfMonth >= 4
            if (currentUnpaid && afterFourth && unpaidTotal > 0) student to unpaidTotal else null
        }
    }

    // ------------------------------------------------------------------
    // Read helpers (used by Excel generation)
    // ------------------------------------------------------------------
    suspend fun getAllStudents(): List<Student> = withContext(Dispatchers.IO) { studentDao.getAll().first() }

    suspend fun getAllHolidays(): List<Holiday> = withContext(Dispatchers.IO) { holidayDao.getAll().first() }

    suspend fun getAttendanceMonthMap(year: Int, month: Int): Map<String, List<AttendanceRecord>> =
        getAttendanceForMonth(year, month)

    // ------------------------------------------------------------------
    // Offline database handoff (JSON backup / import)
    // ------------------------------------------------------------------
    suspend fun exportBackupFile(): File = withContext(Dispatchers.IO) {
        val date = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val file = File(context.filesDir, "$BACKUP_FILE_PREFIX$date.json")
        val container = BackupContainer(
            appName = "DUA SMART APP",
            exportedAt = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME),
            students = studentDao.getAll().first(),
            users = userDao.getAll().first(),
            attendance = attendanceDao.getAll(),
            holidays = holidayDao.getAll().first(),
            fees = feeDao.getAll().first(),
            settings = settingDao.getAll()
        )
        FileOutputStream(file).use { it.write(json.encodeToString(container).toByteArray()) }
        file
    }

    suspend fun importBackupFile(file: File): String = withContext(Dispatchers.IO) {
        val content = file.readBytes()
        val container = json.decodeFromString(BackupContainer.serializer(), content)
        db.withTransaction {
            studentDao.deleteAll()
            userDao.deleteAll()
            attendanceDao.deleteAll()
            holidayDao.deleteAll()
            feeDao.deleteAll()
            settingDao.deleteAll()
            container.students.forEach { studentDao.insert(it) }
            container.users.forEach { userDao.insert(it) }
            container.attendance.forEach { attendanceDao.insert(it) }
            container.holidays.forEach { holidayDao.insert(it) }
            container.fees.forEach { feeDao.insert(it) }
            container.settings.forEach { settingDao.put(it) }
        }
        context.getString(R.string.backup_imported)
    }

    fun shareFileUri(file: File): Uri = FileProvider.getUriForFile(context, AUTHORITY_FILE_PROVIDER, file)

    fun whatsAppIntent(phoneNumber: String, message: String): Intent {
        val encoded = java.net.URLEncoder.encode(message, "UTF-8")
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val url = "https://wa.me/$cleanNumber?text=$encoded"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    // ------------------------------------------------------------------
    // WhatsApp message templates
    // ------------------------------------------------------------------
    suspend fun absentMessage(student: Student): String = withContext(Dispatchers.IO) {
        context.getString(R.string.whatsapp_absent_msg, student.fullName, student.rollNo)
    }

    suspend fun feeMessage(student: Student, monthLabel: String, arrears: Double): String = withContext(Dispatchers.IO) {
        val currency = settingDao.get("currency_symbol") ?: "Rs."
        context.getString(R.string.whatsapp_fee_msg, student.fullName, monthLabel, arrears, currency)
    }
}

/**
 * Serializable snapshot of the entire database for portable offline handoff.
 */
import kotlinx.serialization.Serializable

@Serializable
data class BackupContainer(
    val appName: String,
    val exportedAt: String,
    val students: List<Student>,
    val users: List<AppUser>,
    val attendance: List<AttendanceRecord>,
    val holidays: List<Holiday>,
    val fees: List<FeeRecord>,
    val settings: List<AppSetting>
)
