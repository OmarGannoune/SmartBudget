package com.omargannoune.smartbudget.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.omargannoune.smartbudget.R
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.ui.components.AppTextButton
import com.omargannoune.smartbudget.ui.components.PrimaryButton
import com.omargannoune.smartbudget.ui.components.ScreenTitle
import java.math.BigDecimal
import java.math.RoundingMode

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
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxWidth()
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

@Composable
private fun ProfileScreen(
    name: String,
    currency: String,
    onNameChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OnboardingTextField(
                    value = currency,
                    onValueChange = onCurrencyChange,
                    label = "Currency",
                    placeholder = "MAD"
                )
                Text(
                    text = "Default is MAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalsSetupScreen(
    onAddGoal: (name: String, targetMinor: Long, targetDate: String?) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    OnboardingScreenContainer(
        title = "Create your first goal",
        subtitle = "Small goals add up fast.",
        onPrimaryClick = onContinue,
        primaryText = "Continue",
        onSecondaryClick = onSkip,
        secondaryText = "Skip",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = "Goal name",
                placeholder = "New Car",
                isError = nameError != null,
                errorMessage = nameError
            )
            OnboardingTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = "Target amount",
                placeholder = "0.00",
                isError = amountError != null,
                errorMessage = amountError
            )
            OnboardingTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = "Target date (optional)",
                placeholder = "YYYY-MM-DD"
            )
            PrimaryButton(
                text = "Add goal",
                onClick = {
                    nameError = null
                    amountError = null
                    val amountMinor = parseAmountToMinor(targetAmount)
                    if (goalName.isBlank()) {
                        nameError = "Enter a goal name"
                    }
                    if (amountMinor == null || amountMinor <= 0L) {
                        amountError = "Enter a valid amount"
                    }
                    if (nameError == null && amountError == null) {
                        onAddGoal(
                            goalName.trim(),
                            amountMinor ?: 0L,
                            targetDate.trim().ifBlank { null }
                        )
                        goalName = ""
                        targetAmount = ""
                        targetDate = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
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
