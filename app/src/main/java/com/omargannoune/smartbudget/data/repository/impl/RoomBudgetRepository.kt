package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.dao.BudgetDao
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomBudgetRepository(
    private val budgetDao: BudgetDao,
    private val timeProvider: TimeProvider
) : BudgetRepository {
    override fun observeMonthlyBudget(month: String): Flow<MonthlyBudgetEntity?> =
        budgetDao.observeMonthlyBudget(month)

    override fun observeCategoryBudgets(month: String): Flow<List<CategoryMonthlyBudgetEntity>> =
        budgetDao.observeCategoryBudgets(month)

    override suspend fun upsertMonthlyBudget(budget: MonthlyBudgetEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            val toSave = if (budget.id == 0L) {
                budget.copy(createdAt = now, updatedAt = now)
            } else {
                budget.copy(updatedAt = now)
            }
            if (toSave.id == 0L) {
                budgetDao.insertMonthlyBudget(toSave)
            } else {
                budgetDao.updateMonthlyBudget(toSave)
            }
        }
    }

    override suspend fun upsertCategoryBudget(budget: CategoryMonthlyBudgetEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            val toSave = if (budget.id == 0L) {
                budget.copy(createdAt = now, updatedAt = now)
            } else {
                budget.copy(updatedAt = now)
            }
            if (toSave.id == 0L) {
                budgetDao.insertCategoryBudget(toSave)
            } else {
                budgetDao.updateCategoryBudget(toSave)
            }
        }
    }

    override suspend fun deleteCategoryBudget(month: String, categoryId: Long) {
        withContext(Dispatchers.IO) {
            budgetDao.deleteCategoryBudget(month, categoryId)
        }
    }
}
