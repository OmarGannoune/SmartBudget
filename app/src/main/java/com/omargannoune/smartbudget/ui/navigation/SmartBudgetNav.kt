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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Icon
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.omargannoune.smartbudget.ui.budgets.BudgetsScreen
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel
import com.omargannoune.smartbudget.ui.expenses.ExpensesScreen
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import com.omargannoune.smartbudget.ui.goals.GoalsScreen
import com.omargannoune.smartbudget.ui.goals.GoalsViewModel
import com.omargannoune.smartbudget.ui.settings.SettingsScreen
import com.omargannoune.smartbudget.ui.settings.SettingsViewModel
import com.phosphoricons.phosphor.PhosphorIcons
import com.phosphoricons.phosphor.regular.Gear
import com.phosphoricons.phosphor.regular.House
import com.phosphoricons.phosphor.regular.Target
import com.phosphoricons.phosphor.regular.Wallet

private object Routes {
    const val Expenses = "expenses"
    const val Budgets = "budgets"
    const val Goals = "goals"
    const val Settings = "settings"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.Expenses, "Expenses", PhosphorIcons.Regular.House),
    BottomItem(Routes.Budgets, "Budgets", PhosphorIcons.Regular.Wallet),
    BottomItem(Routes.Goals, "Goals", PhosphorIcons.Regular.Target),
    BottomItem(Routes.Settings, "Settings", PhosphorIcons.Regular.Gear)
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
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}
