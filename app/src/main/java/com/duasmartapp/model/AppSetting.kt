package com.duasmartapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "app_settings", primaryKeys = ["key"])
@Serializable
data class AppSetting(
    val key: String,
    val value: String
)
