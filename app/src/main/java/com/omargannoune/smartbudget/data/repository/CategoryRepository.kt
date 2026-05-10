package com.omargannoune.smartbudget.data.repository

import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<CategoryEntity>>
    fun observeAllCategories(): Flow<List<CategoryEntity>>
    suspend fun createCategory(name: String, icon: String?, color: String?)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun archiveCategory(category: CategoryEntity)
    suspend fun deleteCategoryMoveExpenses(categoryId: Long, otherCategoryName: String = "Other")
    suspend fun ensureDefaultCategories()
}
