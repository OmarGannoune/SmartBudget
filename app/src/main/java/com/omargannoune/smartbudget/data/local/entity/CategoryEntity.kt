package com.omargannoune.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
