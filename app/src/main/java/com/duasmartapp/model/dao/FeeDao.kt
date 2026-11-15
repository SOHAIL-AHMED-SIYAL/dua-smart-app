package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.FeeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_records WHERE studentId = :studentId AND month = :month")
    suspend fun getByStudentMonth(studentId: Int, month: String): FeeRecord?

    @Query("SELECT * FROM fee_records WHERE month = :month ORDER BY id")
    fun getByMonth(month: String): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records ORDER BY month DESC, id ASC")
    fun getAll(): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FeeRecord): Long

    @Update
    suspend fun update(record: FeeRecord)

    @Query("DELETE FROM fee_records")
    suspend fun deleteAll()
}
