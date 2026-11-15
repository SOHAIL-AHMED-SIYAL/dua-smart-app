package com.duasmartapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.duasmartapp.model.AppUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users ORDER BY role DESC, username ASC")
    fun getAll(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE username = :username")
    suspend fun getByUsername(username: String): AppUser?

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getById(id: Int): AppUser?

    @Query("SELECT * FROM app_users WHERE role = 'TEACHER' AND isEnabled = 1 ORDER BY username ASC")
    fun getEnabledTeachers(): Flow<List<AppUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: AppUser): Long

    @Update
    suspend fun update(user: AppUser)

    @Delete
    suspend fun delete(user: AppUser)

    @Query("DELETE FROM app_users WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM app_users")
    suspend fun deleteAll()
}
