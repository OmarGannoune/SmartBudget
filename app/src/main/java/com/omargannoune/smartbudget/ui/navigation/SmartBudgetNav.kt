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
import com.omargannoune.smartbudget.ui.screens.PlaceholderScreen

private object Routes {
    const val Expenses = "expenses"
    const val Budgets = "budgets"
    const val Goals = "goals"
    const val Settings = "settings"
}

private data class BottomItem(
    val route: String,
    val label: String
)

private val bottomItems = listOf(
    BottomItem(Routes.Expenses, "Expenses"),
    BottomItem(Routes.Budgets, "Budgets"),
    BottomItem(Routes.Goals, "Goals"),
    BottomItem(Routes.Settings, "Settings")
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
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.Goals) {
                PlaceholderScreen(
                    title = "Savings goals",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.Settings) {
                PlaceholderScreen(
                    title = "Settings",
                    modifier = Modifier.padding(innerPadding)
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
                icon = { Text(text = item.label.take(1)) },
                label = { Text(text = item.label) }
            )
        }
    }
}
