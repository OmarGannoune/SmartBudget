package com.omargannoune.smartbudget.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.ui.components.ScreenTitle

@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    modifier: Modifier = Modifier,
    onClearData: () -> Unit,
    onExportCsv: (android.content.Context) -> Unit,
    // Keep existing if needed for category management sub-flow
    onOpenRecurring: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        ScreenTitle(text = "Settings")
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                SettingsSection(title = "CATEGORIES") {
                    SettingsItem(
                        icon = Lucide.Tag,
                        title = "Manage categories",
                        onClick = { /* Navigate or open dialog */ }
                    )
                }
            }

            item {
                SettingsSection(title = "CURRENCY") {
                    SettingsItem(
                        icon = Lucide.DollarSign,
                        title = "Change currency",
                        value = uiState.currency,
                        onClick = { /* Change currency logic */ }
                    )
                }
            }

            item {
                SettingsSection(title = "EXPORT") {
                    SettingsItem(
                        icon = Lucide.Download,
                        title = "Export CSV",
                        onClick = { onExportCsv(context) }
                    )
                }
            }

            item {
                SettingsSection(title = "ABOUT") {
                    SettingsItem(
                        icon = Lucide.Info,
                        title = "App info",
                        value = "1.0.0",
                        onClick = { }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    elevation = null
                ) {
                    Icon(
                        imageVector = Lucide.Trash2,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Clear all data",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This will delete all your expenses, budgets and goals. You will be taken back to onboarding.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
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

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
