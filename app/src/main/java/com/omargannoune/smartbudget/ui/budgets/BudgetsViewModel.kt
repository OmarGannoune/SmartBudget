package com.omargannoune.smartbudget.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN)
    private val currentMonth = LocalDate.now().format(monthFormatter)

    private val categoriesFlow = categoryRepository.observeActiveCategories()
    private val monthlyBudgetFlow = budgetRepository.observeMonthlyBudget(currentMonth)
    private val categoryBudgetsFlow = budgetRepository.observeCategoryBudgets(currentMonth)
    private val totalSpentFlow = expenseRepository.observeTotalForMonth(currentMonth)
    private val categorySpentFlow = categoriesFlow.flatMapLatest { categories ->
        if (categories.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(
                categories.map { category ->
                    expenseRepository.observeTotalForMonthCategory(currentMonth, category.id)
                }
            ) { totals ->
                categories.mapIndexed { index, category ->
                    category.id to totals[index]
                }.toMap()
            }
        }
    }

    val budgetsUiState: StateFlow<BudgetsUiState> = combine(
        monthlyBudgetFlow,
        categoryBudgetsFlow,
        categoriesFlow,
        totalSpentFlow,
        combine(categorySpentFlow, onboardingRepository.observeProfile()) { spent, profile ->
            spent to profile
        }
    ) { monthlyBudget, categoryBudgets, categories, totalSpent, spentAndProfile ->
        val (categorySpent, profile) = spentAndProfile
        val budgetMap = categoryBudgets.associateBy { it.categoryId }
        val statuses = categories.map { category ->
            val limit = budgetMap[category.id]?.limitMinor
            val spent = categorySpent[category.id] ?: 0L
            val remaining = limit?.minus(spent)
            CategoryBudgetStatus(
                category = category,
                limitMinor = limit,
                spentMinor = spent,
                remainingMinor = remaining,
                isOverspent = limit != null && spent > (limit ?: 0L)
            )
        }
        BudgetsUiState(
            month = currentMonth,
            monthlyBudget = monthlyBudget,
            totalSpentMinor = totalSpent,
            totalRemainingMinor = monthlyBudget?.totalLimitMinor?.minus(totalSpent),
            categoryBudgets = categoryBudgets,
            categories = categories,
            categoryStatuses = statuses,
            currency = profile.currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    data class BudgetsUiState(
        val month: String = "",
        val monthlyBudget: MonthlyBudgetEntity? = null,
        val totalSpentMinor: Long = 0L,
        val totalRemainingMinor: Long? = null,
        val categoryBudgets: List<CategoryMonthlyBudgetEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList(),
        val categoryStatuses: List<CategoryBudgetStatus> = emptyList(),
        val currency: String = "MAD"
    )

    data class CategoryBudgetStatus(
        val category: CategoryEntity,
        val limitMinor: Long?,
        val spentMinor: Long,
        val remainingMinor: Long?,
        val isOverspent: Boolean
    )

    fun setMonthlyBudget(limitMinor: Long, existingId: Long?) {
        viewModelScope.launch {
            budgetRepository.upsertMonthlyBudget(
                MonthlyBudgetEntity(
                    id = existingId ?: 0L,
                    month = currentMonth,
                    totalLimitMinor = limitMinor,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }

    fun setCategoryBudget(categoryId: Long, limitMinor: Long, existingId: Long?) {
        viewModelScope.launch {
            budgetRepository.upsertCategoryBudget(
                CategoryMonthlyBudgetEntity(
                    id = existingId ?: 0L,
                    month = currentMonth,
                    categoryId = categoryId,
                    limitMinor = limitMinor,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }
}
