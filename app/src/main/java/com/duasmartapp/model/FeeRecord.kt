package com.duasmartapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "fee_records",
    indices = [Index(value = ["studentId", "month"], unique = true)]
)
@Serializable
data class FeeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val month: String, // yyyy-MM
    val amountDue: Double,
    val amountPaid: Double = 0.0,
    val status: String = "UNPAID", // "PAID" | "UNPAID" | "ARREARS"
    val paidDate: String? = null
)
