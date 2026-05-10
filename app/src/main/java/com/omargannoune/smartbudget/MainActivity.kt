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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartBudgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val app = application as SmartBudgetApp
                    val expensesViewModel: ExpensesViewModel = viewModel(
                        factory = AppViewModelProvider.factory(app)
                    )
                    val uiState by expensesViewModel.expensesUiState.collectAsState()
                    ExpensesScreen(
                        uiState = uiState,
                        modifier = Modifier.padding(innerPadding),
                        onAddExpense = expensesViewModel::createExpense
                    )
                }
            }
        }
    }
}
