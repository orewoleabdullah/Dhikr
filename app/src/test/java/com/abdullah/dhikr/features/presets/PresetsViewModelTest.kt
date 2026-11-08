package com.abdullah.dhikr.features.presets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.data.repository.FakeDhikrRepository
import com.abdullah.dhikr.features.presets.presentation.PresetFilterTab
import com.abdullah.dhikr.features.presets.presentation.PresetsViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PresetsViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var fakeDhikrRepository: FakeDhikrRepository
    private lateinit var viewModel: PresetsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val testFile = File(tempFolder.newFolder(), "test_user_prefs.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        fakeDhikrRepository = FakeDhikrRepository()
        viewModel = PresetsViewModel(fakeDhikrRepository, userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testBuiltInPresets_areSeededOnInitialize() = runTest {
        val state = viewModel.uiState.value
        assertEquals(8, state.dhikrList.size)
        assertTrue(state.dhikrList.all { !it.isCustom })
    }

    @Test
    fun testSearch_filtersResultsByTitleOrArabicOrTranslation() = runTest {
        viewModel.onSearchQueryChange("Alhamdulillah")

        val state = viewModel.uiState.value
        assertEquals(2, state.dhikrList.size)
        assertTrue(state.dhikrList.any { it.title == "Alhamdulillah" })

        viewModel.onSearchQueryChange("سُبْحَانَ")
        val arabicSearchState = viewModel.uiState.value
        assertEquals(2, arabicSearchState.dhikrList.size)
    }

    @Test
    fun testAddCustomDhikr_validInputs_createsNewEntry() = runTest {
        val success = viewModel.saveDhikr(
            title = "Hasbunallah",
            arabic = "حَسْبُنَا اللَّهُ",
            translation = "Allah is sufficient for us",
            description = "Qur'anic phrase",
            defaultTarget = 7
        )

        assertTrue(success)
        val state = viewModel.uiState.value
        assertEquals(9, state.dhikrList.size)

        val newCustom = state.dhikrList.find { it.title == "Hasbunallah" }
        assertNotNull(newCustom)
        assertTrue(newCustom!!.isCustom)
        assertEquals(7, newCustom.defaultTarget)
    }

    @Test
    fun testAddCustomDhikr_emptyTitleOrInvalidTarget_rejected() = runTest {
        val emptyTitleSuccess = viewModel.saveDhikr(
            title = "",
            arabic = "test",
            translation = "test",
            description = null,
            defaultTarget = 33
        )
        assertFalse(emptyTitleSuccess)

        val invalidTargetSuccess = viewModel.saveDhikr(
            title = "Valid Name",
            arabic = "",
            translation = "",
            description = null,
            defaultTarget = 0
        )
        assertFalse(invalidTargetSuccess)
    }

    @Test
    fun testEditCustomDhikr_updatesExistingEntry() = runTest {
        viewModel.saveDhikr(
            title = "My Dhikr",
            arabic = "عربي",
            translation = "Translation",
            description = null,
            defaultTarget = 33
        )

        val addedItem = viewModel.uiState.value.dhikrList.find { it.title == "My Dhikr" }!!
        viewModel.openEditDialog(addedItem)

        viewModel.saveDhikr(
            title = "My Updated Dhikr",
            arabic = "عربي معدل",
            translation = "Updated Translation",
            description = "Note",
            defaultTarget = 100
        )

        val updatedItem = viewModel.uiState.value.dhikrList.find { it.id == addedItem.id }
        assertNotNull(updatedItem)
        assertEquals("My Updated Dhikr", updatedItem!!.title)
        assertEquals(100, updatedItem.defaultTarget)
    }

    @Test
    fun testDeleteCustomDhikr_removesEntry() = runTest {
        viewModel.saveDhikr(
            title = "To Be Deleted",
            arabic = "",
            translation = "",
            description = null,
            defaultTarget = 33
        )

        val addedItem = viewModel.uiState.value.dhikrList.find { it.title == "To Be Deleted" }!!
        viewModel.requestDeleteDhikr(addedItem)

        assertNotNull(viewModel.uiState.value.deleteConfirmationDhikr)

        viewModel.confirmDeleteDhikr()

        assertNull(viewModel.uiState.value.deleteConfirmationDhikr)
        val remaining = viewModel.uiState.value.dhikrList.find { it.id == addedItem.id }
        assertNull(remaining)
    }

    @Test
    fun testBuiltInPreset_cannotBeEditedOrDeleted() = runTest {
        val builtIn = viewModel.uiState.value.dhikrList.first { !it.isCustom }

        viewModel.openEditDialog(builtIn)
        assertFalse(viewModel.uiState.value.isAddEditDialogVisible)

        viewModel.requestDeleteDhikr(builtIn)
        assertNull(viewModel.uiState.value.deleteConfirmationDhikr)
    }

    @Test
    fun testSelectDhikr_updatesActiveSelectionPreference() = runTest {
        val builtIn = viewModel.uiState.value.dhikrList.find { it.title == "Alhamdulillah" }!!
        viewModel.selectDhikr(builtIn)

        val activeTitle = userPreferencesRepository.counterPreferences.first().dhikrTitle
        assertEquals("Alhamdulillah", activeTitle)
    }

    @Test
    fun testFilterTabs_filtersBuiltInAndCustomCorrectly() = runTest {
        viewModel.saveDhikr("Custom One", "", "", null, 33)

        viewModel.onSelectFilterTab(PresetFilterTab.BUILT_IN)
        val builtInOnly = viewModel.uiState.value.dhikrList
        assertTrue(builtInOnly.all { !it.isCustom })
        assertEquals(8, builtInOnly.size)

        viewModel.onSelectFilterTab(PresetFilterTab.CUSTOM)
        val customOnly = viewModel.uiState.value.dhikrList
        assertTrue(customOnly.all { it.isCustom })
        assertEquals(1, customOnly.size)
    }
}
