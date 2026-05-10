package com.omargannoune.smartbudget.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
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
        name,
        currency
    ) { categories, userName, currencyCode ->
        OnboardingUiState(
            categories = categories,
            name = userName,
            currency = currencyCode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    data class OnboardingUiState(
        val categories: List<CategoryEntity> = emptyList(),
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
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.createCategory(name = name, icon = null, color = null)
        }
    }

    fun setMonthlyBudget(limitMinor: Long) {
        viewModelScope.launch {
            val month = LocalDate.now().format(DateTimeFormatter.ofPattern(DateFormats.MONTH_PATTERN))
            budgetRepository.upsertMonthlyBudget(
                MonthlyBudgetEntity(
                    month = month,
                    totalLimitMinor = limitMinor,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingComplete(true)
        }
    }
}
