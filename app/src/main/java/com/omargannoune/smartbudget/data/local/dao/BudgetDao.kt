package com.omargannoune.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE month = :month LIMIT 1")
    fun observeMonthlyBudget(month: String): Flow<MonthlyBudgetEntity?>

    @Insert
    suspend fun insertMonthlyBudget(budget: MonthlyBudgetEntity): Long

    @Update
    suspend fun updateMonthlyBudget(budget: MonthlyBudgetEntity)

    @Query("SELECT * FROM category_monthly_budgets WHERE month = :month ORDER BY categoryId ASC")
    fun observeCategoryBudgets(month: String): Flow<List<CategoryMonthlyBudgetEntity>>

    @Insert
    suspend fun insertCategoryBudget(budget: CategoryMonthlyBudgetEntity): Long

    @Update
    suspend fun updateCategoryBudget(budget: CategoryMonthlyBudgetEntity)

    @Query("DELETE FROM category_monthly_budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun deleteCategoryBudget(month: String, categoryId: Long)
}
