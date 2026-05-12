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
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Text(
            text = text,
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
