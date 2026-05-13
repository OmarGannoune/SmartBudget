package com.omargannoune.smartbudget.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.R
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.ExpenseRowComponent
import com.omargannoune.smartbudget.ui.components.formatAmount
import com.omargannoune.smartbudget.ui.components.getCategoryColor
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExpensesScreen(
    uiState: ExpensesViewModel.ExpensesUiState,
    modifier: Modifier = Modifier,
    onAddExpenseClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onDeleteExpense: (Long) -> Unit = {},
    onUpdateExpense: (
        expenseId: Long,
        amountMinor: Long,
        date: String,
        categoryId: Long,
        note: String?,
        paymentMethod: String?,
        necessityRating: Int?
    ) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var selectedExpenseForEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    // Filter expenses (single selection, null = All)
    val filteredExpenses = remember(uiState.expenses, selectedCategoryId) {
        var filtered = uiState.expenses
        if (selectedCategoryId != null) {
            filtered = filtered.filter { it.categoryId == selectedCategoryId }
        }
        filtered
    }

    // Make the entire page scrollable by using a LazyColumn that contains header items
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            ScreenTitle(text = "History")
            Spacer(modifier = Modifier.height(12.dp))
            MonthHeader(
                month = uiState.month,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
            Spacer(modifier = Modifier.height(16.dp))
            TotalSpentCard(totalMinor = uiState.totalMinor, previousMonthTotalMinor = uiState.previousMonthTotalMinor, currency = uiState.currency)
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "Add expense",
                onClick = onAddExpenseClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Filter tabs only (no sort)
            FilterSortBar(
                categories = uiState.allCategories,
                selectedCategoryId = selectedCategoryId,
                onSelectCategory = { id -> selectedCategoryId = id }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredExpenses.isEmpty()) {
            item { EmptyExpensesState() }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                val category = uiState.allCategories.find { it.id == expense.categoryId }
                ExpenseRowComponent(
                    expense = expense,
                    categoryName = category?.name ?: "Unknown",
                    categoryIcon = category?.icon,
                    categoryColorHex = category?.color,
                    currency = uiState.currency,
                    onEdit = {
                        selectedExpenseForEdit = expense
                        showEditSheet = true
                        onEditExpense(expense)
                    },
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }
    }

    if (showEditSheet && selectedExpenseForEdit != null) {
        AddExpenseBottomSheet(
            categories = uiState.allCategories,
            expenseToEdit = selectedExpenseForEdit,
            onDismiss = {
                showEditSheet = false
                selectedExpenseForEdit = null
            },
            onSave = { amountMinor, date, categoryId, note, paymentMethod, necessityRating ->
                onUpdateExpense(
                    selectedExpenseForEdit!!.id,
                    amountMinor,
                    date,
                    categoryId,
                    note,
                    paymentMethod,
                    necessityRating
                )
                showEditSheet = false
                selectedExpenseForEdit = null
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
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(Lucide.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Lucide.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TotalSpentCard(totalMinor: Long, previousMonthTotalMinor: Long = 0L, currency: String = "MAD") {
    val percentageChange = if (previousMonthTotalMinor > 0) {
        ((totalMinor - previousMonthTotalMinor).toDouble() / previousMonthTotalMinor) * 100
    } else {
        0.0
    }
    
    val comparisonColor = when {
        percentageChange > 0 -> Color(0xFFEF5350)  // Red for increase
        percentageChange < 0 -> Color(0xFF66BB6A)  // Green for decrease
        else -> MaterialTheme.colorScheme.onSurfaceVariant  // Gray for no change
    }
    
    val comparisonText = when {
        percentageChange > 0 -> "+${String.format("%.1f", percentageChange)}% vs last month"
        percentageChange < 0 -> "${String.format("%.1f", percentageChange)}% vs last month"
        else -> "Same as last month"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Total Spent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = formatAmount(totalMinor, currency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comparisonText,
                style = MaterialTheme.typography.labelSmall,
                color = comparisonColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExpensesList(
    expenses: List<ExpenseEntity>,
    categories: List<CategoryEntity>,
    currency: String = "MAD",
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onDeleteExpense: (Long) -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            val category = categories.find { it.id == expense.categoryId }
            ExpenseRowComponent(
                expense = expense,
                categoryName = category?.name ?: "Unknown",
                categoryIcon = category?.icon,
                categoryColorHex = category?.color,
                currency = currency,
                onEdit = { onEditExpense(expense) },
                onDelete = { onDeleteExpense(expense.id) }
            )
        }
    }
}

@Composable
private fun EmptyExpensesState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Lucide.ReceiptText, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No expenses yet", style = MaterialTheme.typography.titleMedium)
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
private fun FilterSortBar(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onSelectCategory: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // All tab
        val allSelected = selectedCategoryId == null
        Surface(
            onClick = { onSelectCategory(null) },
            tonalElevation = if (allSelected) 6.dp else 0.dp,
            shape = RoundedCornerShape(20.dp),
            color = if (allSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.04f),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("All", style = MaterialTheme.typography.labelSmall, color = if (allSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (allSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }

        categories.filter { it.isActive }.forEach { category ->
            val isSelected = selectedCategoryId == category.id
            val catColor = try {
                Color(android.graphics.Color.parseColor(category.color ?: "#5DE2C6"))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.tertiary
            }

            Surface(
                onClick = { onSelectCategory(category.id) },
                tonalElevation = if (isSelected) 6.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) catColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.04f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, catColor) else null,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.icon),
                        contentDescription = category.name,
                        modifier = Modifier.size(16.dp),
                        tint = catColor
                    )
                    Text(
                        category.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
