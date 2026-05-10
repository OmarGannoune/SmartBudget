package com.omargannoune.smartbudget.ui.goals

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
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
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
    onAddContribution: (goalId: Long, amountMinor: Long, note: String?) -> Unit
) {
    var showAddGoal by remember { mutableStateOf(false) }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Savings goals",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { showAddGoal = true }) {
            Text(text = "Add goal")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.goals.isEmpty()) {
            EmptyGoalsState()
        } else {
            GoalsList(
                goals = uiState.goals,
                onAddContribution = { selectedGoalId = it }
            )
        }
    }

    if (showAddGoal) {
        AddGoalDialog(
            onDismiss = { showAddGoal = false },
            onSave = { name, amountMinor, targetDate ->
                onAddGoal(name, amountMinor, targetDate)
                showAddGoal = false
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
    onAddContribution: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(goals, key = { it.id }) { goal ->
            GoalCard(goal = goal, onAddContribution = onAddContribution)
        }
    }
}

@Composable
private fun GoalCard(goal: SavingsGoalEntity, onAddContribution: (Long) -> Unit) {
    val remaining = (goal.targetAmountMinor - goal.currentAmountMinor).coerceAtLeast(0)
    val progress = if (goal.targetAmountMinor > 0L) {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
    } else {
        0f
    }
    val deadlineInfo = remember(goal.targetDate) { buildDeadlineInfo(goal.targetDate) }
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
                color = if (goal.isCompleted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatAmount(goal.currentAmountMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Target: ${formatAmount(goal.targetAmountMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Remaining: ${formatAmount(remaining)}",
                style = MaterialTheme.typography.bodyMedium,
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
            Button(onClick = { onAddContribution(goal.id) }) {
                Text(text = "Add contribution")
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
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, targetMinor: Long, targetDate: String?) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Goal name") },
                    isError = nameError != null,
                    singleLine = true
                )
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Target amount") },
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
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Target date (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = null
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (nameText.isBlank()) {
                        nameError = "Enter a goal name"
                    }
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    }
                    if (nameError == null && amountError == null) {
                        onSave(
                            nameText.trim(),
                            amountMinor ?: 0L,
                            dateText.trim().ifBlank { null }
                        )
                    }
                }
            ) {
                Text("Save goal")
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
        title = { Text(text = "Add contribution") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0) {
                        amountError = "Enter a valid amount"
                    }
                    if (amountError == null) {
                        onSave(amountMinor ?: 0L, noteText.trim().ifBlank { null })
                    }
                }
            ) {
                Text("Add")
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
