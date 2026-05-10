package com.omargannoune.smartbudget.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import coil.compose.AsyncImage
import com.omargannoune.smartbudget.ui.budgets.BudgetsScreen
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel
import com.omargannoune.smartbudget.ui.expenses.ExpensesScreen
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import com.omargannoune.smartbudget.ui.goals.GoalsScreen
import com.omargannoune.smartbudget.ui.goals.GoalsViewModel
import com.omargannoune.smartbudget.ui.settings.SettingsScreen
import com.omargannoune.smartbudget.ui.settings.SettingsViewModel

private object Routes {
    const val Expenses = "expenses"
    const val Budgets = "budgets"
    const val Goals = "goals"
    const val Settings = "settings"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val iconAssetPath: String
)

private val bottomItems = listOf(
    BottomItem(Routes.Expenses, "Expenses", "file:///android_asset/icons/house.svg"),
    BottomItem(Routes.Budgets, "Budgets", "file:///android_asset/icons/wallet.svg"),
    BottomItem(Routes.Goals, "Goals", "file:///android_asset/icons/target.svg"),
    BottomItem(Routes.Settings, "Settings", "file:///android_asset/icons/gear.svg")
)

@Composable
fun SmartBudgetNav(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Expenses,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.Expenses) {
                val viewModel: ExpensesViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.expensesUiState.collectAsState()
                ExpensesScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onAddExpense = viewModel::createExpense,
                    onPreviousMonth = viewModel::goToPreviousMonth,
                    onNextMonth = viewModel::goToNextMonth
                )
            }
            composable(Routes.Budgets) {
                val viewModel: BudgetsViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.budgetsUiState.collectAsState()
                BudgetsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onSaveMonthlyBudget = viewModel::setMonthlyBudget,
                    onSaveCategoryBudget = viewModel::setCategoryBudget
                )
            }
            composable(Routes.Goals) {
                val viewModel: GoalsViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.goalsUiState.collectAsState()
                GoalsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onAddGoal = viewModel::createGoal,
                    onAddContribution = viewModel::addContribution
                )
            }
            composable(Routes.Settings) {
                val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.settingsUiState.collectAsState()
                SettingsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onCreateCategory = viewModel::createCategory,
                    onRenameCategory = viewModel::renameCategory,
                    onArchiveCategory = viewModel::archiveCategory,
                    onDeleteCategory = viewModel::deleteCategory
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { NavIcon(iconPath = item.iconAssetPath, label = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}

@Composable
private fun NavIcon(iconPath: String, label: String) {
    AsyncImage(
        model = iconPath,
        contentDescription = label,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .size(24.dp)
    )
}
