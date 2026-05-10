package com.omargannoune.smartbudget.data.repository

import com.omargannoune.smartbudget.data.local.entity.SavingsContributionEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

interface SavingsRepository {
    fun observeGoals(): Flow<List<SavingsGoalEntity>>
    fun observeGoal(goalId: Long): Flow<SavingsGoalEntity?>
    fun observeContributions(goalId: Long): Flow<List<SavingsContributionEntity>>
    suspend fun createGoal(goal: SavingsGoalEntity)
    suspend fun updateGoal(goal: SavingsGoalEntity)
    suspend fun deleteGoal(goalId: Long)
    suspend fun addContribution(contribution: SavingsContributionEntity)
}
