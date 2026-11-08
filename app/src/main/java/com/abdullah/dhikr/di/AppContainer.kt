package com.abdullah.dhikr.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.abdullah.dhikr.data.local.database.DhikrDatabase
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.data.repository.OfflineDhikrRepository
import com.abdullah.dhikr.data.repository.OfflineSessionRepository
import com.abdullah.dhikr.domain.repository.DhikrRepository
import com.abdullah.dhikr.domain.repository.SessionRepository

/**
 * App container for Dependency Injection.
 */
interface AppContainer {
    val dhikrRepository: DhikrRepository
    val sessionRepository: SessionRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * [AppContainer] implementation that provides application dependencies.
 */
class AppDataContainer(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) : AppContainer {

    private val database: DhikrDatabase by lazy {
        DhikrDatabase.getDatabase(context)
    }

    override val dhikrRepository: DhikrRepository by lazy {
        OfflineDhikrRepository(database.dhikrDao())
    }

    override val sessionRepository: SessionRepository by lazy {
        OfflineSessionRepository(database.dhikrSessionDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}
