package com.omargannoune.smartbudget.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.data.repository.BudgetRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.SavingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val categoryRepository: CategoryRepository,
    private val savingsRepository: SavingsRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    private val name = MutableStateFlow("")
    private val currency = MutableStateFlow("MAD")

    val uiState: StateFlow<OnboardingUiState> = combine(
        categoryRepository.observeAllCategories(),
        savingsRepository.observeGoals(),
        name,
        currency
    ) { categories: List<CategoryEntity>, goals: List<SavingsGoalEntity>, userName: String, currencyCode: String ->
        OnboardingUiState(
            categories = categories,
            goals = goals,
            name = userName,
            currency = currencyCode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    data class OnboardingUiState(
        val categories: List<CategoryEntity> = emptyList(),
        val goals: List<SavingsGoalEntity> = emptyList(),
        val name: String = "",
        val currency: String = "MAD"
    )

    init {
        viewModelScope.launch {
            val profile = onboardingRepository.observeProfile().first()
            name.value = profile.name
            currency.value = profile.currency
        }
    }

    fun updateName(value: String) {
        name.value = value
    }

    fun updateCurrency(value: String) {
        currency.value = value
    }

    fun saveProfile() {
        viewModelScope.launch {
            onboardingRepository.saveProfile(name.value.trim(), currency.value.trim())
        }
    }

    fun createGoal(name: String, targetMinor: Long, targetDate: String?) {
        viewModelScope.launch {
            savingsRepository.createGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmountMinor = targetMinor,
                    currentAmountMinor = 0L,
                    targetDate = targetDate,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateGoal(id: Long, name: String, targetMinor: Long, targetDate: String?) {
        viewModelScope.launch {
            // Fetch the existing goal to preserve its state
            val existingGoal = savingsRepository.observeGoal(id).first()
            if (existingGoal != null) {
                savingsRepository.updateGoal(
                    existingGoal.copy(
                        name = name,
                        targetAmountMinor = targetMinor,
                        targetDate = targetDate,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            savingsRepository.deleteGoal(goalId)
        }
    }

    fun createCategory(name: String, icon: String?, color: String?) {
        viewModelScope.launch {
            categoryRepository.createCategory(name = name, icon = icon, color = color)
        }
    }

    fun updateCategory(id: Long, name: String, icon: String?, color: String?) {
        viewModelScope.launch {
            // Fetch all categories and find the one to update
            val categories = categoryRepository.observeAllCategories().first()
            val existingCategory = categories.find { it.id == id }
            if (existingCategory != null) {
                categoryRepository.updateCategory(
                    existingCategory.copy(
                        name = name,
                        icon = icon,
                        color = color,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            categoryRepository.deleteCategoryMoveExpenses(categoryId)
        }
    }

    fun setMonthlyBudget(limitMinor: Long) {
        viewModelScope.launch {
            val month = LocalDate.now().format(DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN))
            budgetRepository.upsertMonthlyBudget(
                MonthlyBudgetEntity(
                    month = month,
                    totalLimitMinor = limitMinor,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setCategoryBudgets(categoryBudgets: Map<Long, Long>) {
        viewModelScope.launch {
            val month = LocalDate.now().format(DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN))
            categoryBudgets.forEach { (categoryId, limitMinor) ->
                if (limitMinor > 0L) {
                    budgetRepository.upsertCategoryBudget(
                        CategoryMonthlyBudgetEntity(
                            month = month,
                            categoryId = categoryId,
                            limitMinor = limitMinor,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingComplete(true)
        }
    }
}
