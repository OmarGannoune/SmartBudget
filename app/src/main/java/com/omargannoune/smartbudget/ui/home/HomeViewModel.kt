package com.omargannoune.smartbudget.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import com.omargannoune.smartbudget.data.repository.SavingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val savingsRepository: SavingsRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN)
    private val currentMonth = LocalDate.now().format(monthFormatter)

    private val categoriesFlow = categoryRepository.observeActiveCategories()
    private val totalSpentFlow = expenseRepository.observeTotalForMonth(currentMonth)
    private val monthlyBudgetFlow = budgetRepository.observeMonthlyBudget(currentMonth)
    private val goalsFlow = savingsRepository.observeGoals()

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

    val uiState: StateFlow<HomeUiState> = combine(
        categoriesFlow,
        totalSpentFlow,
        monthlyBudgetFlow,
        goalsFlow,
        combine(categorySpentFlow, onboardingRepository.observeProfile()) { spent, profile ->
            spent to profile
        }
    ) { categories, totalSpent, monthlyBudget, goals, spentAndProfile ->
        val (categorySpent, profile) = spentAndProfile
        val topCategories = buildTopCategories(categories, categorySpent)
        val activeGoals = goals.filter { !it.isCompleted }.take(3)
        HomeUiState(
            month = currentMonth,
            totalSpentMinor = totalSpent,
            monthlyBudget = monthlyBudget,
            remainingMinor = monthlyBudget?.totalLimitMinor?.minus(totalSpent),
            topCategories = topCategories,
            goals = activeGoals,
            greeting = greetingForTime(profile.name),
            currency = profile.currency,
            userName = profile.name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    data class HomeUiState(
        val month: String = "",
        val totalSpentMinor: Long = 0L,
        val monthlyBudget: MonthlyBudgetEntity? = null,
        val remainingMinor: Long? = null,
        val topCategories: List<CategorySpend> = emptyList(),
        val goals: List<SavingsGoalEntity> = emptyList(),
        val greeting: String = "",
        val currency: String = "MAD",
        val userName: String = ""
    )

    data class CategorySpend(
        val name: String,
        val icon: String?,
        val color: String?,
        val spentMinor: Long
    )

    private fun buildTopCategories(
        categories: List<CategoryEntity>,
        categorySpent: Map<Long, Long>
    ): List<CategorySpend> {
        return categories.mapNotNull { category ->
            val spent = categorySpent[category.id] ?: 0L
            if (spent <= 0L) {
                null
            } else {
                CategorySpend(
                    name = category.name,
                    icon = category.icon,
                    color = category.color,
                    spentMinor = spent
                )
            }
        }.sortedByDescending { it.spentMinor }
            .take(3)
    }

    private fun greetingForTime(name: String = ""): String {
        val hour = LocalTime.now().hour
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        return if (name.isNotEmpty()) "$greeting, $name" else greeting
    }
}
