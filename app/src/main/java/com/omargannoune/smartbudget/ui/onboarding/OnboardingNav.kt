package com.omargannoune.smartbudget.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.R
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.local.entity.SavingsGoalEntity
import com.omargannoune.smartbudget.ui.components.AppTextButton
import com.omargannoune.smartbudget.ui.components.CategoryDefaults
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import com.omargannoune.smartbudget.ui.components.getCategoryIcon
import com.omargannoune.smartbudget.ui.components.getCategoryColor
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private object OnboardingRoutes {
    const val Welcome = "welcome"
    const val Profile = "profile"
    const val Goals = "goals"
    const val Categories = "categories"
    const val Budget = "budget"
    const val Done = "done"
}

@Composable
fun OnboardingNav(
    viewModelFactory: ViewModelProvider.Factory,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = OnboardingRoutes.Welcome,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(OnboardingRoutes.Welcome) {
            WelcomeScreen(
                onStart = { navController.navigate(OnboardingRoutes.Profile) },
                onSkip = {
                    viewModel.completeOnboarding()
                    onFinish()
                }
            )
        }
        composable(OnboardingRoutes.Profile) {
            ProfileScreen(
                name = uiState.name,
                currency = uiState.currency,
                onNameChange = viewModel::updateName,
                onCurrencyChange = viewModel::updateCurrency,
                onContinue = {
                    viewModel.saveProfile()
                    navController.navigate(OnboardingRoutes.Goals)
                },
                onSkip = { navController.navigate(OnboardingRoutes.Goals) }
            )
        }
        composable(OnboardingRoutes.Goals) {
            GoalsSetupScreen(
                goals = uiState.goals,
                onAddGoal = viewModel::createGoal,
                onUpdateGoal = { id, name, targetMinor, targetDate ->
                    viewModel.updateGoal(id, name, targetMinor, targetDate)
                },
                onDeleteGoal = { goalId ->
                    viewModel.deleteGoal(goalId)
                },
                onContinue = { navController.navigate(OnboardingRoutes.Categories) },
                onSkip = { navController.navigate(OnboardingRoutes.Categories) }
            )
        }
        composable(OnboardingRoutes.Categories) {
            CategoriesSetupScreen(
                categories = uiState.categories,
                onAddCategory = viewModel::createCategory,
                onUpdateCategory = { id, name, icon, color ->
                    viewModel.updateCategory(id, name, icon, color)
                },
                onDeleteCategory = { categoryId ->
                    viewModel.deleteCategory(categoryId)
                },
                onContinue = { navController.navigate(OnboardingRoutes.Budget) },
                onSkip = { navController.navigate(OnboardingRoutes.Budget) }
            )
        }
        composable(OnboardingRoutes.Budget) {
            BudgetSetupScreen(
                categories = uiState.categories,
                onSaveBudget = viewModel::setMonthlyBudget,
                onSaveCategoryBudgets = viewModel::setCategoryBudgets,
                onContinue = { navController.navigate(OnboardingRoutes.Done) },
                onSkip = { navController.navigate(OnboardingRoutes.Done) }
            )
        }
        composable(OnboardingRoutes.Done) {
            DoneScreen(
                onFinish = {
                    viewModel.completeOnboarding()
                    onFinish()
                }
            )
        }
    }
}

@Composable
private fun WelcomeScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_illustration),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SMARTBUDGET",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Plan your month.\nTrack every expense.\nReach your goals.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    text = "Get started",
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextButton(
                    text = "Skip for now",
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreenContainer(
    title: String,
    subtitle: String,
    onPrimaryClick: () -> Unit,
    primaryText: String,
    onSecondaryClick: () -> Unit,
    secondaryText: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScreenTitle(text = title)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton(text = primaryText, onClick = onPrimaryClick, modifier = Modifier.fillMaxWidth())
            AppTextButton(text = secondaryText, onClick = onSecondaryClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    name: String,
    currency: String,
    onNameChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("MAD", "USD", "EUR", "GBP", "JPY")

    OnboardingScreenContainer(
        title = "Make it yours",
        subtitle = "Set a name and currency. You can change this later.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Your name",
                placeholder = "John Doe"
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onCurrencyChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsSetupScreen(
    goals: List<SavingsGoalEntity>,
    onAddGoal: (name: String, targetMinor: Long, targetDate: String?) -> Unit,
    onUpdateGoal: (id: Long, name: String, targetMinor: Long, targetDate: String?) -> Unit,
    onDeleteGoal: (goalId: Long) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    OnboardingScreenContainer(
        title = "Create your goals",
        subtitle = "Small goals add up fast. Add as many as you want.",
        onPrimaryClick = onContinue,
        primaryText = if (goals.isNotEmpty()) "Continue (${goals.size} added)" else "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { 
                    editingGoal = null
                    showAddDialog = true 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Icon(Lucide.Plus, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add a new goal", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            }

            if (goals.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        goal.targetDate?.let {
                                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { editingGoal = goal; showAddDialog = true },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Lucide.Pencil,
                                                null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteGoal(goal.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Lucide.Trash2,
                                                null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val progress = if (goal.targetAmountMinor > 0L) {
                                    (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatAmount(goal.currentAmountMinor),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatAmount(goal.targetAmountMinor),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalBottomSheetDialog(
            existingGoal = editingGoal,
            onDismiss = { 
                showAddDialog = false
                editingGoal = null
            },
            onConfirm = { name, amount, date ->
                if (editingGoal != null) {
                    onUpdateGoal(editingGoal!!.id, name, amount, date)
                } else {
                    onAddGoal(name, amount, date)
                }
                showAddDialog = false
                editingGoal = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalBottomSheetDialog(
    existingGoal: SavingsGoalEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String?) -> Unit
) {
    var nameText by remember { mutableStateOf(existingGoal?.name ?: "") }
    var amountText by remember {
        mutableStateOf(existingGoal?.let { (it.targetAmountMinor / 100).toString() } ?: "")
    }
    var selectedDate by remember { mutableStateOf(existingGoal?.targetDate?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingGoal == null) "Create Goal" else "Edit Goal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Goal Name
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    placeholder = { Text("Goal name") },
                    label = { Text("Goal name") },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Amount Section
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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

                    // Amount Shortcuts
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(100, 500, 1000, 5000).forEach { value ->
                            Surface(
                                onClick = {
                                    val current = amountText.toDoubleOrNull() ?: 0.0
                                    amountText = (current + value).toInt().toString()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
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

                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Target Date
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Lucide.Calendar,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Target date • ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd"))}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
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
                                onConfirm(nameText.trim(), amountMinor ?: 0L, selectedDate.toString())
                            }
                        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()

    val formattedDate = selectedDate?.let {
        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("New Goal", style = MaterialTheme.typography.titleLarge)
                
                OnboardingTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Goal Name",
                    placeholder = "e.g. Dream Vacation"
                )
                OnboardingTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Target Amount",
                    placeholder = "0.00"
                )
                
                OutlinedTextField(
                    value = formattedDate ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Date (Optional)") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Lucide.Calendar, null, modifier = Modifier.size(20.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amountMinor = parseAmountToMinor(amount)
                            if (name.isNotBlank() && amountMinor != null) {
                                onConfirm(name, amountMinor, formattedDate)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Add", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoriesSetupScreen(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String?, String?) -> Unit,
    onUpdateCategory: (id: Long, name: String, icon: String?, color: String?) -> Unit,
    onDeleteCategory: (categoryId: Long) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    OnboardingScreenContainer(
        title = "Pick your categories",
        subtitle = "Keep it simple. You can add more anytime.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { 
                    editingCategory = null
                    showAddDialog = true 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Icon(Lucide.Plus, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add a new category", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            }

            if (categories.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val catColor = getCategoryColor(category.color)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(catColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = catColor
                                        )
                                    }
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { editingCategory = category; showAddDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Lucide.Pencil,
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCategory(category.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Lucide.Trash2,
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            existingCategory = editingCategory,
            onDismiss = { 
                showAddDialog = false
                editingCategory = null
            },
            onConfirm = { name, icon, color ->
                if (editingCategory != null) {
                    onUpdateCategory(editingCategory!!.id, name, icon, color)
                } else {
                    onAddCategory(name, icon, color)
                }
                showAddDialog = false
                editingCategory = null
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(
    existingCategory: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var selectedIcon by remember { mutableStateOf<String?>(existingCategory?.icon ?: CategoryDefaults.Icons.first()) }
    var selectedColor by remember { mutableStateOf<String?>(existingCategory?.color ?: CategoryDefaults.Colors.first()) }

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
                Text(text = if (existingCategory == null) "New Category" else "Edit Category", style = MaterialTheme.typography.titleLarge)

                OnboardingTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Category Name",
                    placeholder = "e.g. Groceries"
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
                        onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon, selectedColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Add", color = MaterialTheme.colorScheme.background)
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
private fun CategoryPreviewList(categories: List<CategoryEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 300.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val catColor = getCategoryColor(category.color)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = catColor
                        )
                    }
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSetupScreen(
    categories: List<CategoryEntity>,
    onSaveBudget: (Long) -> Unit,
    onSaveCategoryBudgets: (Map<Long, Long>) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryBudgets by remember { mutableStateOf(categories.associate { it.id to "" }) }

    OnboardingScreenContainer(
        title = "Set your monthly budget",
        subtitle = "Add a total limit and optional category limits.",
        onPrimaryClick = {
            amountError = null
            val amountMinor = parseAmountToMinor(amountText)
            if (amountMinor == null || amountMinor <= 0L) {
                amountError = "Enter a valid amount"
            } else {
                onSaveBudget(amountMinor)
                // Save category budgets
                val categoryBudgetMap = categoryBudgets.mapValues { (_, value) ->
                    parseAmountToMinor(value) ?: 0L
                }
                onSaveCategoryBudgets(categoryBudgetMap)
                onContinue()
            }
        },
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Total Monthly Budget
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total Monthly Budget", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                // Amount Input (matching goal creation style)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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

                    // Amount Shortcuts
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(1000, 5000, 10000, 50000).forEach { value ->
                            Surface(
                                onClick = {
                                    val current = amountText.toDoubleOrNull() ?: 0.0
                                    amountText = (current + value).toInt().toString()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
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

                if (amountError != null) {
                    Text(
                        text = amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Category Budgets
            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category Budgets (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 250.dp)) {
                        items(categories, key = { it.id }) { category ->
                            CategoryBudgetField(
                                category = category,
                                budgetAmount = categoryBudgets[category.id] ?: "",
                                onBudgetChange = { newAmount ->
                                    categoryBudgets = categoryBudgets.toMutableMap().apply {
                                        this[category.id] = newAmount
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetField(
    category: CategoryEntity,
    budgetAmount: String,
    onBudgetChange: (String) -> Unit
) {
    val catColor = getCategoryColor(category.color)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category Icon and Name
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = catColor
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Budget Input
        OutlinedTextField(
            value = budgetAmount,
            onValueChange = onBudgetChange,
            modifier = Modifier.width(100.dp),
            placeholder = { Text("0.00", style = MaterialTheme.typography.bodySmall) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun DoneScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ScreenTitle(text = "You are all set")
                Text(
                    text = "Your budget is ready. Let us track your progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            // Check Icon
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.CircleCheck,
                    contentDescription = "Success",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        PrimaryButton(text = "Go to Home", onClick = onFinish, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.tertiary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun parseAmountToMinor(amountText: String): Long? {
    val normalized = amountText.trim().replace(',', '.')
    if (normalized.isBlank()) return null
    return runCatching {
        val decimal = BigDecimal(normalized)
        decimal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}

private fun formatAmount(amountMinor: Long): String {
    val major = amountMinor / 100
    val minor = kotlin.math.abs(amountMinor % 100)
    return "$major.${minor.toString().padStart(2, '0')}"
}
