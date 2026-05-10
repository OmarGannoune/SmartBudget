package com.omargannoune.smartbudget.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omargannoune.smartbudget.data.local.dao.BudgetDao
import com.omargannoune.smartbudget.data.local.dao.CategoryDao
import com.omargannoune.smartbudget.data.local.dao.ExpenseDao
import com.omargannoune.smartbudget.data.local.dao.RecurringRuleDao
import com.omargannoune.smartbudget.data.local.dao.SavingsGoalDao
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.CategoryMonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.data.local.entity.MonthlyBudgetEntity
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsContributionEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity

@Database(
    entities = [
        CategoryEntity::class,
        ExpenseEntity::class,
        MonthlyBudgetEntity::class,
        CategoryMonthlyBudgetEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        RecurringRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SmartBudgetDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringRuleDao(): RecurringRuleDao
}
