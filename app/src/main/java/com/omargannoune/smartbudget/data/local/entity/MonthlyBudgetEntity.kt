package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_budgets",
    indices = [Index(value = ["month"], unique = true)]
)
data class MonthlyBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val month: String,
    val totalLimitMinor: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
