package com.omargannoune.smartbudget.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronRight
import com.omargannoune.smartbudget.R
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.SectionTitle
import com.omargannoune.smartbudget.ui.components.formatAmount
import com.omargannoune.smartbudget.ui.components.getCategoryIcon

@Composable
fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit,
    onSeeMoreExpenses: () -> Unit,
    onSeeAllGoals: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .alpha(0.2f),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 80.dp)
        ) {
        Text(
            text = uiState.greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCard(
            month = uiState.month,
            totalSpentMinor = uiState.totalSpentMinor,
            budgetLimitMinor = uiState.monthlyBudget?.totalLimitMinor,
            remainingMinor = uiState.remainingMinor,
            currency = uiState.currency
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "Add expense",
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(text = "Your goals")
            if (uiState.goals.isNotEmpty()) {
                TextButton(onClick = onSeeAllGoals) {
                    Text("See all", color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.goals.isEmpty()) {
            EmptyGoalsState()
        } else {
            GoalsPreview(goals = uiState.goals.take(3), currency = uiState.currency)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(text = "Quick insights")
            TextButton(onClick = onSeeMoreExpenses) {
                Text("See History", color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Lucide.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.topCategories.isEmpty()) {
            EmptyInsightsState()
        } else {
            TopCategoriesList(categories = uiState.topCategories, currency = uiState.currency)
        }
        }
    }
}

@Composable
private fun SummaryCard(
    month: String,
    totalSpentMinor: Long,
    budgetLimitMinor: Long?,
    remainingMinor: Long?,
    currency: String = "MAD"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Spent this month",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(totalSpentMinor, currency),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (budgetLimitMinor != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val progress = (totalSpentMinor.toFloat() / budgetLimitMinor).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if ((remainingMinor ?: 0L) < 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = remainingMinor?.let { formatAmount(it) } ?: "0.00 MAD",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if ((remainingMinor ?: 0L) < 0L) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsPreview(goals: List<SavingsGoalEntity>, currency: String = "MAD") {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { goal ->
            GoalPreviewCard(goal = goal, currency = currency)
        }
    }
}

@Composable
private fun GoalPreviewCard(goal: SavingsGoalEntity, currency: String = "MAD") {
    val progress = if (goal.targetAmountMinor > 0L) {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
    } else {
        0f
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatAmount(goal.targetAmountMinor, currency),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun TopCategoriesList(categories: List<HomeViewModel.CategorySpend>, currency: String = "MAD") {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.color ?: "#5DE2C6"))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.tertiary
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = catColor
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatAmount(category.spentMinor, currency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsState() {
    Text(
        text = "No active goals.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EmptyInsightsState() {
    Text(
        text = "No expenses to show insights.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
