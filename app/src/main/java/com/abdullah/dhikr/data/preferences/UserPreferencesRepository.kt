package com.abdullah.dhikr.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class CounterPreferences(
    val count: Int = 0,
    val target: Int = 33,
    val sessionIncrementCount: Int = 0,
    val dhikrTitle: String = "SubhanAllah",
    val dhikrArabic: String = "سُبْحَانَ ٱللَّٰهِ",
    val dhikrTranslation: String = "Glory be to Allah",
    val isCustomDhikr: Boolean = false
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val COMPLETION_FEEDBACK_ENABLED = booleanPreferencesKey("completion_feedback_enabled")
        val ACTIVE_SESSION_ID = longPreferencesKey("active_session_id")

        val COUNTER_COUNT = intPreferencesKey("counter_count")
        val COUNTER_TARGET = intPreferencesKey("counter_target")
        val COUNTER_SESSION_INCREMENTS = intPreferencesKey("counter_session_increments")
        val COUNTER_DHIKR_TITLE = stringPreferencesKey("counter_dhikr_title")
        val COUNTER_DHIKR_ARABIC = stringPreferencesKey("counter_dhikr_arabic")
        val COUNTER_DHIKR_TRANSLATION = stringPreferencesKey("counter_dhikr_translation")
        val COUNTER_IS_CUSTOM = booleanPreferencesKey("counter_is_custom")

        const val TAG = "UserPreferencesRepo"
    }

    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading theme mode.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[THEME_MODE]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    val isDarkTheme: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading theme preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_DARK_THEME] ?: false
        }

    val isVibrationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading vibration preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[VIBRATION_ENABLED] ?: true
        }

    val isSoundEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sound preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SOUND_ENABLED] ?: false
        }

    val isCompletionFeedbackEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading completion feedback preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[COMPLETION_FEEDBACK_ENABLED] ?: true
        }

    val activeSessionId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading active session ID.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ACTIVE_SESSION_ID]
        }

    val counterPreferences: Flow<CounterPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading counter preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            CounterPreferences(
                count = preferences[COUNTER_COUNT] ?: 0,
                target = preferences[COUNTER_TARGET] ?: 33,
                sessionIncrementCount = preferences[COUNTER_SESSION_INCREMENTS] ?: 0,
                dhikrTitle = preferences[COUNTER_DHIKR_TITLE] ?: "SubhanAllah",
                dhikrArabic = preferences[COUNTER_DHIKR_ARABIC] ?: "سُبْحَانَ ٱللَّٰهِ",
                dhikrTranslation = preferences[COUNTER_DHIKR_TRANSLATION] ?: "Glory be to Allah",
                isCustomDhikr = preferences[COUNTER_IS_CUSTOM] ?: false
            )
        }

    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
            if (mode != ThemeMode.SYSTEM) {
                preferences[IS_DARK_THEME] = (mode == ThemeMode.DARK)
            }
        }
    }

    suspend fun saveThemePreference(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
            preferences[THEME_MODE] = if (isDarkTheme) ThemeMode.DARK.name else ThemeMode.LIGHT.name
        }
    }

    suspend fun saveVibrationPreference(isVibrationEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = isVibrationEnabled
        }
    }

    suspend fun saveSoundPreference(isSoundEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = isSoundEnabled
        }
    }

    suspend fun saveCompletionFeedbackPreference(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[COMPLETION_FEEDBACK_ENABLED] = isEnabled
        }
    }

    suspend fun saveActiveSessionId(sessionId: Long?) {
        dataStore.edit { preferences ->
            if (sessionId != null) {
                preferences[ACTIVE_SESSION_ID] = sessionId
            } else {
                preferences.remove(ACTIVE_SESSION_ID)
            }
        }
    }

    suspend fun saveCounterPreferences(counterPrefs: CounterPreferences) {
        dataStore.edit { preferences ->
            preferences[COUNTER_COUNT] = counterPrefs.count
            preferences[COUNTER_TARGET] = counterPrefs.target
            preferences[COUNTER_SESSION_INCREMENTS] = counterPrefs.sessionIncrementCount
            preferences[COUNTER_DHIKR_TITLE] = counterPrefs.dhikrTitle
            preferences[COUNTER_DHIKR_ARABIC] = counterPrefs.dhikrArabic
            preferences[COUNTER_DHIKR_TRANSLATION] = counterPrefs.dhikrTranslation
            preferences[COUNTER_IS_CUSTOM] = counterPrefs.isCustomDhikr
        }
    }

    suspend fun resetAllCounterPreferences() {
        dataStore.edit { preferences ->
            preferences[COUNTER_COUNT] = 0
            preferences[COUNTER_TARGET] = 33
            preferences[COUNTER_SESSION_INCREMENTS] = 0
            preferences[COUNTER_DHIKR_TITLE] = "SubhanAllah"
            preferences[COUNTER_DHIKR_ARABIC] = "سُبْحَانَ ٱللَّٰهِ"
            preferences[COUNTER_DHIKR_TRANSLATION] = "Glory be to Allah"
            preferences[COUNTER_IS_CUSTOM] = false
            preferences.remove(ACTIVE_SESSION_ID)
        }
    }
}
