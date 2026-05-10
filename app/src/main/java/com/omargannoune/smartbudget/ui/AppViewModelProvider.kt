package com.omargannoune.smartbudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omargannoune.smartbudget.SmartBudgetApp
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel

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
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
