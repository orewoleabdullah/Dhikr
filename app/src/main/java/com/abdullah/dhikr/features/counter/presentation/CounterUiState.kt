package com.abdullah.dhikr.features.counter.presentation

import com.abdullah.dhikr.domain.model.DhikrPreset

/**
 * State representing active session status.
 */
enum class SessionStatus {
    IDLE,
    ACTIVE,
    COMPLETED
}

/**
 * UI State for the Dhikr Counter Screen.
 */
data class CounterUiState(
    val selectedDhikr: DhikrPreset = DhikrPreset.FREE_COUNTER,
    val availableDhikrs: List<DhikrPreset> = DhikrPreset.PRESETS,
    val isCustomDhikr: Boolean = false,
    val isAfterSalahMode: Boolean = false,
    val afterSalahStep: Int = 0,
    val currentCount: Int = 0,
    val target: Int = 33,
    val sessionIncrementCount: Int = 0,
    val sessionStatus: SessionStatus = SessionStatus.IDLE,
    val isTargetReached: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = false,
    val isCompletionFeedbackEnabled: Boolean = true,
    val showResetDialog: Boolean = false,
    val showDhikrSelection: Boolean = false,
    val showTargetSelection: Boolean = false,
    val showCustomTargetDialog: Boolean = false,
    val showCustomDhikrDialog: Boolean = false
) {
    val isUndoAvailable: Boolean
        get() = if (isAfterSalahMode) {
            currentCount > 0 || afterSalahStep > 0
        } else {
            currentCount > 0 && sessionIncrementCount > 0
        }
}

/**
 * Side-effect events for one-shot UI feedback.
 */
sealed interface CounterUiEvent {
    data object TriggerTapHaptic : CounterUiEvent
    data object TriggerCompletionHaptic : CounterUiEvent
    data object TriggerSoundEffect : CounterUiEvent
}
