package com.omargannoune.smartbudget.data.repository

import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

interface RecurringRepository {
    fun observeActiveRules(): Flow<List<RecurringRuleEntity>>
    fun observeAllRules(): Flow<List<RecurringRuleEntity>>
    suspend fun createRule(rule: RecurringRuleEntity)
    suspend fun updateRule(rule: RecurringRuleEntity)
    suspend fun deleteRule(ruleId: Long)
}
