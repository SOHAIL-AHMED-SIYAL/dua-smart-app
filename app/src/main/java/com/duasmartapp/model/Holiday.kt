package com.duasmartapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "holidays")
@Serializable
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,   // yyyy-MM-dd (same as start for single date)
    val type: String // "SUN" | "HOL"
)
