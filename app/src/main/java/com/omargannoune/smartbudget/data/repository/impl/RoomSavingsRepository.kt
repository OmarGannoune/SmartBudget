package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.dao.SavingsGoalDao
import com.omargannoune.smartbudget.data.local.entity.SavingsContributionEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.data.repository.SavingsRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomSavingsRepository(
    private val savingsGoalDao: SavingsGoalDao,
    private val timeProvider: TimeProvider
) : SavingsRepository {
    override fun observeGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.observeGoals()

    override fun observeGoal(goalId: Long): Flow<SavingsGoalEntity?> =
        savingsGoalDao.observeGoal(goalId)

    override fun observeContributions(goalId: Long): Flow<List<SavingsContributionEntity>> =
        savingsGoalDao.observeContributions(goalId)

    override suspend fun createGoal(goal: SavingsGoalEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            savingsGoalDao.insertGoal(goal.copy(createdAt = now, updatedAt = now))
        }
    }

    override suspend fun updateGoal(goal: SavingsGoalEntity) {
        withContext(Dispatchers.IO) {
            savingsGoalDao.updateGoal(goal.copy(updatedAt = timeProvider.nowMillis()))
        }
    }

    override suspend fun deleteGoal(goalId: Long) {
        withContext(Dispatchers.IO) {
            savingsGoalDao.deleteGoal(goalId)
        }
    }

    override suspend fun deleteAllGoals() {
        withContext(Dispatchers.IO) {
            savingsGoalDao.deleteAll()
        }
    }

    override suspend fun addContribution(contribution: SavingsContributionEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            savingsGoalDao.insertContribution(contribution.copy(createdAt = now, updatedAt = now))
        }
    }
}
