package com.omargannoune.smartbudget.ui.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.SectionTitle
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenTitle(text = "Home")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = uiState.greeting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCard(
            month = uiState.month,
            totalSpentMinor = uiState.totalSpentMinor,
            budgetLimitMinor = uiState.monthlyBudget?.totalLimitMinor,
            remainingMinor = uiState.remainingMinor
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Add expense", onClick = onAddExpense)
        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Your goals")
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.goals.isEmpty()) {
            EmptyGoalsState()
        } else {
            GoalsPreview(goals = uiState.goals)
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Quick insights")
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.topCategories.isEmpty()) {
            EmptyInsightsState()
        } else {
            TopCategoriesList(categories = uiState.topCategories)
        }
    }
}

@Composable
private fun SummaryCard(
    month: String,
    totalSpentMinor: Long,
    budgetLimitMinor: Long?,
    remainingMinor: Long?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "This month (${formatMonthLabel(month)})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Spent: ${formatAmount(totalSpentMinor)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (budgetLimitMinor != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val progress = (totalSpentMinor.toFloat() / budgetLimitMinor).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if ((remainingMinor ?: 0L) < 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Remaining: ${remainingMinor?.let { formatAmount(it) } ?: "0.00 MAD"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if ((remainingMinor ?: 0L) < 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No budget set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalsPreview(goals: List<SavingsGoalEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { goal ->
            GoalPreviewCard(goal = goal)
        }
    }
}

@Composable
private fun GoalPreviewCard(goal: SavingsGoalEntity) {
    val progress = if (goal.targetAmountMinor > 0L) {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
    } else {
        0f
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatAmount(goal.currentAmountMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Target: ${formatAmount(goal.targetAmountMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopCategoriesList(categories: List<HomeViewModel.CategorySpend>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories, key = { it.name }) { category ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatAmount(category.spentMinor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No goals yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Create a savings goal to stay motivated.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyInsightsState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add your first expense to unlock insights.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMonthLabel(month: String): String {
    if (month.isBlank()) return "This month"
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        YearMonth.parse(month).format(formatter)
    }.getOrDefault(month)
}

private fun formatAmount(amountMinor: Long): String {
    val major = amountMinor / 100
    val minor = kotlin.math.abs(amountMinor % 100)
    return "$major.${minor.toString().padStart(2, '0')} MAD"
}
