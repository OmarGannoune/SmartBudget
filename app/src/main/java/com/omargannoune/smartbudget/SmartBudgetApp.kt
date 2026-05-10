package com.omargannoune.smartbudget

import android.app.Application
import com.omargannoune.smartbudget.data.AppContainer

class SmartBudgetApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
