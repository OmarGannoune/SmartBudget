package com.omargannoune.smartbudget.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.SectionTitle
import com.omargannoune.smartbudget.ui.components.formatAmount
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
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
            currency = uiState.currency,
            onEdit = { showMonthlyDialog = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(text = "Category limits")
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.categories.isEmpty()) {
            EmptyCategoryBudgets()
        } else {
            CategoryBudgetList(
                statuses = uiState.categoryStatuses,
                currency = uiState.currency,
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
    currency: String = "MAD",
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total budget",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onEdit) {
                    Text(text = "Edit", color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = limitMinor?.let { formatAmount(it, currency) } ?: "No budget set",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (limitMinor != null) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (spentMinor.toFloat() / limitMinor).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if ((remainingMinor ?: 0L) < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Spent: ${formatAmount(spentMinor, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Remaining: ${remainingMinor?.let { formatAmount(it, currency) } ?: "0.00 $currency"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if ((remainingMinor ?: 0L) < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetList(
    statuses: List<BudgetsViewModel.CategoryBudgetStatus>,
    currency: String = "MAD",
    onEdit: (CategoryEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(statuses, key = { it.category.id }) { status ->
            CategoryBudgetRow(
                category = status.category,
                limitMinor = status.limitMinor,
                spentMinor = status.spentMinor,
                remainingMinor = status.remainingMinor,
                isOverspent = status.isOverspent,
                currency = currency,
                onEdit = { onEdit(status.category) }
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    category: CategoryEntity,
    limitMinor: Long?,
    spentMinor: Long,
    remainingMinor: Long?,
    isOverspent: Boolean,
    currency: String = "MAD",
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.color ?: "#5DE2C6"))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.tertiary
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = catColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onEdit) {
                    Text(text = if (limitMinor == null) "Set Limit" else "Edit", color = MaterialTheme.colorScheme.tertiary)
                }
            }
            
            if (limitMinor != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val progressValue = (spentMinor.toFloat() / limitMinor).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Spent ${formatAmount(spentMinor, currency)} / ${formatAmount(limitMinor, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isOverspent) "Over by ${formatAmount(kotlin.math.abs(remainingMinor ?: 0L), currency)}" else "${remainingMinor?.let { formatAmount(it, currency) }} left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCategoryBudgets() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
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
        mutableStateOf(existingLimitMinor?.let { (it / 100).toString() } ?: "")
    }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Monthly Budget", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total limit") },
                    isError = amountError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
            Button(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSave(amountMinor)
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
private fun CategoryBudgetDialog(
    category: CategoryEntity,
    existingLimitMinor: Long?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember {
        mutableStateOf(existingLimitMinor?.let { (it / 100).toString() } ?: "")
    }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Limit for ${category.name}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Category limit") },
                    isError = amountError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
            Button(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSave(amountMinor)
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
