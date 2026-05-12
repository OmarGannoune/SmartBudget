package com.omargannoune.smartbudget.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
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
            userName = profile.name,
            exportMessage = message,
            exportFilePath = filePath
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    data class SettingsUiState(
        val categories: List<CategoryEntity> = emptyList(),
        val currency: String = "MAD",
        val userName: String = "",
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

    fun updateName(name: String) {
        viewModelScope.launch {
            val profile = onboardingRepository.observeProfile().first()
            onboardingRepository.saveProfile(name, profile.currency)
        }
    }

    fun createCategory(name: String, icon: String?, color: String?) {
        viewModelScope.launch {
            categoryRepository.createCategory(name = name, icon = icon, color = color)
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String, newIcon: String?, newColor: String?) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(
                name = newName,
                icon = newIcon,
                color = newColor
            ))
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

    fun importCsv(context: Context, fileUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val csv = context.contentResolver.openInputStream(fileUri)?.bufferedReader().use { it?.readText() }
                    ?: throw Exception("Unable to read file")
                
                val categories = categoryRepository.observeAllCategories().first()
                val categoryLookup = categories.associateBy { it.name }
                
                val lines = csv.split("\n").filter { it.isNotBlank() }
                if (lines.isEmpty()) {
                    throw Exception("CSV file is empty")
                }
                
                // Skip header
                val dataLines = lines.drop(1)
                var importedCount = 0
                
                dataLines.forEach { line ->
                    try {
                        val values = parseCsvLine(line)
                        if (values.size >= 9) {
                            val date = values[0]
                            val amount = values[1].toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                            val currency = values[2]
                            val categoryName = values[3]
                            val note = values[4]
                            val paymentMethod = values[5]
                            val necessity = values[6].toIntOrNull()
                            val isRecurring = values[7] == "1"
                            val recurringSourceId = values[8].toLongOrNull()
                            
                            val categoryId = categoryLookup[categoryName]?.id
                                ?: categoryRepository.observeAllCategories().first().firstOrNull()?.id
                                ?: return@forEach
                            
                            val expense = ExpenseEntity(
                                date = date,
                                amountMinor = amount,
                                currency = currency,
                                categoryId = categoryId,
                                note = note,
                                paymentMethod = paymentMethod,
                                necessityRating = necessity,
                                isRecurringInstance = isRecurring,
                                recurringSourceId = recurringSourceId,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            
                            expenseRepository.createExpense(expense)
                            importedCount++
                        }
                    } catch (e: Exception) {
                        // Skip malformed lines
                    }
                }
                
                exportMessage.value = "Imported $importedCount expenses successfully"
            }.onFailure {
                exportMessage.value = "Import failed: ${it.message}"
            }
        }
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
                
                // Share the file with other apps
                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "SmartBudget Export - $timestamp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(shareIntent, "Share SmartBudget Export")
                context.startActivity(chooser)
                
                exportMessage.value = "CSV exported successfully"
                exportFilePath.value = file.absolutePath
            }.onFailure {
                exportMessage.value = "Export failed: ${it.message}"
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

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    // Escaped quote
                    current.append('"')
                    i += 2
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                    i++
                }
                else -> {
                    current.append(char)
                    i++
                }
            }
        }
        result.add(current.toString())
        return result
    }
}
