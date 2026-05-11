package com.omargannoune.smartbudget.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.ui.components.CategoryDefaults
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.getCategoryColor
import com.omargannoune.smartbudget.ui.components.getCategoryIcon

@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    modifier: Modifier = Modifier,
    onClearData: () -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onExportCsv: (android.content.Context) -> Unit,
    onCreateCategory: (String, String?, String?) -> Unit,
    onRenameCategory: (CategoryEntity, String, String?, String?) -> Unit,
    onArchiveCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onClearExportMessage: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

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
                        onClick = { showManageCategories = !showManageCategories }
                    )
                }
            }

            if (showManageCategories) {
                item {
                    CategoryManagementSection(
                        categories = uiState.categories,
                        onAddClick = { showAddCategory = true },
                        onEdit = { editingCategory = it },
                        onArchive = onArchiveCategory,
                        onDelete = onDeleteCategory
                    )
                }
            }

            item {
                SettingsSection(title = "CURRENCY") {
                    SettingsItem(
                        icon = Lucide.DollarSign,
                        title = "Change currency",
                        value = uiState.currency,
                        onClick = { showCurrencyPicker = true }
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
                    Icon(Lucide.Trash2, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Clear all data", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (uiState.exportMessage != null) {
        AlertDialog(
            onDismissRequest = onClearExportMessage,
            title = { Text("Export") },
            text = { Text(uiState.exportMessage) },
            confirmButton = {
                TextButton(onClick = onClearExportMessage) { Text("OK") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This will delete all your expenses, budgets and goals. You will be taken back to onboarding.") },
            confirmButton = {
                TextButton(
                    onClick = { onClearData(); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            current = uiState.currency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { onUpdateCurrency(it); showCurrencyPicker = false }
        )
    }

    if (showAddCategory) {
        CategoryEditDialog(
            title = "Add Category",
            onDismiss = { showAddCategory = false },
            onSave = { name, icon, color -> 
                onCreateCategory(name, icon, color)
                showAddCategory = false
            }
        )
    }

    editingCategory?.let { category ->
        CategoryEditDialog(
            title = "Edit Category",
            initialName = category.name,
            initialIcon = category.icon,
            initialColor = category.color,
            onDismiss = { editingCategory = null },
            onSave = { name, icon, color ->
                onRenameCategory(category, name, icon, color)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun CategoryManagementSection(
    categories: List<CategoryEntity>,
    onAddClick: () -> Unit,
    onEdit: (CategoryEntity) -> Unit,
    onArchive: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Categories", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = onAddClick) {
                Icon(Lucide.Plus, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New")
            }
        }
        categories.forEach { category ->
            val catColor = getCategoryColor(category.color)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = catColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.name,
                        color = if (category.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Row {
                    IconButton(onClick = { onEdit(category) }) {
                        Icon(Lucide.Pencil, null, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onArchive(category) }) {
                        Icon(if (category.isActive) Lucide.Archive else Lucide.ArchiveRestore, null, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDelete(category) }) {
                        Icon(Lucide.X, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("MAD", "USD", "EUR", "GBP", "JPY")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                currencies.forEach { curr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(curr) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = curr == current, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(curr)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun CategoryEditDialog(
    title: String,
    initialName: String = "",
    initialIcon: String? = CategoryDefaults.Icons.first(),
    initialColor: String? = CategoryDefaults.Colors.first(),
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Groceries") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Icon", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.height(150.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(CategoryDefaults.Icons) { iconName ->
                                val isSelected = selectedIcon == iconName
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { selectedIcon = iconName }
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Color", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryDefaults.Colors.take(6).forEach { colorHex ->
                            ColorOption(
                                colorHex = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryDefaults.Colors.drop(6).forEach { colorHex ->
                            ColorOption(
                                colorHex = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name, selectedIcon, selectedColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(colorHex)))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Lucide.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, value: String? = null, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Icon(imageVector = Lucide.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}
