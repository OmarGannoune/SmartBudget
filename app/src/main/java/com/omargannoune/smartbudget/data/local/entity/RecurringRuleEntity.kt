package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val amountMinor: Long,
    val currency: String,
    val categoryId: Long,
    val frequency: String,
    val startDate: String,
    val endDate: String? = null,
    val nextOccurrenceDate: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
