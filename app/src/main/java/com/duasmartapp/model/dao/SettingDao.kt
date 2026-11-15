package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.AppSetting

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend put(setting: AppSetting)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM app_settings")
    suspend fun count(): Int

    @Query("SELECT * FROM app_settings ORDER BY key ASC")
    suspend fun getAll(): List<AppSetting>
}
