package com.omargannoune.smartbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.omargannoune.smartbudget.ui.theme.SmartBudgetTheme
import com.omargannoune.smartbudget.ui.AppViewModelProvider
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.omargannoune.smartbudget.ui.expenses.ExpensesScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.omargannoune.smartbudget.ui.budgets.BudgetsScreen
import com.omargannoune.smartbudget.ui.budgets.BudgetsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartBudgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val app = application as SmartBudgetApp
                    val showBudgets = remember { mutableStateOf(false) }
                    val expensesViewModel: ExpensesViewModel = viewModel(
                        factory = AppViewModelProvider.factory(app)
                    )
                    val budgetsViewModel: BudgetsViewModel = viewModel(
                        factory = AppViewModelProvider.factory(app)
                    )
                    val uiState by expensesViewModel.expensesUiState.collectAsState()
                    val budgetsUiState by budgetsViewModel.budgetsUiState.collectAsState()
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            TextButton(onClick = { showBudgets.value = false }) {
                                Text(text = "Expenses")
                            }
                            TextButton(onClick = { showBudgets.value = true }) {
                                Text(text = "Budgets")
                            }
                        }
                        if (showBudgets.value) {
                            BudgetsScreen(uiState = budgetsUiState)
                        } else {
                            ExpensesScreen(
                                uiState = uiState,
                                modifier = Modifier,
                                onAddExpense = expensesViewModel::createExpense,
                                onPreviousMonth = expensesViewModel::goToPreviousMonth,
                                onNextMonth = expensesViewModel::goToNextMonth
                            )
                        }
                    }
                }
            }
        }
    }
}
