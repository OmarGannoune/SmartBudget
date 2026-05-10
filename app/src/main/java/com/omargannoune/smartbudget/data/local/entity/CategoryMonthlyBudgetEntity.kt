package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_monthly_budgets",
    indices = [Index(value = ["month", "categoryId"], unique = true)]
)
data class CategoryMonthlyBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val month: String,
    val categoryId: Long,
    val limitMinor: Long,
    val createdAt: Long,
    val updatedAt: Long
)
