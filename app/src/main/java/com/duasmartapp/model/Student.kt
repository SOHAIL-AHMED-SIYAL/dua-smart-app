package com.duasmartapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "students",
    indices = [Index(value = ["rollNo"], unique = true)]
)
@Serializable
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rollNo: String,
    val fullName: String,
    val guardianName: String,
    val classGrade: String,
    val whatsAppNumber: String,
    val monthlyFeeRate: Double,
    val admissionDate: String,
    val isActive: Boolean = true
)
