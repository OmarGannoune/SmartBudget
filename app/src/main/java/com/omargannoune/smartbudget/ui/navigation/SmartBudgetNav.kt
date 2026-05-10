package com.omargannoune.smartbudget.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.composables.icons.lucide.*
import com.omargannoune.smartbudget.ui.budgets.BudgetsScreen
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel
import com.omargannoune.smartbudget.ui.expenses.ExpensesScreen
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import com.omargannoune.smartbudget.ui.goals.GoalsScreen
import com.omargannoune.smartbudget.ui.goals.GoalsViewModel
import com.omargannoune.smartbudget.ui.home.HomeScreen
import com.omargannoune.smartbudget.ui.home.HomeViewModel
import com.omargannoune.smartbudget.ui.recurring.RecurringScreen
import com.omargannoune.smartbudget.ui.recurring.RecurringViewModel
import com.omargannoune.smartbudget.ui.settings.SettingsScreen
import com.omargannoune.smartbudget.ui.settings.SettingsViewModel

private object Routes {
    const val Home = "home"
    const val ExpensesWithAdd = "expenses?openAdd={openAdd}"
    const val Budgets = "budgets"
    const val Goals = "goals"
    const val Settings = "settings"
    const val Recurring = "recurring"

    fun expensesRoute(openAdd: Boolean): String = "expenses?openAdd=$openAdd"
}

private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val isAdd: Boolean = false
)

@Composable
fun SmartBudgetNav(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { CustomBottomBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.Home) {
                val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.uiState.collectAsState()
                HomeScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onAddExpense = { navController.navigate(Routes.expensesRoute(openAdd = true)) }
                )
            }
            composable(
                route = Routes.ExpensesWithAdd,
                arguments = listOf(navArgument("openAdd") {
                    type = NavType.BoolType
                    defaultValue = false
                })
            ) {
                val openAdd = it.arguments?.getBoolean("openAdd") ?: false
                val viewModel: ExpensesViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.expensesUiState.collectAsState()
                ExpensesScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    openAdd = openAdd,
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
                    onClearData = viewModel::clearAllData,
                    onUpdateCurrency = viewModel::updateCurrency,
                    onExportCsv = viewModel::exportCsv,
                    onCreateCategory = viewModel::createCategory,
                    onRenameCategory = viewModel::renameCategory,
                    onArchiveCategory = viewModel::archiveCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onClearExportMessage = viewModel::clearExportMessage
                )
            }
            composable(Routes.Recurring) {
                val viewModel: RecurringViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.uiState.collectAsState()
                RecurringScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onBack = { navController.popBackStack() },
                    onCreateRule = viewModel::createRule,
                    onToggleActive = viewModel::toggleActive,
                    onDeleteRule = viewModel::deleteRule
                )
            }
        }
    }
}

@Composable
private fun CustomBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem(Routes.Home, Lucide.House, "Home"),
        BottomNavItem(Routes.Budgets, Lucide.Wallet, "Budgets"),
        BottomNavItem("add", Lucide.Plus, "Add", isAdd = true),
        BottomNavItem(Routes.Goals, Lucide.Target, "Goals"),
        BottomNavItem(Routes.Settings, Lucide.Settings, "Settings")
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                if (item.isAdd) {
                    Box(
                        modifier = Modifier
                            .size(64.dp, 52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                            .clickable {
                                navController.navigate(Routes.expensesRoute(openAdd = true))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    val isSelected = currentRoute == item.route
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
