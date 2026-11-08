package com.abdullah.dhikr.features.presets.presentation

import com.abdullah.dhikr.domain.model.DhikrItem

enum class PresetFilterTab {
    ALL,
    BUILT_IN,
    CUSTOM
}

/**
 * UI state for the Dhikr Presets Management screen.
 */
data class PresetsUiState(
    val dhikrList: List<DhikrItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilterTab: PresetFilterTab = PresetFilterTab.ALL,
    val activeSelectedTitle: String = "SubhanAllah",
    val isAddEditDialogVisible: Boolean = false,
    val editingDhikr: DhikrItem? = null,
    val deleteConfirmationDhikr: DhikrItem? = null,
    val activeSessionSwitchWarning: DhikrItem? = null
)
