package com.omargannoune.smartbudget.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "smartbudget_prefs")

class OnboardingRepository(private val context: Context) {
    data class Profile(val name: String, val currency: String)

    private object Keys {
        val onboardingComplete: Preferences.Key<Boolean> =
            booleanPreferencesKey("onboarding_complete")
        val userName: Preferences.Key<String> = stringPreferencesKey("user_name")
        val currency: Preferences.Key<String> = stringPreferencesKey("currency")
    }

    fun observeOnboardingComplete(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.onboardingComplete] ?: false
        }
    }

    fun observeProfile(): Flow<Profile> {
        return context.dataStore.data.map { prefs ->
            Profile(
                name = prefs[Keys.userName] ?: "",
                currency = prefs[Keys.currency] ?: "MAD"
            )
        }
    }

    suspend fun saveProfile(name: String, currency: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.userName] = name
            prefs[Keys.currency] = currency
        }
    }

    suspend fun setOnboardingComplete(isComplete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.onboardingComplete] = isComplete
        }
    }
}
