package com.omargannoune.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omargannoune.smartbudget.data.local.entity.SavingsContributionEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAt DESC")
    fun observeGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :goalId LIMIT 1")
    fun observeGoal(goalId: Long): Flow<SavingsGoalEntity?>

    @Insert
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Long)

    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun observeContributions(goalId: Long): Flow<List<SavingsContributionEntity>>

    @Insert
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long
}
