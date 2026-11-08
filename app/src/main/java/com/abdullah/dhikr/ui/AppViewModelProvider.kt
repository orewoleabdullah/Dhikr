package com.abdullah.dhikr.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.abdullah.dhikr.DhikrApplication
import com.abdullah.dhikr.features.counter.presentation.CounterViewModel
import com.abdullah.dhikr.features.history.presentation.HistoryViewModel
import com.abdullah.dhikr.features.presets.presentation.PresetsViewModel
import com.abdullah.dhikr.features.qibla.presentation.QiblaViewModel
import com.abdullah.dhikr.features.settings.presentation.SettingsViewModel
import com.abdullah.dhikr.features.statistics.presentation.StatisticsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            CounterViewModel(
                userPreferencesRepository = dhikrApplication().container.userPreferencesRepository,
                sessionRepository = dhikrApplication().container.sessionRepository,
                dhikrRepository = dhikrApplication().container.dhikrRepository
            )
        }
        initializer {
            PresetsViewModel(
                dhikrRepository = dhikrApplication().container.dhikrRepository,
                userPreferencesRepository = dhikrApplication().container.userPreferencesRepository
            )
        }
        initializer {
            QiblaViewModel(
                application = dhikrApplication()
            )
        }
        initializer {
            HistoryViewModel(
                sessionRepository = dhikrApplication().container.sessionRepository
            )
        }
        initializer {
            StatisticsViewModel(
                sessionRepository = dhikrApplication().container.sessionRepository
            )
        }
        initializer {
            SettingsViewModel(
                userPreferencesRepository = dhikrApplication().container.userPreferencesRepository,
                sessionRepository = dhikrApplication().container.sessionRepository,
                dhikrRepository = dhikrApplication().container.dhikrRepository
            )
        }
    }
}

/**
 * Extension function to query for [Application] object and return an instance of
 * [DhikrApplication].
 */
fun CreationExtras.dhikrApplication(): DhikrApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DhikrApplication)
