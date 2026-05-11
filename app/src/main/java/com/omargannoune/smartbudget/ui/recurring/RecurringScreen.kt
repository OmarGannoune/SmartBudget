package com.omargannoune.smartbudget.ui.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.RecurringRuleEntity
import com.omargannoune.smartbudget.ui.components.AppTextButton
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
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
            ScreenTitle(text = "Recurring bills")
            IconButton(onClick = onBack) {
                Icon(Lucide.X, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        uiState.generatedCount?.let { count ->
            GenerationBanner(count = count)
            Spacer(modifier = Modifier.height(12.dp))
        }
        PrimaryButton(
            text = "Add recurring bill",
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        )
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
            categories = uiState.categories.filter { it.isActive },
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Lucide.Info, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecurringList(
    rules: List<RecurringRuleEntity>,
    categories: List<CategoryEntity>,
    onToggleActive: (RecurringRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(rules, key = { it.id }) { rule ->
            val category = categories.find { it.id == rule.categoryId }
            RecurringRow(
                rule = rule,
                category = category,
                onToggleActive = onToggleActive,
                onDeleteRule = onDeleteRule
            )
        }
    }
}

@Composable
private fun RecurringRow(
    rule: RecurringRuleEntity,
    category: CategoryEntity?,
    onToggleActive: (RecurringRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    val catColor = try {
        Color(android.graphics.Color.parseColor(category?.color ?: "#5DE2C6"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category?.icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = catColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (rule.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = category?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatAmount(rule.amountMinor),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Frequency: ${rule.frequency.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Next: ${rule.nextOccurrenceDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { onToggleActive(rule) }) {
                        Icon(
                            imageVector = if (rule.isActive) Lucide.CirclePause else Lucide.CirclePlay,
                            contentDescription = if (rule.isActive) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onDeleteRule(rule.id) }) {
                        Icon(
                            imageVector = Lucide.Trash2,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecurringState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Lucide.CalendarClock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        title = { Text(text = "New Recurring Bill", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Name") },
                    isError = nameError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(text = nameError ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    isError = amountError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amountError != null) {
                    Text(text = amountError ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text("Start (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        label = { Text("End (Optional)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = null
                    amountError = null
                    dateError = null

                    if (nameText.isBlank()) nameError = "Enter a name"
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) amountError = "Enter a valid amount"
                    if (startDateText.isBlank()) dateError = "Enter a start date"
                    
                    if (nameError == null && amountError == null && dateError == null && selectedCategoryId != null) {
                        onSave(
                            nameText.trim(),
                            amountMinor ?: 0L,
                            selectedCategoryId!!,
                            frequency,
                            startDateText.trim(),
                            endDateText.trim().ifBlank { null }
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Save", color = MaterialTheme.colorScheme.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
    val selected = categories.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            leadingIcon = {
                if (selected != null) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(selected.color ?: "#5DE2C6"))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.tertiary
                    }
                    Icon(
                        imageVector = getCategoryIcon(selected.icon),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(category.color ?: "#5DE2C6"))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.tertiary
                            }
                            Icon(
                                imageVector = getCategoryIcon(category.icon),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(category.name)
                        }
                    },
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
            value = selected.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Frequency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
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
