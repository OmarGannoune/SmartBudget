package com.omargannoune.smartbudget.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.formatAmount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun GoalsScreen(
    uiState: GoalsViewModel.GoalsUiState,
    modifier: Modifier = Modifier,
    onAddGoal: (name: String, targetMinor: Long, targetDate: String?) -> Unit,
    onEditGoal: (id: Long, name: String, targetMinor: Long, targetDate: String?) -> Unit,
    onDeleteGoal: (id: Long) -> Unit,
    onAddContribution: (goalId: Long, amountMinor: Long, note: String?) -> Unit
) {
    var showAddGoal by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ScreenTitle(text = "Savings goals")
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(
            text = "Add goal",
            onClick = { showAddGoal = true },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.goals.isEmpty()) {
            EmptyGoalsState()
        } else {
            GoalsList(
                goals = uiState.goals,
                currency = uiState.currency,
                onAddContribution = { selectedGoalId = it },
                onEdit = { editingGoal = it },
                onDelete = { goalToDelete = it }
            )
        }
    }

    if (showAddGoal) {
        AddGoalBottomSheet(
            existingGoal = null,
            onDismiss = { showAddGoal = false },
            onSave = { name, amountMinor, targetDate ->
                onAddGoal(name, amountMinor, targetDate)
                showAddGoal = false
            }
        )
    }

    editingGoal?.let { goal ->
        AddGoalBottomSheet(
            existingGoal = goal,
            onDismiss = { editingGoal = null },
            onSave = { name, amountMinor, targetDate ->
                onEditGoal(goal.id, name, amountMinor, targetDate)
                editingGoal = null
            }
        )
    }

    goalToDelete?.let { goal ->
        DeleteGoalConfirmationDialog(
            goalName = goal.name,
            onDismiss = { goalToDelete = null },
            onConfirm = {
                onDeleteGoal(goal.id)
                goalToDelete = null
            }
        )
    }

    selectedGoalId?.let { goalId ->
        AddContributionDialog(
            onDismiss = { selectedGoalId = null },
            onSave = { amountMinor, note ->
                onAddContribution(goalId, amountMinor, note)
                selectedGoalId = null
            }
        )
    }
}

@Composable
private fun GoalsList(
    goals: List<SavingsGoalEntity>,
    currency: String = "MAD",
    onAddContribution: (Long) -> Unit,
    onEdit: (SavingsGoalEntity) -> Unit,
    onDelete: (SavingsGoalEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(goals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                currency = currency,
                onAddContribution = onAddContribution,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoalEntity,
    currency: String = "MAD",
    onAddContribution: (Long) -> Unit,
    onEdit: (SavingsGoalEntity) -> Unit,
    onDelete: (SavingsGoalEntity) -> Unit
) {
    val remaining = (goal.targetAmountMinor - goal.currentAmountMinor).coerceAtLeast(0)
    val progress = if (goal.targetAmountMinor > 0L) {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
    } else {
        0f
    }
    val deadlineInfo = remember(goal.targetDate) { buildDeadlineInfo(goal.targetDate) }
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
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    TextButton(onClick = { onEdit(goal) }) {
                        Text("Edit", color = MaterialTheme.colorScheme.tertiary)
                    }
                    TextButton(onClick = { onDelete(goal) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (goal.isCompleted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatAmount(goal.currentAmountMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Target: ${formatAmount(goal.targetAmountMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Remaining: ${formatAmount(remaining, currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (goal.isCompleted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (goal.isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            if (deadlineInfo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target date: ${deadlineInfo.formattedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (deadlineInfo.warning != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = deadlineInfo.warning,
                        style = MaterialTheme.typography.labelSmall,
                        color = deadlineWarningColor(deadlineInfo.severity)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryButton(
                text = "Add contribution",
                onClick = { onAddContribution(goal.id) },
                modifier = Modifier.fillMaxWidth()
            )
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
            text = "Set your first goal",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Choose a goal and track your progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeleteGoalConfirmationDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Delete goal?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$goalName\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.background)
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
private fun AddContributionDialog(
    onDismiss: () -> Unit,
    onSave: (amountMinor: Long, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Add contribution",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    }
                    if (amountError == null) {
                        onSave(amountMinor ?: 0L, noteText.trim().ifBlank { null })
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

private fun parseAmountToMinor(amountText: String): Long? {
    val normalized = amountText.trim().replace(',', '.')
    if (normalized.isBlank()) return null
    return runCatching {
        val decimal = BigDecimal(normalized)
        decimal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}

private data class DeadlineInfo(
    val formattedDate: String,
    val warning: String?,
    val severity: DeadlineSeverity
)

private enum class DeadlineSeverity {
    None,
    Soon,
    DueToday,
    Overdue
}

private fun buildDeadlineInfo(targetDate: String?): DeadlineInfo? {
    if (targetDate.isNullOrBlank()) return null
    val date = runCatching { LocalDate.parse(targetDate) }.getOrNull() ?: return null
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, date)
    val warning = when {
        daysLeft < 0 -> "Past target date"
        daysLeft == 0L -> "Due today"
        daysLeft in 1..7 -> "$daysLeft days left"
        else -> null
    }
    val severity = when {
        daysLeft < 0 -> DeadlineSeverity.Overdue
        daysLeft == 0L -> DeadlineSeverity.DueToday
        daysLeft in 1..7 -> DeadlineSeverity.Soon
        else -> DeadlineSeverity.None
    }
    return DeadlineInfo(
        formattedDate = date.format(formatter),
        warning = warning,
        severity = severity
    )
}

@Composable
private fun deadlineWarningColor(severity: DeadlineSeverity) = when (severity) {
    DeadlineSeverity.Overdue -> MaterialTheme.colorScheme.error
    DeadlineSeverity.DueToday -> MaterialTheme.colorScheme.error
    DeadlineSeverity.Soon -> MaterialTheme.colorScheme.secondary
    DeadlineSeverity.None -> MaterialTheme.colorScheme.onSurfaceVariant
}
