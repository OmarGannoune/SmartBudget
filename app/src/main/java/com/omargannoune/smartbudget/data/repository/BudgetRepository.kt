package com.omargannoune.smartbudget.data.repository

import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeMonthlyBudget(month: String): Flow<MonthlyBudgetEntity?>
    fun observeCategoryBudgets(month: String): Flow<List<CategoryMonthlyBudgetEntity>>
    suspend fun upsertMonthlyBudget(budget: MonthlyBudgetEntity)
    suspend fun upsertCategoryBudget(budget: CategoryMonthlyBudgetEntity)
    suspend fun deleteCategoryBudget(month: String, categoryId: Long)
    suspend fun deleteAllMonthlyBudgets()
    suspend fun deleteAllCategoryBudgets()
}
