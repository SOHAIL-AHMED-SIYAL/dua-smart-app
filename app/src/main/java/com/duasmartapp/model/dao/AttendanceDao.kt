package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord): Long

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY id")
    fun getByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun getByStudentAndDate(studentId: Int, date: String): AttendanceRecord?

    @Query("""
        SELECT * FROM attendance
        WHERE date >= :from AND date <= :to
        ORDER BY date ASC, id ASC
    """)
    suspend fun getBetween(from: String, to: String): List<AttendanceRecord>

    @Query("""
        SELECT * FROM attendance
        WHERE substr(date, 1, 4) = :year AND substr(date, 6, 2) = :month
        ORDER BY date ASC, id ASC
    """)
    suspend fun getMonth(year: String, month: String): List<AttendanceRecord>

    @Query("DELETE FROM attendance WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()

    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    suspend fun getAll(): List<AttendanceRecord>
}
