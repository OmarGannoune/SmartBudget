package com.omargannoune.smartbudget.ui.budgets

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
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.SectionTitle
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetsScreen(
    uiState: BudgetsViewModel.BudgetsUiState,
    modifier: Modifier = Modifier,
    onSaveMonthlyBudget: (limitMinor: Long, existingId: Long?) -> Unit,
    onSaveCategoryBudget: (categoryId: Long, limitMinor: Long, existingId: Long?) -> Unit
) {
    var showMonthlyDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    val categoryBudgetMap = remember(uiState.categoryBudgets) {
        uiState.categoryBudgets.associateBy { it.categoryId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenTitle(text = "Budgets")
        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(text = formatMonthLabel(uiState.month))
        Spacer(modifier = Modifier.height(16.dp))
        MonthlyBudgetCard(
            limitMinor = uiState.monthlyBudget?.totalLimitMinor,
            spentMinor = uiState.totalSpentMinor,
            remainingMinor = uiState.totalRemainingMinor,
            onEdit = { showMonthlyDialog = true }
        )
        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Category limits")
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.categories.isEmpty()) {
            EmptyCategoryBudgets()
        } else {
            CategoryBudgetList(
                statuses = uiState.categoryStatuses,
                onEdit = { editingCategory = it }
            )
        }
    }

    if (showMonthlyDialog) {
        MonthlyBudgetDialog(
            existingLimitMinor = uiState.monthlyBudget?.totalLimitMinor,
            onDismiss = { showMonthlyDialog = false },
            onSave = { limitMinor ->
                onSaveMonthlyBudget(limitMinor, uiState.monthlyBudget?.id)
                showMonthlyDialog = false
            }
        )
    }

    editingCategory?.let { category ->
        val existingBudget = categoryBudgetMap[category.id]
        CategoryBudgetDialog(
            category = category,
            existingLimitMinor = existingBudget?.limitMinor,
            onDismiss = { editingCategory = null },
            onSave = { limitMinor ->
                onSaveCategoryBudget(category.id, limitMinor, existingBudget?.id)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun MonthlyBudgetCard(
    limitMinor: Long?,
    spentMinor: Long,
    remainingMinor: Long?,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total budget",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onEdit) {
                    Text(text = "Edit")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = limitMinor?.let { formatAmount(it) } ?: "No budget set",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (limitMinor != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (spentMinor.toFloat() / limitMinor).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if ((remainingMinor ?: 0L) < 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Spent: ${formatAmount(spentMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Remaining: ${remainingMinor?.let { formatAmount(it) } ?: "0.00 MAD"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if ((remainingMinor ?: 0L) < 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetList(
    statuses: List<BudgetsViewModel.CategoryBudgetStatus>,
    onEdit: (CategoryEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(statuses, key = { it.category.id }) { status ->
            CategoryBudgetRow(
                name = status.category.name,
                limitMinor = status.limitMinor,
                spentMinor = status.spentMinor,
                remainingMinor = status.remainingMinor,
                isOverspent = status.isOverspent,
                onEdit = { onEdit(status.category) }
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    name: String,
    limitMinor: Long?,
    spentMinor: Long,
    remainingMinor: Long?,
    isOverspent: Boolean,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (limitMinor == null) {
                    Text(
                        text = "No limit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val progressValue = (spentMinor.toFloat() / limitMinor).coerceIn(0f, 1f)
                    Text(
                        text = "Limit: ${formatAmount(limitMinor)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isOverspent) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Spent: ${formatAmount(spentMinor)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Remaining: ${remainingMinor?.let { formatAmount(it) } ?: "0.00 MAD"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOverspent) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            TextButton(onClick = onEdit) {
                Text(text = "Set")
            }
        }
    }
}

@Composable
private fun EmptyCategoryBudgets() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No category limits",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Set category budgets to track limits.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyBudgetDialog(
    existingLimitMinor: Long?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember {
        mutableStateOf(existingLimitMinor?.let { formatAmount(it).removeSuffix(" MAD") } ?: "")
    }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Set monthly budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total budget") },
                    isError = amountError != null,
                    singleLine = true
                )
                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSave(amountMinor)
                    }
                }
            ) {
                Text("Save")
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
private fun CategoryBudgetDialog(
    category: CategoryEntity,
    existingLimitMinor: Long?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember {
        mutableStateOf(existingLimitMinor?.let { formatAmount(it).removeSuffix(" MAD") } ?: "")
    }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Set ${category.name} limit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Category limit") },
                    isError = amountError != null,
                    singleLine = true
                )
                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSave(amountMinor)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

private fun formatMonthLabel(month: String): String {
    if (month.isBlank()) return "This month"
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        YearMonth.parse(month).format(formatter)
    }.getOrDefault(month)
}
