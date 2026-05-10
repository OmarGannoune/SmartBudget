package com.omargannoune.smartbudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omargannoune.smartbudget.SmartBudgetApp
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import com.omargannoune.smartbudget.ui.goals.GoalsViewModel
import com.omargannoune.smartbudget.ui.home.HomeViewModel
import com.omargannoune.smartbudget.ui.onboarding.OnboardingViewModel
import com.omargannoune.smartbudget.ui.recurring.RecurringViewModel
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
                            categoryRepository = app.container.categoryRepository,
                            expenseRepository = app.container.expenseRepository,
                            onboardingRepository = app.container.onboardingRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(RecurringViewModel::class.java) -> {
                        RecurringViewModel(
                            recurringRepository = app.container.recurringRepository,
                            categoryRepository = app.container.categoryRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                        OnboardingViewModel(
                            onboardingRepository = app.container.onboardingRepository,
                            categoryRepository = app.container.categoryRepository,
                            savingsRepository = app.container.savingsRepository,
                            budgetRepository = app.container.budgetRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                        HomeViewModel(
                            expenseRepository = app.container.expenseRepository,
                            budgetRepository = app.container.budgetRepository,
                            categoryRepository = app.container.categoryRepository,
                            savingsRepository = app.container.savingsRepository
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
