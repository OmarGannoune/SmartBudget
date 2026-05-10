package com.omargannoune.smartbudget.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModelProvider
import com.omargannoune.smartbudget.data.preferences.OnboardingRepository
import com.omargannoune.smartbudget.ui.onboarding.OnboardingNav

@Composable
fun SmartBudgetRootNav(
    viewModelFactory: ViewModelProvider.Factory,
    onboardingRepository: OnboardingRepository
) {
    val isComplete by produceState<Boolean?>(initialValue = null, onboardingRepository) {
        onboardingRepository.observeOnboardingComplete().collect { value = it }
    }

    when (isComplete) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        true -> SmartBudgetNav(viewModelFactory = viewModelFactory)
        false -> OnboardingNav(
            viewModelFactory = viewModelFactory,
            onFinish = {}
        )
    }
}
