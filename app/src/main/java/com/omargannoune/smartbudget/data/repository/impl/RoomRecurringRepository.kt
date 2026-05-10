package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.DateFormats
import com.omargannoune.smartbudget.data.local.dao.ExpenseDao
import com.omargannoune.smartbudget.data.local.dao.RecurringRuleDao
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import com.omargannoune.smartbudget.data.repository.RecurringRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RoomRecurringRepository(
    private val recurringRuleDao: RecurringRuleDao,
    private val expenseDao: ExpenseDao,
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

    override suspend fun generateDueExpenses(): Int {
        return withContext(Dispatchers.IO) {
            val formatter = DateTimeFormatter.ofPattern(DateFormats.DATE_PATTERN)
            val today = LocalDate.now()
            val rules = recurringRuleDao.getActiveRules()
            var generatedCount = 0

            rules.forEach { rule ->
                val endDate = rule.endDate?.let { LocalDate.parse(it, formatter) }
                var nextDate = LocalDate.parse(rule.nextOccurrenceDate, formatter)
                var didGenerate = false

                while (!nextDate.isAfter(today)) {
                    if (endDate != null && nextDate.isAfter(endDate)) {
                        break
                    }

                    val dateString = nextDate.format(formatter)
                    val alreadyCreated =
                        expenseDao.countRecurringExpenseForDate(rule.id, dateString) > 0

                    if (!alreadyCreated) {
                        val now = timeProvider.nowMillis()
                        expenseDao.insert(
                            ExpenseEntity(
                                amountMinor = rule.amountMinor,
                                currency = rule.currency,
                                date = dateString,
                                categoryId = rule.categoryId,
                                note = rule.name,
                                paymentMethod = null,
                                necessityRating = null,
                                isRecurringInstance = true,
                                recurringSourceId = rule.id,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                        generatedCount += 1
                    }

                    nextDate = advanceDate(nextDate, rule.frequency)
                    didGenerate = true
                }

                val isExpired = endDate != null && nextDate.isAfter(endDate)
                if (didGenerate || (isExpired && rule.isActive)) {
                    recurringRuleDao.update(
                        rule.copy(
                            nextOccurrenceDate = nextDate.format(formatter),
                            isActive = if (isExpired) false else rule.isActive,
                            updatedAt = timeProvider.nowMillis()
                        )
                    )
                }
            }
            generatedCount
        }
    }

    private fun advanceDate(date: LocalDate, frequency: String): LocalDate {
        return when (frequency.lowercase()) {
            "weekly" -> date.plusWeeks(1)
            else -> date.plusMonths(1)
        }
    }
}
