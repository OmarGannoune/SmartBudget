package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.dao.RecurringRuleDao
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import com.omargannoune.smartbudget.data.repository.RecurringRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomRecurringRepository(
    private val recurringRuleDao: RecurringRuleDao,
    private val timeProvider: TimeProvider
) : RecurringRepository {
    override fun observeActiveRules(): Flow<List<RecurringRuleEntity>> =
        recurringRuleDao.observeActiveRules()

    override fun observeAllRules(): Flow<List<RecurringRuleEntity>> =
        recurringRuleDao.observeAllRules()

    override suspend fun createRule(rule: RecurringRuleEntity) {
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            recurringRuleDao.insert(rule.copy(createdAt = now, updatedAt = now))
        }
    }

    override suspend fun updateRule(rule: RecurringRuleEntity) {
        withContext(Dispatchers.IO) {
            recurringRuleDao.update(rule.copy(updatedAt = timeProvider.nowMillis()))
        }
    }

    override suspend fun deleteRule(ruleId: Long) {
        withContext(Dispatchers.IO) {
            recurringRuleDao.delete(ruleId)
        }
    }
}
