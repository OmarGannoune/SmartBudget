package com.omargannoune.smartbudget.data

import android.content.Context
import com.omargannoune.smartbudget.data.local.DatabaseProvider
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import com.omargannoune.smartbudget.data.repository.RecurringRepository
import com.omargannoune.smartbudget.data.repository.SavingsRepository
import com.omargannoune.smartbudget.data.repository.impl.RoomBudgetRepository
import com.omargannoune.smartbudget.data.repository.impl.RoomCategoryRepository
import com.omargannoune.smartbudget.data.repository.impl.RoomExpenseRepository
import com.omargannoune.smartbudget.data.repository.impl.RoomRecurringRepository
import com.omargannoune.smartbudget.data.repository.impl.RoomSavingsRepository
import com.omargannoune.smartbudget.data.util.TimeProvider

class AppContainer(context: Context) {
    private val database = DatabaseProvider.provideDatabase(context)
    private val timeProvider = TimeProvider

    val categoryRepository: CategoryRepository = RoomCategoryRepository(
        categoryDao = database.categoryDao(),
        expenseDao = database.expenseDao(),
        timeProvider = timeProvider
    )

    val expenseRepository: ExpenseRepository = RoomExpenseRepository(
        expenseDao = database.expenseDao(),
        timeProvider = timeProvider
    )

    val budgetRepository: BudgetRepository = RoomBudgetRepository(
        budgetDao = database.budgetDao(),
        timeProvider = timeProvider
    )

    val savingsRepository: SavingsRepository = RoomSavingsRepository(
        savingsGoalDao = database.savingsGoalDao(),
        timeProvider = timeProvider
    )

    val recurringRepository: RecurringRepository = RoomRecurringRepository(
        recurringRuleDao = database.recurringRuleDao(),
        timeProvider = timeProvider
    )
}
