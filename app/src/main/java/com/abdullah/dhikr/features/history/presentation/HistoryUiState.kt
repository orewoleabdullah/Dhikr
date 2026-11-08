package com.abdullah.dhikr.features.history.presentation

import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity

/**
 * UI State for the Dhikr History Screen.
 */
data class HistoryUiState(
    val sessionsGroupedByDate: Map<String, List<DhikrSessionEntity>> = emptyMap(),
    val deleteConfirmationSession: DhikrSessionEntity? = null,
    val selectedDetailSession: DhikrSessionEntity? = null,
    val isLoading: Boolean = false
)
