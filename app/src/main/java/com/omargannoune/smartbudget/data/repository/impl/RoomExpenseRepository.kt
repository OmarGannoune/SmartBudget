package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.dao.ExpenseDao
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val timeProvider: TimeProvider
) : ExpenseRepository {
    override fun observeExpensesForMonth(monthPrefix: String): Flow<List<ExpenseEntity>> =
        expenseDao.observeExpensesForMonth(monthPrefix)

    override fun observeTotalForMonth(monthPrefix: String): Flow<Long> =
        expenseDao.observeTotalForMonth(monthPrefix)

    override fun observeTotalForMonthCategory(monthPrefix: String, categoryId: Long): Flow<Long> =
        expenseDao.observeTotalForMonthCategory(monthPrefix, categoryId)

    override suspend fun getAllExpenses(): List<ExpenseEntity> =
        withContext(Dispatchers.IO) {
            expenseDao.getAllExpenses()
        }

    override suspend fun createExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            expenseDao.insert(expense.copy(createdAt = now, updatedAt = now))
        }
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            expenseDao.update(expense.copy(updatedAt = timeProvider.nowMillis()))
        }
    }

    override suspend fun deleteExpense(expenseId: Long) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteById(expenseId)
        }
    }

    override suspend fun deleteAllExpenses() {
        withContext(Dispatchers.IO) {
            expenseDao.deleteAll()
        }
    }
}
