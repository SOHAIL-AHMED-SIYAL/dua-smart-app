package com.duasmartapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "app_users",
    indices = [Index(value = ["username"], unique = true)]
)
@Serializable
data class AppUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val role: String, // "ADMIN" | "TEACHER"
    val isEnabled: Boolean = true
)
