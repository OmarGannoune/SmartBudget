package com.omargannoune.smartbudget.ui.expenses

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExpensesScreen(
    uiState: ExpensesViewModel.ExpensesUiState,
    modifier: Modifier = Modifier,
    openAdd: Boolean = false,
    onAddExpense: (
        amountMinor: Long,
        date: String,
        categoryId: Long,
        note: String?,
        paymentMethod: String?,
        necessityRating: Int?
    ) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var showAddExpense by remember { mutableStateOf(false) }
    var shouldOpenAdd by remember(openAdd) { mutableStateOf(openAdd) }

    LaunchedEffect(shouldOpenAdd) {
        if (shouldOpenAdd) {
            showAddExpense = true
            shouldOpenAdd = false
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenTitle(text = "Expenses")
        Spacer(modifier = Modifier.height(12.dp))
        MonthHeader(
            month = uiState.month,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Spacer(modifier = Modifier.height(16.dp))
        TotalCard(totalMinor = uiState.totalMinor)
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = "Add expense",
            onClick = { showAddExpense = true },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.expenses.isEmpty()) {
            EmptyExpensesState()
        } else {
            ExpensesList(
                expenses = uiState.expenses,
                categories = uiState.allCategories
            )
        }
    }

    if (showAddExpense) {
        AddExpenseDialog(
            categories = uiState.activeCategories,
            onDismiss = { showAddExpense = false },
            onSave = { amountMinor, date, categoryId, note, paymentMethod, necessity ->
                onAddExpense(amountMinor, date, categoryId, note, paymentMethod, necessity)
                showAddExpense = false
            }
        )
    }
}

@Composable
private fun MonthHeader(
    month: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatMonthLabel(month),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Lucide.ChevronLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Lucide.ChevronRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatMonthLabel(month: String): String {
    if (month.isBlank()) return "This month"
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        YearMonth.parse(month).format(formatter)
    }.getOrDefault(month)
}

@Composable
private fun TotalCard(totalMinor: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total Spent",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(totalMinor),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpensesList(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            val category = categories.find { it.id == expense.categoryId }
            ExpenseRow(expense = expense, category = category)
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseEntity, category: CategoryEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val catColor = try {
                    Color(android.graphics.Color.parseColor(category?.color ?: "#5DE2C6"))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.tertiary
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category?.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = catColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = category?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (!expense.note.isNullOrBlank()) {
                        Text(
                            text = expense.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmount(expense.amountMinor),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyExpensesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Lucide.ReceiptText,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add your first expense to start tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        amountMinor: Long,
        date: String,
        categoryId: Long,
        note: String?,
        paymentMethod: String?,
        necessityRating: Int?
    ) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var noteText by remember { mutableStateOf("") }
    var paymentMethodText by remember { mutableStateOf("") }
    var necessityText by remember { mutableStateOf("") }
    var selectedCategoryId by remember {
        mutableStateOf(categories.firstOrNull()?.id)
    }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Expense", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    isError = amountError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                CategoryPicker(
                    categories = categories,
                    selectedId = selectedCategoryId,
                    onSelected = { selectedCategoryId = it },
                    isError = categoryError != null
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = paymentMethodText,
                    onValueChange = { paymentMethodText = it },
                    label = { Text("Payment Method") },
                    placeholder = { Text("Cash, Card, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = necessityText,
                        onValueChange = { necessityText = it },
                        label = { Text("Necessity (1-10)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountError = null
                    categoryError = null

                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    }
                    if (selectedCategoryId == null) {
                        categoryError = "Select a category"
                    }
                    if (amountError == null && categoryError == null) {
                        val necessity = necessityText.toIntOrNull()?.coerceIn(1, 10)
                        onSave(
                            amountMinor ?: 0L,
                            dateText,
                            selectedCategoryId ?: 0L,
                            noteText.ifBlank { null },
                            paymentMethodText.ifBlank { null },
                            necessity
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
private fun CategoryPicker(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    isError: Boolean
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
            isError = isError,
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
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
