package com.abdullah.dhikr.features.counter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.data.repository.FakeDhikrRepository
import com.abdullah.dhikr.domain.model.DhikrPreset
import com.abdullah.dhikr.features.counter.presentation.CounterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CounterViewModelChainTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var fakeDhikrRepository: FakeDhikrRepository
    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val testFile = File(tempFolder.newFolder(), "test_chain_prefs.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        fakeDhikrRepository = FakeDhikrRepository()
        viewModel = CounterViewModel(
            userPreferencesRepository = userPreferencesRepository,
            dhikrRepository = fakeDhikrRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSelectQuickCounter_allowsFreeCounting() = runTest {
        viewModel.selectDhikr(DhikrPreset.FREE_COUNTER)

        val state = viewModel.uiState.value
        assertEquals("Quick Counter", state.selectedDhikr.title)
        assertFalse(state.isAfterSalahMode)
        assertEquals(0, state.currentCount)

        // Increment 5 times
        repeat(5) { viewModel.incrementCount() }
        assertEquals(5, viewModel.uiState.value.currentCount)
    }

    @Test
    fun testAfterSalahChain_automaticallyAdvancesAcrossSteps() = runTest {
        // Select After-Salah Tasbeeh
        viewModel.selectDhikr(DhikrPreset.AFTER_SALAH_PRESET)

        var state = viewModel.uiState.value
        assertTrue(state.isAfterSalahMode)
        assertEquals(0, state.afterSalahStep) // Step 0: SubhanAllah
        assertTrue(state.selectedDhikr.title.contains("SubhanAllah"))

        // Increment 33 times for Step 0 (SubhanAllah)
        repeat(33) { viewModel.incrementCount() }

        state = viewModel.uiState.value
        assertEquals(1, state.afterSalahStep) // Automatically advanced to Step 1: Alhamdulillah
        assertEquals(0, state.currentCount)
        assertTrue(state.selectedDhikr.title.contains("Alhamdulillah"))

        // Increment 33 times for Step 1 (Alhamdulillah)
        repeat(33) { viewModel.incrementCount() }

        state = viewModel.uiState.value
        assertEquals(2, state.afterSalahStep) // Automatically advanced to Step 2: Allahu Akbar
        assertEquals(0, state.currentCount)
        assertTrue(state.selectedDhikr.title.contains("Allahu Akbar"))

        // Increment 33 times for Step 2 (Allahu Akbar) -> Completes After-Salah chain and moves to Quick Counter
        repeat(33) { viewModel.incrementCount() }

        state = viewModel.uiState.value
        assertFalse(state.isAfterSalahMode)
        assertEquals(0, state.afterSalahStep)
        assertEquals(0, state.currentCount)
        assertEquals("Quick Counter", state.selectedDhikr.title)
    }

    @Test
    fun testAfterSalahChain_undoStepsBackAcrossBoundaries() = runTest {
        viewModel.selectDhikr(DhikrPreset.AFTER_SALAH_PRESET)

        // Count 33 to advance to Step 1 (Alhamdulillah)
        repeat(33) { viewModel.incrementCount() }

        var state = viewModel.uiState.value
        assertEquals(1, state.afterSalahStep)
        assertEquals(0, state.currentCount)

        // Undo once at count 0 of Step 1
        viewModel.undoCount()

        state = viewModel.uiState.value
        assertEquals(0, state.afterSalahStep) // Stepped back to SubhanAllah
        assertEquals(32, state.currentCount) // Count restored to 32
        assertTrue(state.selectedDhikr.title.contains("SubhanAllah"))
    }
}
