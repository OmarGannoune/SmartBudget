package com.omargannoune.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextOccurrenceDate ASC")
    fun observeActiveRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextOccurrenceDate ASC")
    suspend fun getActiveRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules ORDER BY createdAt DESC")
    fun observeAllRules(): Flow<List<RecurringRuleEntity>>

    @Insert
    suspend fun insert(rule: RecurringRuleEntity): Long

    @Update
    suspend fun update(rule: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :ruleId")
    suspend fun delete(ruleId: Long)

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAll()
}
