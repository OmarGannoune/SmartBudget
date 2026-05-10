package com.omargannoune.smartbudget.ui.settings

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import com.omargannoune.smartbudget.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsViewModel(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val exportMessage = MutableStateFlow<String?>(null)
    private val exportFilePath = MutableStateFlow<String?>(null)

    val settingsUiState: StateFlow<SettingsUiState> = combine(
        categoryRepository.observeAllCategories(),
        onboardingRepository.observeProfile(),
        exportMessage,
        exportFilePath
    ) { categories, profile, message, filePath ->
        SettingsUiState(
            categories = categories,
            currency = profile.currency,
            exportMessage = message,
            exportFilePath = filePath
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    data class SettingsUiState(
        val categories: List<CategoryEntity> = emptyList(),
        val currency: String = "MAD",
        val exportMessage: String? = null,
        val exportFilePath: String? = null
    )

    fun clearAllData() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingComplete(false)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            onboardingRepository.updateCurrency(currency)
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.createCategory(name = name, icon = null, color = null)
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(name = newName))
        }
    }

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategoryMoveExpenses(category.id)
        }
    }

    fun clearExportMessage() {
        exportMessage.value = null
        exportFilePath.value = null
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val expenses = expenseRepository.getAllExpenses()
                val categories = categoryRepository.observeAllCategories().first()
                val categoryLookup = categories.associateBy { it.id }
                val csv = buildCsv(expenses, categoryLookup)
                val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                val timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val file = File(directory, "smartbudget_export_${timestamp}.csv")
                file.writeText(csv)
                exportMessage.value = "CSV saved successfully"
                exportFilePath.value = file.absolutePath
            }.onFailure {
                exportMessage.value = "Export failed"
                exportFilePath.value = null
            }
        }
    }

    private fun buildCsv(
        expenses: List<com.omargannoune.smartbudget.data.local.entity.ExpenseEntity>,
        categoryLookup: Map<Long, CategoryEntity>
    ): String {
        val builder = StringBuilder()
        builder.append("date,amount,currency,category,note,payment_method,necessity,is_recurring,recurring_source_id\n")
        expenses.forEach { expense ->
            val categoryName = categoryLookup[expense.categoryId]?.name ?: "Unknown"
            val amount = formatAmount(expense.amountMinor)
            builder.append(
                listOf(
                    expense.date, amount, expense.currency, categoryName,
                    expense.note, expense.paymentMethod, expense.necessityRating?.toString(),
                    if (expense.isRecurringInstance) "1" else "0", expense.recurringSourceId?.toString()
                ).joinToString(separator = ",") { escapeCsv(it) }
            )
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun formatAmount(amountMinor: Long): String {
        val major = amountMinor / 100
        val minor = kotlin.math.abs(amountMinor % 100)
        return "$major.${minor.toString().padStart(2, '0')}"
    }

    private fun escapeCsv(value: String?): String {
        val raw = value ?: ""
        val escaped = raw.replace("\"", "\"\"")
        val needsQuotes = escaped.any { it == ',' || it == '\n' || it == '\r' || it == '"' }
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}
