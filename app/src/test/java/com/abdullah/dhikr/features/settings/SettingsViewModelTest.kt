package com.abdullah.dhikr.features.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.abdullah.dhikr.data.preferences.ThemeMode
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.data.repository.FakeDhikrRepository
import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.features.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var fakeDhikrRepository: FakeDhikrRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val testFile = File(tempFolder.newFolder(), "test_settings_prefs.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        fakeDhikrRepository = FakeDhikrRepository()
        viewModel = SettingsViewModel(
            userPreferencesRepository = userPreferencesRepository,
            dhikrRepository = fakeDhikrRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetThemeMode_persistsMode() = runTest {
        viewModel.setThemeMode(ThemeMode.DARK)

        val savedMode = userPreferencesRepository.themeMode.first()
        assertEquals(ThemeMode.DARK, savedMode)
    }

    @Test
    fun testToggleVibration_persistsPreference() = runTest {
        viewModel.toggleVibration(false)

        val isVibeEnabled = userPreferencesRepository.isVibrationEnabled.first()
        assertFalse(isVibeEnabled)
    }

    @Test
    fun testToggleSound_persistsPreference() = runTest {
        viewModel.toggleSound(true)

        val isSoundEnabled = userPreferencesRepository.isSoundEnabled.first()
        assertTrue(isSoundEnabled)
    }

    @Test
    fun testToggleCompletionFeedback_persistsPreference() = runTest {
        viewModel.toggleCompletionFeedback(false)

        val isCompletionEnabled = userPreferencesRepository.isCompletionFeedbackEnabled.first()
        assertFalse(isCompletionEnabled)
    }

    @Test
    fun testDeleteAllApplicationData_resetsRepositoryAndPreferences() = runTest {
        // Add a custom preset
        fakeDhikrRepository.insertItem(
            DhikrItem(id = 10, title = "Custom Dhikr", isCustom = true)
        )

        // Delete all application data
        viewModel.deleteAllApplicationData()

        val remainingDhikrs = fakeDhikrRepository.getAllItemsStream().first()
        assertTrue(remainingDhikrs.none { it.isCustom })
        assertTrue(remainingDhikrs.any { !it.isCustom }) // Built-in presets re-seeded
    }
}
