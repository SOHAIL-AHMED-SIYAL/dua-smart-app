package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY CAST(rollNo AS INTEGER) ASC, fullName ASC")
    fun getAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Int): Student?

    @Query("SELECT * FROM students WHERE rollNo = :rollNo")
    suspend fun getByRollNo(rollNo: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    suspend fun countActive(): Int

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
