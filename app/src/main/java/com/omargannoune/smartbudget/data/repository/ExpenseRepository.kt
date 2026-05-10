package com.omargannoune.smartbudget.data.repository

import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpensesForMonth(monthPrefix: String): Flow<List<ExpenseEntity>>
    fun observeTotalForMonth(monthPrefix: String): Flow<Long>
    fun observeTotalForMonthCategory(monthPrefix: String, categoryId: Long): Flow<Long>
    suspend fun getAllExpenses(): List<ExpenseEntity>
    suspend fun createExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expenseId: Long)
}
