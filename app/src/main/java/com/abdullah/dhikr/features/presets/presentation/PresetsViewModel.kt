package com.abdullah.dhikr.features.presets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.domain.repository.DhikrRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PresetsViewModel(
    private val dhikrRepository: DhikrRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(PresetFilterTab.ALL)
    private val _uiState = MutableStateFlow(PresetsUiState())

    val uiState: StateFlow<PresetsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dhikrRepository.seedBuiltInPresetsIfEmpty()
        }

        viewModelScope.launch {
            combine(
                userPreferencesRepository.counterPreferences,
                _searchQuery,
                _selectedTab,
                _searchQuery.flatMapLatest { query ->
                    dhikrRepository.searchItemsStream(query)
                }
            ) { counterPrefs, query, tab, items ->
                val filteredItems = items.filter { item ->
                    when (tab) {
                        PresetFilterTab.ALL -> true
                        PresetFilterTab.BUILT_IN -> !item.isCustom
                        PresetFilterTab.CUSTOM -> item.isCustom
                    }
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        dhikrList = filteredItems,
                        searchQuery = query,
                        selectedFilterTab = tab,
                        activeSelectedTitle = counterPrefs.dhikrTitle
                    )
                }
            }.collect {}
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectFilterTab(tab: PresetFilterTab) {
        _selectedTab.value = tab
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                isAddEditDialogVisible = true,
                editingDhikr = null
            )
        }
    }

    fun openEditDialog(dhikr: DhikrItem) {
        // Built-in presets cannot be edited
        if (!dhikr.isCustom) return
        _uiState.update {
            it.copy(
                isAddEditDialogVisible = true,
                editingDhikr = dhikr
            )
        }
    }

    fun closeAddEditDialog() {
        _uiState.update {
            it.copy(
                isAddEditDialogVisible = false,
                editingDhikr = null
            )
        }
    }

    fun saveDhikr(
        title: String,
        arabic: String,
        translation: String,
        description: String?,
        defaultTarget: Int
    ): Boolean {
        if (title.isBlank() || defaultTarget <= 0) return false

        val currentEditing = _uiState.value.editingDhikr
        viewModelScope.launch {
            if (currentEditing != null) {
                dhikrRepository.updateItem(
                    currentEditing.copy(
                        title = title.trim(),
                        arabicText = arabic.trim(),
                        translation = translation.trim(),
                        description = description?.trim(),
                        defaultTarget = defaultTarget,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                dhikrRepository.insertItem(
                    DhikrItem(
                        title = title.trim(),
                        arabicText = arabic.trim(),
                        translation = translation.trim(),
                        description = description?.trim(),
                        defaultTarget = defaultTarget,
                        isCustom = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
        closeAddEditDialog()
        return true
    }

    fun requestDeleteDhikr(dhikr: DhikrItem) {
        // Built-in presets cannot be deleted
        if (!dhikr.isCustom) return
        _uiState.update { it.copy(deleteConfirmationDhikr = dhikr) }
    }

    fun cancelDeleteDhikr() {
        _uiState.update { it.copy(deleteConfirmationDhikr = null) }
    }

    fun confirmDeleteDhikr() {
        val dhikrToDelete = _uiState.value.deleteConfirmationDhikr ?: return
        viewModelScope.launch {
            dhikrRepository.deleteItem(dhikrToDelete)
        }
        cancelDeleteDhikr()
    }

    fun selectDhikr(dhikr: DhikrItem) {
        viewModelScope.launch {
            val current = userPreferencesRepository.counterPreferences.first()
            userPreferencesRepository.saveCounterPreferences(
                current.copy(
                    dhikrTitle = dhikr.title,
                    dhikrArabic = dhikr.arabicText,
                    dhikrTranslation = dhikr.translation,
                    target = dhikr.defaultTarget,
                    isCustomDhikr = dhikr.isCustom
                )
            )
        }
    }
}
