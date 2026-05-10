package com.omargannoune.smartbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.omargannoune.smartbudget.ui.theme.SmartBudgetTheme
import com.omargannoune.smartbudget.ui.AppViewModelProvider
import com.omargannoune.smartbudget.ui.expenses.ExpensesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
                    AppPlaceholder(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun AppPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "SmartBudget")
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPlaceholderPreview() {
    SmartBudgetTheme {
        AppPlaceholder()
    }
}
