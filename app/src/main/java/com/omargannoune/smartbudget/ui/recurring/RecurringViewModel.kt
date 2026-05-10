package com.omargannoune.smartbudget.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.RecurringRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val generationCount = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<RecurringUiState> = combine(
        recurringRepository.observeAllRules(),
        categoryRepository.observeActiveCategories(),
        generationCount
    ) { rules, categories, generatedCount ->
        RecurringUiState(
            rules = rules,
            categories = categories,
            generatedCount = generatedCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())

    data class RecurringUiState(
        val rules: List<RecurringRuleEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList(),
        val generatedCount: Int? = null
    )

    init {
        viewModelScope.launch {
            generationCount.value = recurringRepository.generateDueExpenses()
        }
    }

    fun createRule(
        name: String,
        amountMinor: Long,
        categoryId: Long,
        frequency: String,
        startDate: String,
        endDate: String?
    ) {
        viewModelScope.launch {
            recurringRepository.createRule(
                RecurringRuleEntity(
                    name = name,
                    amountMinor = amountMinor,
                    currency = "MAD",
                    categoryId = categoryId,
                    frequency = frequency,
                    startDate = startDate,
                    endDate = endDate,
                    nextOccurrenceDate = startDate,
                    isActive = true,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }

    fun toggleActive(rule: RecurringRuleEntity) {
        viewModelScope.launch {
            recurringRepository.updateRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            recurringRepository.deleteRule(ruleId)
        }
    }
}
