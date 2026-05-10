package com.omargannoune.smartbudget.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN)
    private val currentMonth = LocalDate.now().format(monthFormatter)

    val budgetsUiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeMonthlyBudget(currentMonth),
        budgetRepository.observeCategoryBudgets(currentMonth),
        categoryRepository.observeActiveCategories()
    ) { monthlyBudget, categoryBudgets, categories ->
        BudgetsUiState(
            month = currentMonth,
            monthlyBudget = monthlyBudget,
            categoryBudgets = categoryBudgets,
            categories = categories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    data class BudgetsUiState(
        val month: String = "",
        val monthlyBudget: MonthlyBudgetEntity? = null,
        val categoryBudgets: List<CategoryMonthlyBudgetEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList()
    )
}
