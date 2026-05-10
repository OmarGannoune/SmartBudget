package com.omargannoune.smartbudget.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.entity.SavingsContributionEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.data.repository.SavingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class GoalsViewModel(
    private val savingsRepository: SavingsRepository
) : ViewModel() {
    val goalsUiState: StateFlow<GoalsUiState> = savingsRepository.observeGoals()
        .map { goals -> GoalsUiState(goals = goals) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    data class GoalsUiState(
        val goals: List<SavingsGoalEntity> = emptyList()
    )

    fun createGoal(name: String, targetAmountMinor: Long, targetDate: String?) {
        viewModelScope.launch {
            savingsRepository.createGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmountMinor = targetAmountMinor,
                    currentAmountMinor = 0L,
                    targetDate = targetDate,
                    isCompleted = false,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
        }
    }

    fun addContribution(goalId: Long, amountMinor: Long, note: String?) {
        viewModelScope.launch {
            val goal = goalsUiState.value.goals.firstOrNull { it.id == goalId } ?: return@launch
            val newAmount = goal.currentAmountMinor + amountMinor
            val completed = newAmount >= goal.targetAmountMinor
            savingsRepository.addContribution(
                SavingsContributionEntity(
                    goalId = goalId,
                    amountMinor = amountMinor,
                    date = LocalDate.now().toString(),
                    note = note,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            )
            savingsRepository.updateGoal(
                goal.copy(
                    currentAmountMinor = newAmount,
                    isCompleted = completed
                )
            )
        }
    }
}
