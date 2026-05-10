package com.omargannoune.smartbudget.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omargannoune.smartbudget.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    NavHost(navController = navController, startDestination = OnboardingRoutes.Welcome) {
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
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0B0F14))) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = "Onboarding background gradient",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Illustration image
                Image(
                    painter = painterResource(id = R.drawable.onboarding_illustration),
                    contentDescription = "Savings jar illustration",
                    modifier = Modifier.fillMaxWidth().height(280.dp),
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
                        color = Color(0xFFF2F4F8), // Text Primary from spec
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Plan your month.\nTrack every expense.\nReach your goals.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFA9B1BF), // Text Secondary from spec
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Primary button with Primary Accent color #5DE2C6
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5DE2C6) // Primary Accent from spec
                    )
                ) {
                    Text(
                        text = "Get started",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF0B0F14) // Dark text on light button
                    )
                }
                // Text button for "Skip for now"
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFA9B1BF) // Text Secondary
                    )
                }
            }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenTitle(text = "Make it yours")
            Text(
                text = "Set a name and currency. You can change this later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Your name") },
                singleLine = true
            )
            OutlinedTextField(
                value = currency,
                onValueChange = onCurrencyChange,
                label = { Text("Currency") },
                singleLine = true
            )
            Text(
                text = "Default is MAD",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
            AppTextButton(text = "Skip", onClick = onSkip, modifier = Modifier.fillMaxWidth())
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenTitle(text = "Create your first goal")
            Text(
                text = "Small goals add up fast.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Goal name") },
                isError = nameError != null,
                singleLine = true
            )
            if (nameError != null) {
                Text(
                    text = nameError ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target amount") },
                isError = amountError != null,
                singleLine = true
            )
            if (amountError != null) {
                Text(
                    text = amountError ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = { Text("Target date (optional)") },
                singleLine = true
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
            AppTextButton(text = "Skip", onClick = onSkip, modifier = Modifier.fillMaxWidth())
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenTitle(text = "Pick your categories")
            Text(
                text = "Keep it simple. You can add more anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    modifier = Modifier.weight(1f)
                )
            }
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryPreviewList(categories = categories)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
            AppTextButton(text = "Skip", onClick = onSkip, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CategoryPreviewList(categories: List<CategoryEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it.id }) { category ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenTitle(text = "Set your monthly budget")
            Text(
                text = "Add a total limit and optional category limits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Total monthly budget") },
                isError = amountError != null,
                singleLine = true
            )
            if (amountError != null) {
                Text(
                    text = amountError ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
            AppTextButton(text = "Skip", onClick = onSkip, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DoneScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenTitle(text = "You are all set")
            Text(
                text = "Your budget is ready. Let us track your progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PrimaryButton(text = "Go to Home", onClick = onFinish, modifier = Modifier.fillMaxWidth())
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
