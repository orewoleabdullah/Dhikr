package com.abdullah.dhikr.features.counter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.data.preferences.CounterPreferences
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.domain.model.DhikrPreset
import com.abdullah.dhikr.domain.repository.DhikrRepository
import com.abdullah.dhikr.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CounterViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionRepository: SessionRepository? = null,
    private val dhikrRepository: DhikrRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CounterUiEvent>(extraBufferCapacity = 10)
    val uiEvent: SharedFlow<CounterUiEvent> = _uiEvent.asSharedFlow()

    private var hasTriggeredCompletionHaptic = false
    private var activeSessionId: Long? = null
    private var isInitialized = false

    init {
        // Collect available Dhikrs from database repository
        viewModelScope.launch {
            dhikrRepository?.seedBuiltInPresetsIfEmpty()
            dhikrRepository?.getAllItemsStream()?.collect { items ->
                val presetList = items.map { item ->
                    val isAfterSalah = item.title.contains("After-Salah", ignoreCase = true) || item.id == 101
                    val isQuickCounter = item.title.contains("Quick Counter", ignoreCase = true) || item.id == 100
                    DhikrPreset(
                        id = if (isAfterSalah) DhikrPreset.AFTER_SALAH_PRESET.id else if (isQuickCounter) DhikrPreset.FREE_COUNTER.id else item.id.toString(),
                        title = item.title,
                        arabicText = item.arabicText,
                        translation = item.translation,
                        description = item.description,
                        defaultTarget = item.defaultTarget,
                        isCustom = item.isCustom,
                        isAfterSalahChain = isAfterSalah
                    )
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        availableDhikrs = if (presetList.isNotEmpty()) presetList else DhikrPreset.PRESETS
                    )
                }
            }
        }

        // Load settings toggles continuously
        viewModelScope.launch {
            combine(
                userPreferencesRepository.isVibrationEnabled,
                userPreferencesRepository.isSoundEnabled,
                userPreferencesRepository.isCompletionFeedbackEnabled
            ) { isVibeEnabled, isSoundEnabled, isCompletionEnabled ->
                Triple(isVibeEnabled, isSoundEnabled, isCompletionEnabled)
            }.collect { (isVibeEnabled, isSoundEnabled, isCompletionEnabled) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isVibrationEnabled = isVibeEnabled,
                        isSoundEnabled = isSoundEnabled,
                        isCompletionFeedbackEnabled = isCompletionEnabled
                    )
                }
            }
        }

        // Initialize state ONCE from saved preferences or database active session
        viewModelScope.launch {
            if (!isInitialized) {
                val dbActive = sessionRepository?.getActiveSession()
                if (dbActive != null) {
                    activeSessionId = dbActive.id
                    val matchedPreset = DhikrPreset.PRESETS.find { it.title == dbActive.dhikrNameSnapshot }
                        ?: DhikrPreset(
                            id = "custom_${dbActive.id}",
                            title = dbActive.dhikrNameSnapshot,
                            arabicText = "",
                            translation = "",
                            defaultTarget = dbActive.target
                        )
                    val status = when (dbActive.status) {
                        DhikrSessionEntity.SESSION_STATUS_COMPLETED -> SessionStatus.COMPLETED
                        DhikrSessionEntity.SESSION_STATUS_PAUSED -> SessionStatus.IDLE
                        else -> if (dbActive.count >= dbActive.target) SessionStatus.COMPLETED else SessionStatus.ACTIVE
                    }
                    val isReached = dbActive.count >= dbActive.target

                    if (isReached) {
                        hasTriggeredCompletionHaptic = true
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedDhikr = matchedPreset,
                            currentCount = dbActive.count,
                            target = dbActive.target,
                            sessionIncrementCount = dbActive.count,
                            sessionStatus = status,
                            isTargetReached = isReached
                        )
                    }
                } else {
                    val prefs = userPreferencesRepository.counterPreferences.first()
                    val matchedPreset = DhikrPreset.PRESETS.find { it.title == prefs.dhikrTitle }
                        ?: DhikrPreset(
                            id = "custom",
                            title = prefs.dhikrTitle,
                            arabicText = prefs.dhikrArabic,
                            translation = prefs.dhikrTranslation,
                            defaultTarget = prefs.target
                        )
                    val status = computeSessionStatus(prefs.count, prefs.target)
                    val isReached = prefs.count >= prefs.target

                    if (isReached) {
                        hasTriggeredCompletionHaptic = true
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedDhikr = matchedPreset,
                            isCustomDhikr = prefs.isCustomDhikr,
                            currentCount = prefs.count,
                            target = prefs.target,
                            sessionIncrementCount = prefs.sessionIncrementCount,
                            sessionStatus = status,
                            isTargetReached = isReached
                        )
                    }
                }
                isInitialized = true
            }
        }
    }

    fun incrementCount() {
        _uiState.update { state ->
            if (state.currentCount >= Int.MAX_VALUE) return@update state

            val newCount = state.currentCount + 1
            val newIncrements = state.sessionIncrementCount + 1

            if (state.isVibrationEnabled) {
                _uiEvent.tryEmit(CounterUiEvent.TriggerTapHaptic)
            }

            if (state.isSoundEnabled) {
                _uiEvent.tryEmit(CounterUiEvent.TriggerSoundEffect)
            }

            if (state.isAfterSalahMode) {
                val steps = com.abdullah.dhikr.domain.model.AfterSalahChain.STEPS
                val currentStepIndex = state.afterSalahStep
                val currentStep = steps[currentStepIndex]

                if (newCount >= currentStep.target) {
                    if (currentStepIndex < steps.lastIndex) {
                        val nextStepIndex = currentStepIndex + 1
                        val nextStep = steps[nextStepIndex]

                        if (state.isCompletionFeedbackEnabled && state.isVibrationEnabled) {
                            _uiEvent.tryEmit(CounterUiEvent.TriggerCompletionHaptic)
                        }

                        val nextPreset = DhikrPreset(
                            id = DhikrPreset.AFTER_SALAH_PRESET.id,
                            title = "After-Salah: ${nextStep.title}",
                            arabicText = nextStep.arabicText,
                            translation = "${nextStep.translation} (Step ${nextStepIndex + 1} of 3)",
                            defaultTarget = nextStep.target,
                            isAfterSalahChain = true
                        )

                        val updatedState = state.copy(
                            selectedDhikr = nextPreset,
                            afterSalahStep = nextStepIndex,
                            currentCount = 0,
                            target = nextStep.target,
                            sessionIncrementCount = newIncrements,
                            sessionStatus = SessionStatus.ACTIVE,
                            isTargetReached = false
                        )
                        persistState(updatedState)
                        syncToDatabaseSessionIncrement()
                        updatedState
                    } else {
                        if (!hasTriggeredCompletionHaptic && state.isCompletionFeedbackEnabled) {
                            hasTriggeredCompletionHaptic = true
                            if (state.isVibrationEnabled) {
                                _uiEvent.tryEmit(CounterUiEvent.TriggerCompletionHaptic)
                            }
                        }

                        viewModelScope.launch {
                            activeSessionId?.let { id ->
                                sessionRepository?.completeSession(id)
                            }
                            activeSessionId = null
                        }

                        val freePreset = DhikrPreset.FREE_COUNTER
                        val newTarget = freePreset.defaultTarget
                        val status = computeSessionStatus(0, newTarget)

                        val updatedState = state.copy(
                            selectedDhikr = freePreset,
                            isCustomDhikr = false,
                            isAfterSalahMode = false,
                            afterSalahStep = 0,
                            currentCount = 0,
                            sessionIncrementCount = 0,
                            target = newTarget,
                            sessionStatus = status,
                            isTargetReached = false
                        )
                        persistState(updatedState)
                        syncToDatabaseSessionDhikrChange(freePreset)
                        updatedState
                    }
                } else {
                    val updatedState = state.copy(
                        currentCount = newCount,
                        sessionIncrementCount = newIncrements,
                        sessionStatus = SessionStatus.ACTIVE,
                        isTargetReached = false
                    )
                    persistState(updatedState)
                    syncToDatabaseSessionIncrement()
                    updatedState
                }
            } else {
                val isReached = newCount >= state.target
                val newStatus = computeSessionStatus(newCount, state.target)

                if (isReached && !hasTriggeredCompletionHaptic && state.isCompletionFeedbackEnabled) {
                    hasTriggeredCompletionHaptic = true
                    if (state.isVibrationEnabled) {
                        _uiEvent.tryEmit(CounterUiEvent.TriggerCompletionHaptic)
                    }
                }

                val updatedState = state.copy(
                    currentCount = newCount,
                    sessionIncrementCount = newIncrements,
                    sessionStatus = newStatus,
                    isTargetReached = isReached
                )
                persistState(updatedState)
                syncToDatabaseSessionIncrement()
                updatedState
            }
        }
    }

    fun undoCount() {
        _uiState.update { state ->
            if (state.isAfterSalahMode) {
                if (state.currentCount > 0) {
                    val newCount = state.currentCount - 1
                    val newIncrements = (state.sessionIncrementCount - 1).coerceAtLeast(0)
                    val updatedState = state.copy(
                        currentCount = newCount,
                        sessionIncrementCount = newIncrements,
                        sessionStatus = SessionStatus.ACTIVE,
                        isTargetReached = false
                    )
                    persistState(updatedState)
                    syncToDatabaseSessionUndo()
                    updatedState
                } else if (state.afterSalahStep > 0) {
                    val steps = com.abdullah.dhikr.domain.model.AfterSalahChain.STEPS
                    val prevStepIndex = state.afterSalahStep - 1
                    val prevStep = steps[prevStepIndex]
                    val prevPreset = DhikrPreset(
                        id = DhikrPreset.AFTER_SALAH_PRESET.id,
                        title = "After-Salah: ${prevStep.title}",
                        arabicText = prevStep.arabicText,
                        translation = "${prevStep.translation} (Step ${prevStepIndex + 1} of 3)",
                        defaultTarget = prevStep.target,
                        isAfterSalahChain = true
                    )
                    val newIncrements = (state.sessionIncrementCount - 1).coerceAtLeast(0)
                    val updatedState = state.copy(
                        selectedDhikr = prevPreset,
                        afterSalahStep = prevStepIndex,
                        currentCount = prevStep.target - 1,
                        target = prevStep.target,
                        sessionIncrementCount = newIncrements,
                        sessionStatus = SessionStatus.ACTIVE,
                        isTargetReached = false
                    )
                    persistState(updatedState)
                    syncToDatabaseSessionUndo()
                    updatedState
                } else {
                    state
                }
            } else {
                if (state.currentCount <= 0 || state.sessionIncrementCount <= 0) return@update state

                val newCount = state.currentCount - 1
                val newIncrements = state.sessionIncrementCount - 1
                val isReached = newCount >= state.target
                val newStatus = computeSessionStatus(newCount, state.target)

                if (newCount < state.target) {
                    hasTriggeredCompletionHaptic = false
                }

                val updatedState = state.copy(
                    currentCount = newCount,
                    sessionIncrementCount = newIncrements,
                    sessionStatus = newStatus,
                    isTargetReached = isReached
                )
                persistState(updatedState)
                syncToDatabaseSessionUndo()
                updatedState
            }
        }
    }

    fun showResetDialog(show: Boolean) {
        _uiState.update { it.copy(showResetDialog = show) }
    }

    fun resetCount() {
        hasTriggeredCompletionHaptic = false
        val currentState = _uiState.value
        syncToDatabaseSessionReset(currentState.currentCount, currentState.target)

        _uiState.update { state ->
            if (state.isAfterSalahMode) {
                val step0 = com.abdullah.dhikr.domain.model.AfterSalahChain.STEPS[0]
                val initialPreset = DhikrPreset(
                    id = DhikrPreset.AFTER_SALAH_PRESET.id,
                    title = "After-Salah: ${step0.title}",
                    arabicText = step0.arabicText,
                    translation = "${step0.translation} (Step 1 of 3)",
                    defaultTarget = step0.target,
                    isAfterSalahChain = true
                )
                val updatedState = state.copy(
                    selectedDhikr = initialPreset,
                    afterSalahStep = 0,
                    currentCount = 0,
                    sessionIncrementCount = 0,
                    sessionStatus = SessionStatus.IDLE,
                    isTargetReached = false,
                    showResetDialog = false
                )
                persistState(updatedState)
                updatedState
            } else {
                val updatedState = state.copy(
                    currentCount = 0,
                    sessionIncrementCount = 0,
                    sessionStatus = SessionStatus.IDLE,
                    isTargetReached = false,
                    showResetDialog = false
                )
                persistState(updatedState)
                updatedState
            }
        }
    }

    fun setTarget(newTarget: Int) {
        if (newTarget <= 0) return
        hasTriggeredCompletionHaptic = false
        _uiState.update { state ->
            val isReached = state.currentCount >= newTarget
            val status = computeSessionStatus(state.currentCount, newTarget)
            val updatedState = state.copy(
                target = newTarget,
                sessionStatus = status,
                isTargetReached = isReached,
                showTargetSelection = false,
                showCustomTargetDialog = false
            )
            persistState(updatedState)
            updatedState
        }
    }

    fun selectDhikr(preset: DhikrPreset) {
        hasTriggeredCompletionHaptic = false
        val isAfterSalah = preset.id == DhikrPreset.AFTER_SALAH_PRESET.id || preset.isAfterSalahChain || preset.title.contains("After-Salah", ignoreCase = true)

        _uiState.update { state ->
            if (isAfterSalah) {
                val step0 = com.abdullah.dhikr.domain.model.AfterSalahChain.STEPS[0]
                val activePreset = DhikrPreset(
                    id = DhikrPreset.AFTER_SALAH_PRESET.id,
                    title = "After-Salah: ${step0.title}",
                    arabicText = step0.arabicText,
                    translation = "${step0.translation} (Step 1 of 3)",
                    defaultTarget = step0.target,
                    isAfterSalahChain = true
                )
                val updatedState = state.copy(
                    selectedDhikr = activePreset,
                    isCustomDhikr = false,
                    isAfterSalahMode = true,
                    afterSalahStep = 0,
                    currentCount = 0,
                    sessionIncrementCount = 0,
                    target = step0.target,
                    sessionStatus = SessionStatus.IDLE,
                    isTargetReached = false,
                    showDhikrSelection = false
                )
                persistState(updatedState)
                syncToDatabaseSessionDhikrChange(activePreset)
                updatedState
            } else {
                val newTarget = preset.defaultTarget
                val isReached = 0 >= newTarget
                val status = computeSessionStatus(0, newTarget)

                val updatedState = state.copy(
                    selectedDhikr = preset,
                    isCustomDhikr = preset.isCustom,
                    isAfterSalahMode = false,
                    afterSalahStep = 0,
                    currentCount = 0,
                    sessionIncrementCount = 0,
                    target = newTarget,
                    sessionStatus = status,
                    isTargetReached = isReached,
                    showDhikrSelection = false
                )
                persistState(updatedState)
                syncToDatabaseSessionDhikrChange(preset)
                updatedState
            }
        }
    }

    fun setCustomDhikr(title: String, arabic: String, translation: String, targetCount: Int = 33) {
        val validTarget = if (targetCount > 0) targetCount else 33
        val cleanTitle = title.ifBlank { "Custom Dhikr" }

        viewModelScope.launch {
            if (dhikrRepository != null) {
                dhikrRepository.insertItem(
                    DhikrItem(
                        title = cleanTitle,
                        arabicText = arabic,
                        translation = translation,
                        defaultTarget = validTarget,
                        isCustom = true
                    )
                )
                val allItems = dhikrRepository.getAllItemsStream().first()
                val newlyInserted = allItems.find { it.title.equals(cleanTitle, ignoreCase = true) && it.isCustom }
                if (newlyInserted != null) {
                    val presetFromDb = DhikrPreset(
                        id = newlyInserted.id.toString(),
                        title = newlyInserted.title,
                        arabicText = newlyInserted.arabicText,
                        translation = newlyInserted.translation,
                        defaultTarget = newlyInserted.defaultTarget,
                        isCustom = true
                    )
                    selectDhikr(presetFromDb)
                    return@launch
                }
            }

            val customPreset = DhikrPreset(
                id = "custom_${System.currentTimeMillis()}",
                title = cleanTitle,
                arabicText = arabic,
                translation = translation,
                defaultTarget = validTarget,
                isCustom = true
            )
            selectDhikr(customPreset)
        }
    }

    fun deleteDhikrPreset(preset: DhikrPreset) {
        val intId = preset.id.toIntOrNull()
        viewModelScope.launch {
            val allItems = dhikrRepository?.getAllItemsStream()?.first() ?: emptyList()
            val itemToDelete = allItems.find {
                if (intId != null) it.id == intId else it.title.equals(preset.title, ignoreCase = true)
            }
            if (itemToDelete != null && itemToDelete.isCustom) {
                dhikrRepository?.deleteItem(itemToDelete)
                if (_uiState.value.selectedDhikr.title.equals(preset.title, ignoreCase = true)) {
                    val fallback = DhikrPreset.PRESETS.first()
                    selectDhikr(fallback)
                }
            }
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            activeSessionId?.let { id ->
                sessionRepository?.completeSession(id)
            }
            activeSessionId = null
        }
        resetCount()
    }

    fun toggleDhikrSelection(show: Boolean) {
        _uiState.update { it.copy(showDhikrSelection = show) }
    }

    fun toggleTargetSelection(show: Boolean) {
        _uiState.update { it.copy(showTargetSelection = show) }
    }

    fun toggleCustomTargetDialog(show: Boolean) {
        _uiState.update { it.copy(showCustomTargetDialog = show, showTargetSelection = false) }
    }

    fun toggleCustomDhikrDialog(show: Boolean) {
        _uiState.update { it.copy(showCustomDhikrDialog = show, showDhikrSelection = false) }
    }

    private fun computeSessionStatus(count: Int, target: Int): SessionStatus {
        return when {
            count == 0 -> SessionStatus.IDLE
            count >= target -> SessionStatus.COMPLETED
            else -> SessionStatus.ACTIVE
        }
    }

    private fun persistState(state: CounterUiState) {
        viewModelScope.launch {
            userPreferencesRepository.saveCounterPreferences(
                CounterPreferences(
                    count = state.currentCount,
                    target = state.target,
                    sessionIncrementCount = state.sessionIncrementCount,
                    dhikrTitle = state.selectedDhikr.title,
                    dhikrArabic = state.selectedDhikr.arabicText,
                    dhikrTranslation = state.selectedDhikr.translation,
                    isCustomDhikr = state.isCustomDhikr
                )
            )
        }
    }

    private fun syncToDatabaseSessionDhikrChange(preset: DhikrPreset) {
        if (sessionRepository == null) return
        viewModelScope.launch {
            try {
                val presetIntId = preset.id.toIntOrNull()
                val newId = sessionRepository.createNewSession(
                    presetId = presetIntId,
                    nameSnapshot = preset.title,
                    target = preset.defaultTarget
                )
                activeSessionId = newId
            } catch (_: Exception) {}
        }
    }

    private fun syncToDatabaseSessionIncrement() {
        if (sessionRepository == null) return
        viewModelScope.launch {
            try {
                val currentId = activeSessionId
                if (currentId != null) {
                    sessionRepository.incrementSessionCount(currentId)
                } else {
                    val state = _uiState.value
                    val newId = sessionRepository.createNewSession(
                        presetId = null,
                        nameSnapshot = state.selectedDhikr.title,
                        target = state.target
                    )
                    activeSessionId = newId
                    sessionRepository.incrementSessionCount(newId)
                }
            } catch (_: Exception) {}
        }
    }

    private fun syncToDatabaseSessionUndo() {
        if (sessionRepository == null) return
        viewModelScope.launch {
            try {
                activeSessionId?.let { id ->
                    sessionRepository.undoSessionCount(id)
                }
            } catch (_: Exception) {}
        }
    }

    private fun syncToDatabaseSessionReset(countAtReset: Int, targetAtReset: Int) {
        if (sessionRepository == null) return
        viewModelScope.launch {
            try {
                val currentId = activeSessionId
                if (currentId != null && countAtReset > 0) {
                    if (countAtReset >= targetAtReset) {
                        sessionRepository.completeSession(currentId)
                    } else {
                        sessionRepository.pauseSession(currentId)
                    }
                }
                activeSessionId = null
            } catch (_: Exception) {}
        }
    }
}
