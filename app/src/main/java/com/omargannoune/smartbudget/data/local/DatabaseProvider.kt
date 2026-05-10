package com.omargannoune.smartbudget.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private const val DATABASE_NAME = "smartbudget.db"

    fun provideDatabase(context: Context): SmartBudgetDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SmartBudgetDatabase::class.java,
            DATABASE_NAME
        ).build()
    }
}
