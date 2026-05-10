package com.omargannoune.smartbudget.ui.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import android.content.Intent
import java.io.File

@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    modifier: Modifier = Modifier,
    onCreateCategory: (String) -> Unit,
    onRenameCategory: (CategoryEntity, String) -> Unit,
    onArchiveCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onOpenRecurring: () -> Unit,
    onExportCsv: (android.content.Context) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var renamingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onExportCsv(context) }) {
                Text(text = "Export CSV")
            }
            TextButton(
                onClick = {
                    val path = uiState.exportFilePath ?: return@TextButton
                    val file = File(path)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(sendIntent, "Share CSV")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                },
                enabled = uiState.exportFilePath != null
            ) {
                Text(text = "Share CSV")
            }
        }
        uiState.exportMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onOpenRecurring) {
            Text(text = "Recurring bills")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { showAddDialog = true }) {
            Text(text = "Add category")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.categories.isEmpty()) {
            EmptyCategoriesState()
        } else {
            CategoryList(
                categories = uiState.categories,
                onRename = { renamingCategory = it },
                onArchive = onArchiveCategory,
                onDelete = onDeleteCategory
            )
        }
    }

    if (showAddDialog) {
        CategoryDialog(
            title = "Add category",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onSave = {
                onCreateCategory(it)
                showAddDialog = false
            }
        )
    }

    renamingCategory?.let { category ->
        CategoryDialog(
            title = "Rename category",
            initialValue = category.name,
            onDismiss = { renamingCategory = null },
            onSave = { newName ->
                onRenameCategory(category, newName)
                renamingCategory = null
            }
        )
    }
}

@Composable
private fun CategoryList(
    categories: List<CategoryEntity>,
    onRename: (CategoryEntity) -> Unit,
    onArchive: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories, key = { it.id }) { category ->
            CategoryRow(
                category = category,
                onRename = onRename,
                onArchive = onArchive,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onRename: (CategoryEntity) -> Unit,
    onArchive: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (category.isActive) "Active" else "Archived",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onRename(category) }) {
                    Text(text = "Rename")
                }
                TextButton(onClick = { onArchive(category) }) {
                    Text(text = if (category.isActive) "Archive" else "Unarchive")
                }
                TextButton(onClick = { onDelete(category) }) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameText by remember { mutableStateOf(initialValue) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Category name") },
                    isError = errorText != null,
                    singleLine = true
                )
                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameText.isBlank()) {
                        errorText = "Enter a category name"
                    } else {
                        onSave(nameText.trim())
                    }
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun EmptyCategoriesState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No categories yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add a category to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
