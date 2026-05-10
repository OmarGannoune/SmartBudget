package com.omargannoune.smartbudget.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
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
                onContinue = { navController.navigate(OnboardingRoutes.Categories) },
                onSkip = { navController.navigate(OnboardingRoutes.Categories) }
            )
        }
        composable(OnboardingRoutes.Categories) {
            CategoriesSetupScreen(
                categories = uiState.categories,
                onAddCategory = viewModel::createCategory,
                onContinue = { navController.navigate(OnboardingRoutes.Budget) },
                onSkip = { navController.navigate(OnboardingRoutes.Budget) }
            )
        }
        composable(OnboardingRoutes.Budget) {
            BudgetSetupScreen(
                onSaveBudget = viewModel::setMonthlyBudget,
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_illustration),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SMARTBUDGET",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Left
                    )
                    Text(
                        text = "Plan your month.\nTrack every expense.\nReach your goals.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Left
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
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    OnboardingScreenContainer(
        title = "Create your goals",
        subtitle = "Small goals add up fast. Add as many as you want.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Lucide.Plus, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add a new goal", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            }

            if (goals.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(goals) { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(goal.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = formatAmount(goal.targetAmountMinor),
                                    style = MaterialTheme.typography.bodyMedium,
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

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, date ->
                onAddGoal(name, amount, date)
                showAddDialog = false
            }
        )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountMinor = parseAmountToMinor(amount)
                    if (name.isNotBlank() && amountMinor != null) {
                        onConfirm(name, amountMinor, formattedDate)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Add Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

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
    onAddCategory: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    OnboardingScreenContainer(
        title = "Pick your categories",
        subtitle = "Keep it simple. You can add more anytime.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = "Category name",
                placeholder = "Groceries",
                isError = errorText != null,
                errorMessage = errorText
            )
            PrimaryButton(
                text = "Add category",
                onClick = {
                    errorText = null
                    val trimmed = categoryName.trim()
                    if (trimmed.isBlank()) {
                        errorText = "Enter a category name"
                    } else if (categories.any { it.name.equals(trimmed, ignoreCase = true) }) {
                        errorText = "Category already exists"
                    } else {
                        onAddCategory(trimmed)
                        categoryName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (categories.isNotEmpty()) {
                CategoryPreviewList(categories = categories)
            }
        }
    }
}

@Composable
private fun CategoryPreviewList(categories: List<CategoryEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSetupScreen(
    onSaveBudget: (Long) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    OnboardingScreenContainer(
        title = "Set your monthly budget",
        subtitle = "Add a total limit and optional category limits.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "Total monthly budget",
                placeholder = "0.00",
                isError = amountError != null,
                errorMessage = amountError
            )
            PrimaryButton(
                text = "Set budget",
                onClick = {
                    amountError = null
                    val amountMinor = parseAmountToMinor(amountText)
                    if (amountMinor == null || amountMinor <= 0L) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSaveBudget(amountMinor)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
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
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ScreenTitle(text = "You are all set")
                Text(
                    text = "Your budget is ready. Let us track your progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Image(
                painter = painterResource(id = R.drawable.onboarding_illustration),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )
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
