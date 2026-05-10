package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_contributions",
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["date"])
    ]
)
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val amountMinor: Long,
    val date: String,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
