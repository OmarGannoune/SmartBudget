package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long = 0L,
    val targetDate: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
