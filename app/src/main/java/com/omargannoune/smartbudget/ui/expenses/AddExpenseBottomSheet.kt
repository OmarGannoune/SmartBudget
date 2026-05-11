package com.omargannoune.smartbudget.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.getCategoryColor
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (
        amountMinor: Long,
        date: String,
        categoryId: Long,
        note: String?,
        paymentMethod: String?,
        necessityRating: Int?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("Card") }
    var necessityRating by remember { mutableStateOf(5f) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = categories.firstOrNull { it.isActive }?.id
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Amount Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (amountText.isEmpty()) {
                            Text(
                                "0",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 56.sp
                                )
                            )
                        }
                        BasicTextField(
                            value = amountText,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountText = it },
                            textStyle = MaterialTheme.typography.displayLarge.copy(
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 56.sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.width(IntrinsicSize.Min)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "MAD",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shortcuts
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 50, 100).forEach { value ->
                        Surface(
                            onClick = {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                amountText = (current + value).toInt().toString()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "+$value",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Note Section
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("What was this for? (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary
                ),
                leadingIcon = { Icon(Lucide.FileText, null, modifier = Modifier.size(20.dp)) }
            )

            // Category Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                val activeCategories = categories.filter { it.isActive }
                val rows = activeCategories.chunked(4)
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { category ->
                                val isSelected = selectedCategoryId == category.id
                                val catColor = getCategoryColor(category.color)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCategoryId = category.id }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) catColor else Color.Transparent,
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            // Fill empty slots
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Payment Method
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Payment Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "Card", "Transfer").forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Surface(
                            onClick = { selectedPaymentMethod = method },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                                Text(method, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // Date Selection
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).clickable { showDatePicker = true },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Calendar, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    val dateLabel = if (selectedDate == LocalDate.now()) "Today" else "Date"
                    Text("$dateLabel • ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd"))}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Necessity Slider
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("How necessary was this?", style = MaterialTheme.typography.bodyLarge)
                    Text(necessityRating.toInt().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
                Slider(
                    value = necessityRating,
                    onValueChange = { necessityRating = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.tertiary, activeTrackColor = MaterialTheme.colorScheme.tertiary)
                )
            }

            PrimaryButton(
                text = "Save expense",
                onClick = {
                    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
                    val amountMinor = (amountDouble * 100).toLong()
                    if (amountMinor > 0 && selectedCategoryId != null) {
                        onSave(amountMinor, selectedDate.toString(), selectedCategoryId!!, noteText.ifBlank { null }, selectedPaymentMethod, necessityRating.toInt())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
