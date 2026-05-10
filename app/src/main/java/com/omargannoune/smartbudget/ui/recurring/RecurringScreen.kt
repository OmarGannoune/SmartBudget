package com.omargannoune.smartbudget.ui.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun RecurringScreen(
    uiState: RecurringViewModel.RecurringUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCreateRule: (
        name: String,
        amountMinor: Long,
        categoryId: Long,
        frequency: String,
        startDate: String,
        endDate: String?
    ) -> Unit,
    onToggleActive: (RecurringRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recurring bills",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        uiState.generatedCount?.let { count ->
            GenerationBanner(count = count)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(onClick = { showAddDialog = true }) {
            Text(text = "Add bill")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.rules.isEmpty()) {
            EmptyRecurringState()
        } else {
            RecurringList(
                rules = uiState.rules,
                categories = uiState.categories,
                onToggleActive = onToggleActive,
                onDeleteRule = onDeleteRule
            )
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onSave = { name, amountMinor, categoryId, frequency, startDate, endDate ->
                onCreateRule(name, amountMinor, categoryId, frequency, startDate, endDate)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GenerationBanner(count: Int) {
    val message = if (count == 1) {
        "Generated 1 recurring bill"
    } else {
        "Generated $count recurring bills"
    }
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun RecurringList(
    rules: List<RecurringRuleEntity>,
    categories: List<CategoryEntity>,
    onToggleActive: (RecurringRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(rules, key = { it.id }) { rule ->
            val categoryName = categories.firstOrNull { it.id == rule.categoryId }?.name
            RecurringRow(
                rule = rule,
                categoryName = categoryName,
                onToggleActive = onToggleActive,
                onDeleteRule = onDeleteRule
            )
        }
    }
}

@Composable
private fun RecurringRow(
    rule: RecurringRuleEntity,
    categoryName: String?,
    onToggleActive: (RecurringRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = rule.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Amount: ${formatAmount(rule.amountMinor)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Category: ${categoryName ?: "Unassigned"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Frequency: ${rule.frequency}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Next: ${rule.nextOccurrenceDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onToggleActive(rule) }) {
                    Text(text = if (rule.isActive) "Pause" else "Resume")
                }
                TextButton(onClick = { onDeleteRule(rule.id) }) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Composable
private fun EmptyRecurringState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No recurring bills",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add a recurring bill to automate entries.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddRecurringDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amountMinor: Long,
        categoryId: Long,
        frequency: String,
        startDate: String,
        endDate: String?
    ) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("monthly") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add recurring bill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Bill name") },
                    isError = nameError != null,
                    singleLine = true
                )
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    isError = amountError != null,
                    singleLine = true
                )
                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                CategoryDropdown(
                    categories = categories,
                    selectedId = selectedCategoryId,
                    onSelected = { selectedCategoryId = it }
                )
                FrequencyDropdown(
                    selected = frequency,
                    onSelected = { frequency = it }
                )
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("Start date (YYYY-MM-DD)") },
                    isError = dateError != null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = { Text("End date (optional)") },
                    singleLine = true
                )
                if (dateError != null) {
                    Text(
                        text = dateError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = null
                    amountError = null
                    dateError = null

                    if (nameText.isBlank()) {
                        nameError = "Enter a bill name"
                    }
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    }
                    if (startDateText.isBlank()) {
                        dateError = "Enter a start date"
                    }
                    if (selectedCategoryId == null) {
                        dateError = "Select a category"
                    }
                    if (nameError == null && amountError == null && dateError == null) {
                        onSave(
                            nameText.trim(),
                            amountMinor ?: 0L,
                            selectedCategoryId ?: 0L,
                            frequency,
                            startDateText.trim(),
                            endDateText.trim().ifBlank { null }
                        )
                    }
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FrequencyDropdown(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("monthly", "weekly")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frequency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatAmount(amountMinor: Long): String {
    val major = amountMinor / 100
    val minor = kotlin.math.abs(amountMinor % 100)
    return "$major.${minor.toString().padStart(2, '0')} MAD"
}

private fun parseAmountToMinor(amountText: String): Long? {
    val normalized = amountText.trim().replace(',', '.')
    if (normalized.isBlank()) return null
    return runCatching {
        val decimal = BigDecimal(normalized)
        decimal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}
