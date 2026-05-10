package com.omargannoune.smartbudget.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omargannoune.smartbudget.data.local.entity.CategoryEntity
import com.omargannoune.smartbudget.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> = categoryRepository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    data class SettingsUiState(
        val categories: List<CategoryEntity> = emptyList()
    )

    fun createCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.createCategory(name = name, icon = null, color = null)
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(name = newName))
        }
    }

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategoryMoveExpenses(category.id)
        }
    }
}
