package com.omargannoune.smartbudget.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.ExpenseEntity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.background
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getCategoryIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "ShoppingBag" -> Lucide.ShoppingBag
        "Utensils" -> Lucide.Utensils
        "Bus" -> Lucide.Bus
        "HeartPulse" -> Lucide.HeartPulse
        "Gamepad2" -> Lucide.Gamepad2
        "GraduationCap" -> Lucide.GraduationCap
        "Home" -> Lucide.House
        "Zap" -> Lucide.Zap
        "Car" -> Lucide.Car
        "Smartphone" -> Lucide.Smartphone
        "Plane" -> Lucide.Plane
        "Gift" -> Lucide.Gift
        else -> Lucide.LayoutGrid
    }
}

@Composable
fun getCategoryColor(colorHex: String?): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex ?: "#5DE2C6"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.tertiary
    }
}

object CategoryDefaults {
    val Icons = listOf(
        "ShoppingBag", "Utensils", "Bus", "HeartPulse",
        "Gamepad2", "GraduationCap", "Home", "Zap",
        "Car", "Smartphone", "Plane", "Gift"
    )

    val Colors = listOf(
        "#5DE2C6", "#C7D1FF", "#FFFFB86B", "#FF6B6B",
        "#3BD671", "#F5C451", "#A9B1BF", "#F2F4F8",
        "#9C27B0", "#2196F3", "#009688", "#E91E63"
    )
}

fun formatAmount(amountMinor: Long, currency: String = "MAD"): String {
    val major = amountMinor / 100
    val minor = kotlin.math.abs(amountMinor % 100)
    return "$major.${minor.toString().padStart(2, '0')} $currency"
}

@Composable
fun PaymentMethodIcon(method: String?, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    val icon = when (method?.lowercase()) {
        "cash" -> Lucide.Banknote
        "card" -> Lucide.CreditCard
        "transfer" -> Lucide.ArrowRightLeft
        else -> Lucide.Receipt
    }
    Icon(icon, contentDescription = method, modifier = modifier, tint = tint)
}

@Composable
fun ExpenseRowComponent(
    expense: ExpenseEntity,
    categoryName: String,
    categoryIcon: String?,
    categoryColorHex: String?,
    currency: String,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    // If necessity is 3 or below, we consider it "less necessary" and pop it out
    val isLessNecessary = (expense.necessityRating ?: 5) <= 3
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLessNecessary) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isLessNecessary) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha=0.5f)) else null
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val catColor = getCategoryColor(categoryColorHex)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(categoryIcon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = catColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(categoryName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            if (isLessNecessary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Lucide.TriangleAlert, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (!expense.note.isNullOrBlank()) {
                            Text(expense.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(expense.amountMinor, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLessNecessary) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PaymentMethodIcon(expense.paymentMethod, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(expense.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Lucide.Pencil,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Lucide.Trash2,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
