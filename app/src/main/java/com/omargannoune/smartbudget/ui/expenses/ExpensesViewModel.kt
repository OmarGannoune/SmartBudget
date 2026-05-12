package com.omargannoune.smartbudget.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ExpensesViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN)

    private val currentMonth = MutableStateFlow(LocalDate.now().format(monthFormatter))

    val expensesUiState: StateFlow<ExpensesUiState> = currentMonth
        .flatMapLatest { month ->
            combine(
                expenseRepository.observeExpensesForMonth(month),
                expenseRepository.observeTotalForMonth(month),
                categoryRepository.observeAllCategories(),
                onboardingRepository.observeProfile()
            ) { expenses, total, categories, profile ->
                ExpensesUiState(
                    month = month,
                    totalMinor = total,
                    expenses = expenses,
                    allCategories = categories,
                    activeCategories = categories.filter { it.isActive },
                    currency = profile.currency
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesUiState())

    data class ExpensesUiState(
        val month: String = "",
        val totalMinor: Long = 0L,
        val expenses: List<ExpenseEntity> = emptyList(),
        val allCategories: List<CategoryEntity> = emptyList(),
        val activeCategories: List<CategoryEntity> = emptyList(),
        val currency: String = "MAD"
    )

    fun createExpense(
        amountMinor: Long,
        date: String,
        categoryId: Long,
        note: String?,
        paymentMethod: String?,
        necessityRating: Int?
    ) {
        viewModelScope.launch {
            expenseRepository.createExpense(
                ExpenseEntity(
                    amountMinor = amountMinor,
                    currency = expensesUiState.value.currency,
                    date = date,
                    categoryId = categoryId,
                    note = note,
                    paymentMethod = paymentMethod,
                    necessityRating = necessityRating,
                    isRecurringInstance = false,
                    recurringSourceId = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun goToPreviousMonth() {
        currentMonth.update { month ->
            YearMonth.parse(month).minusMonths(1).format(monthFormatter)
        }
    }

    fun goToNextMonth() {
        currentMonth.update { month ->
            YearMonth.parse(month).plusMonths(1).format(monthFormatter)
        }
    }
}
