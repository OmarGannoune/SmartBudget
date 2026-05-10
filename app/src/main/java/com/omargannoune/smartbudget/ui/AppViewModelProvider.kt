package com.omargannoune.smartbudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omargannoune.smartbudget.SmartBudgetApp
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import com.omargannoune.smartbudget.ui.goals.GoalsViewModel
import com.omargannoune.smartbudget.ui.settings.SettingsViewModel

object AppViewModelProvider {
    fun factory(app: SmartBudgetApp): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(ExpensesViewModel::class.java) -> {
                        ExpensesViewModel(
                            expenseRepository = app.container.expenseRepository,
                            categoryRepository = app.container.categoryRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(BudgetsViewModel::class.java) -> {
                        BudgetsViewModel(
                            budgetRepository = app.container.budgetRepository,
                            categoryRepository = app.container.categoryRepository,
                            expenseRepository = app.container.expenseRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                        GoalsViewModel(
                            savingsRepository = app.container.savingsRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                        SettingsViewModel(
                            categoryRepository = app.container.categoryRepository
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
