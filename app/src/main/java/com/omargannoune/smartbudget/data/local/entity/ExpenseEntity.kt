package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amountMinor: Long,
    val currency: String,
    val date: String,
    val categoryId: Long,
    val note: String? = null,
    val paymentMethod: String? = null,
    val necessityRating: Int? = null,
    val isRecurringInstance: Boolean = false,
    val recurringSourceId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
