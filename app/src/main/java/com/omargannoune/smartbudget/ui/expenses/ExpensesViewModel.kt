package com.omargannoune.smartbudget.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN)
    private val dateFormatter = DateTimeFormatter.ofPattern(DateFormats.DATE_PATTERN)

    private val currentMonth = LocalDate.now().format(monthFormatter)

    val expensesUiState: StateFlow<ExpensesUiState> = combine(
        expenseRepository.observeExpensesForMonth(currentMonth),
        expenseRepository.observeTotalForMonth(currentMonth),
        categoryRepository.observeActiveCategories()
    ) { expenses, total, categories ->
        ExpensesUiState(
            month = currentMonth,
            totalMinor = total,
            expenses = expenses,
            categories = categories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesUiState())

    data class ExpensesUiState(
        val month: String = "",
        val totalMinor: Long = 0L,
        val expenses: List<ExpenseEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList()
    )
}
