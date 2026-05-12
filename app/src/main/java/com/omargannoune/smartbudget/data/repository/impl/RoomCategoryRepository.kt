package com.omargannoune.smartbudget.data.repository.impl

import com.omargannoune.smartbudget.data.local.dao.CategoryDao
import com.omargannoune.smartbudget.data.local.dao.ExpenseDao
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val timeProvider: TimeProvider
) : CategoryRepository {
    
    companion object {
        private val DEFAULT_CATEGORIES = mapOf(
            "Food" to Pair("Utensils", "#FFFFB86B"),
            "Transport" to Pair("Bus", "#5DE2C6"),
            "Rent" to Pair("Home", "#FF6B6B"),
            "Health" to Pair("HeartPulse", "#3BD671"),
            "Leisure" to Pair("Gamepad2", "#C7D1FF"),
            "Studies" to Pair("GraduationCap", "#F5C451"),
            "Other" to Pair("ShoppingBag", "#A9B1BF")
        )
    }
    override fun observeActiveCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeActiveCategories()

    override fun observeAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeAllCategories()

    override suspend fun createCategory(name: String, icon: String?, color: String?) {
        val now = timeProvider.nowMillis()
        val category = CategoryEntity(
            name = name,
            icon = icon,
            color = color,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) {
            categoryDao.update(category.copy(updatedAt = timeProvider.nowMillis()))
        }
    }

    override suspend fun archiveCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) {
            categoryDao.update(
                category.copy(
                    isActive = !category.isActive,
                    updatedAt = timeProvider.nowMillis()
                )
            )
        }
    }

    override suspend fun deleteCategoryMoveExpenses(categoryId: Long, otherCategoryName: String) {
        withContext(Dispatchers.IO) {
            val existingOther = categoryDao.findByName(otherCategoryName)
            val otherId = if (existingOther != null) {
                existingOther.id
            } else {
                val now = timeProvider.nowMillis()
                categoryDao.insert(
                    CategoryEntity(
                        name = otherCategoryName,
                        icon = null,
                        color = null,
                        isActive = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            expenseDao.moveExpensesToCategory(categoryId, otherId)
            categoryDao.deleteById(categoryId)
        }
    }

    override suspend fun ensureDefaultCategories() {
        withContext(Dispatchers.IO) {
            DEFAULT_CATEGORIES.forEach { (name, iconColorPair) ->
                val (icon, color) = iconColorPair
                val existing = categoryDao.findByName(name)
                if (existing == null) {
                    val now = timeProvider.nowMillis()
                    categoryDao.insert(
                        CategoryEntity(
                            name = name,
                            icon = icon,
                            color = color,
                            isActive = true,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                } else if (existing.icon == null || existing.color == null) {
                    // Update existing default category with icon/color if missing
                    categoryDao.update(
                        existing.copy(
                            icon = icon,
                            color = color,
                            updatedAt = timeProvider.nowMillis()
                        )
                    )
                }
            }
        }
    }
    
    fun isDefaultCategory(categoryName: String): Boolean {
        return DEFAULT_CATEGORIES.containsKey(categoryName)
    }
}
