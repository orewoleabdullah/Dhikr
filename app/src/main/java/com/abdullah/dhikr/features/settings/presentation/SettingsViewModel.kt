package com.abdullah.dhikr.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.dhikr.data.preferences.ThemeMode
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.domain.repository.DhikrRepository
import com.abdullah.dhikr.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionRepository: SessionRepository? = null,
    private val dhikrRepository: DhikrRepository? = null
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val isDarkTheme: StateFlow<Boolean> = userPreferencesRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isVibrationEnabled: StateFlow<Boolean> = userPreferencesRepository.isVibrationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isSoundEnabled: StateFlow<Boolean> = userPreferencesRepository.isSoundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isCompletionFeedbackEnabled: StateFlow<Boolean> = userPreferencesRepository.isCompletionFeedbackEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemeMode(mode)
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(isDark)
        }
    }

    fun toggleVibration(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveVibrationPreference(isEnabled)
        }
    }

    fun toggleSound(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveSoundPreference(isEnabled)
        }
    }

    fun toggleCompletionFeedback(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveCompletionFeedbackPreference(isEnabled)
        }
    }

    fun deleteAllApplicationData() {
        viewModelScope.launch {
            // Delete all sessions from DB
            sessionRepository?.let { repo ->
                val sessions = repo.getAllSessionsStream().first()
                sessions.forEach { session ->
                    repo.deleteSession(session)
                }
            }

            // Delete custom presets from DB
            dhikrRepository?.let { repo ->
                val presets = repo.getAllItemsStream().first()
                presets.filter { it.isCustom }.forEach { customPreset ->
                    repo.deleteItem(customPreset)
                }
                repo.seedBuiltInPresetsIfEmpty()
            }

            // Reset DataStore preferences
            userPreferencesRepository.resetAllCounterPreferences()
        }
    }
}
