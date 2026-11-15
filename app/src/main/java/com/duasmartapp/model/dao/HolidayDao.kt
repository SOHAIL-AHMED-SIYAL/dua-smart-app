package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.Holiday
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY startDate ASC")
    fun getAll(): Flow<List<Holiday>>

    @Query("""
        SELECT * FROM holidays
        WHERE :date BETWEEN startDate AND endDate
    """)
    suspend fun getByDate(date: String): List<Holiday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holiday: Holiday): Long

    @Update
    suspend fun update(holiday: Holiday)

    @Delete
    suspend fun delete(holiday: Holiday)

    @Query("DELETE FROM holidays")
    suspend fun deleteAll()
}
