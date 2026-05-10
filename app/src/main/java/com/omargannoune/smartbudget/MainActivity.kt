package com.omargannoune.smartbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.omargannoune.smartbudget.ui.theme.SmartBudgetTheme
import com.omargannoune.smartbudget.ui.AppViewModelProvider
import com.omargannoune.smartbudget.ui.navigation.SmartBudgetNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartBudgetTheme {
                val app = application as SmartBudgetApp
                val viewModelFactory = AppViewModelProvider.factory(app)
                SmartBudgetNav(viewModelFactory = viewModelFactory)
            }
        }
    }
}
