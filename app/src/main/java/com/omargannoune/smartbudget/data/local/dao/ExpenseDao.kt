package com.omargannoune.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query(
        "SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, id DESC"
    )
    fun observeExpensesForMonth(monthPrefix: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE date LIKE :monthPrefix || '%'"
    )
    fun observeTotalForMonth(monthPrefix: String): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE date LIKE :monthPrefix || '%' AND categoryId = :categoryId"
    )
    fun observeTotalForMonthCategory(monthPrefix: String, categoryId: Long): Flow<Long>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteById(expenseId: Long)

    @Query("UPDATE expenses SET categoryId = :otherCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun moveExpensesToCategory(oldCategoryId: Long, otherCategoryId: Long)
}
