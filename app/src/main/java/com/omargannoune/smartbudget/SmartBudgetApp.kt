package com.omargannoune.smartbudget

import android.app.Application
import com.omargannoune.smartbudget.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartBudgetApp : Application() {
    lateinit var container: AppContainer
        private set
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.categoryRepository.ensureDefaultCategories()
            container.recurringRepository.generateDueExpenses()
        }
    }
}
