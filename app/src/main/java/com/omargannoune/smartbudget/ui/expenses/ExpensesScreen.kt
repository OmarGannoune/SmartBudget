package com.omargannoune.smartbudget.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
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
    onNextMonth: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenTitle(text = "History")
        Spacer(modifier = Modifier.height(12.dp))
        MonthHeader(
            month = uiState.month,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Spacer(modifier = Modifier.height(16.dp))
        TotalSpentCard(totalMinor = uiState.totalMinor, currency = uiState.currency)
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = "Add expense",
            onClick = onAddExpenseClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.expenses.isEmpty()) {
            EmptyExpensesState()
        } else {
            ExpensesList(
                expenses = uiState.expenses,
                categories = uiState.allCategories,
                currency = uiState.currency
            )
        }
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
private fun TotalSpentCard(totalMinor: Long, currency: String = "MAD") {
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
        }
    }
}

@Composable
private fun ExpensesList(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>, currency: String = "MAD") {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            val category = categories.find { it.id == expense.categoryId }
            ExpenseRow(expense = expense, category = category, currency = currency)
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseEntity, category: CategoryEntity?, currency: String = "MAD") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val catColor = getCategoryColor(category?.color)
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
                    Text(category?.name ?: "Unknown", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    if (!expense.note.isNullOrBlank()) {
                        Text(expense.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatAmount(expense.amountMinor, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(expense.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
