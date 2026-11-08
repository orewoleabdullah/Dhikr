package com.abdullah.dhikr.features.counter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.abdullah.dhikr.data.preferences.UserPreferencesRepository
import com.abdullah.dhikr.domain.model.DhikrPreset
import com.abdullah.dhikr.features.counter.presentation.CounterUiEvent
import com.abdullah.dhikr.features.counter.presentation.CounterViewModel
import com.abdullah.dhikr.features.counter.presentation.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class CounterViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val testFile = File(tempFolder.newFolder(), "test_user_prefs.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        viewModel = CounterViewModel(userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_countIsZero() = runTest {
        val state = viewModel.uiState.value
        assertEquals(0, state.currentCount)
        assertEquals(33, state.target)
        assertEquals(SessionStatus.IDLE, state.sessionStatus)
        assertFalse(state.isUndoAvailable)
        assertFalse(state.isTargetReached)
    }

    @Test
    fun testIncrement_singleTap_increasesCountByOne() = runTest {
        viewModel.incrementCount()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentCount)
        assertEquals(1, state.sessionIncrementCount)
        assertEquals(SessionStatus.ACTIVE, state.sessionStatus)
        assertTrue(state.isUndoAvailable)
    }

    @Test
    fun testIncrement_repeatedTaps_incrementsCorrectly() = runTest {
        repeat(10) {
            viewModel.incrementCount()
        }

        val state = viewModel.uiState.value
        assertEquals(10, state.currentCount)
        assertEquals(10, state.sessionIncrementCount)
    }

    @Test
    fun testUndo_reversesOneIncrement() = runTest {
        repeat(5) {
            viewModel.incrementCount()
        }

        viewModel.undoCount()

        val state = viewModel.uiState.value
        assertEquals(4, state.currentCount)
        assertEquals(4, state.sessionIncrementCount)
    }

    @Test
    fun testUndo_whenCountIsZero_cannotProduceNegativeCount() = runTest {
        viewModel.undoCount()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentCount)
        assertEquals(0, state.sessionIncrementCount)
        assertFalse(state.isUndoAvailable)
    }

    @Test
    fun testResetConfirmation_clearsCount() = runTest {
        repeat(5) {
            viewModel.incrementCount()
        }

        viewModel.resetCount()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentCount)
        assertEquals(0, state.sessionIncrementCount)
        assertEquals(SessionStatus.IDLE, state.sessionStatus)
        assertFalse(state.isUndoAvailable)
    }

    @Test
    fun testResetCancellation_preservesCount() = runTest {
        repeat(5) {
            viewModel.incrementCount()
        }

        viewModel.showResetDialog(true)
        assertTrue(viewModel.uiState.value.showResetDialog)

        viewModel.showResetDialog(false)
        assertFalse(viewModel.uiState.value.showResetDialog)
        assertEquals(5, viewModel.uiState.value.currentCount)
    }

    @Test
    fun testTargetCompletion_triggersHapticOnceAndAllowsContinuedCounting() = runTest {
        val events = mutableListOf<CounterUiEvent>()
        val job = launch(testDispatcher) {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.setTarget(3)

        viewModel.incrementCount() // 1
        viewModel.incrementCount() // 2
        viewModel.incrementCount() // 3 -> Target Reached

        val stateAtTarget = viewModel.uiState.value
        assertEquals(3, stateAtTarget.currentCount)
        assertTrue(stateAtTarget.isTargetReached)
        assertEquals(SessionStatus.COMPLETED, stateAtTarget.sessionStatus)

        // Count past target
        viewModel.incrementCount() // 4
        val stateAfterTarget = viewModel.uiState.value
        assertEquals(4, stateAfterTarget.currentCount)
        assertTrue(stateAfterTarget.isTargetReached)

        // Completion haptic should have been emitted exactly once
        val completionEvents = events.filterIsInstance<CounterUiEvent.TriggerCompletionHaptic>()
        assertEquals(1, completionEvents.size)

        job.cancel()
    }

    @Test
    fun testUndo_belowTarget_resetsCompletionHapticFlag() = runTest {
        val events = mutableListOf<CounterUiEvent>()
        val job = launch(testDispatcher) {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.setTarget(3)
        repeat(3) { viewModel.incrementCount() }

        assertEquals(1, events.filterIsInstance<CounterUiEvent.TriggerCompletionHaptic>().size)

        // Undo below target
        viewModel.undoCount()
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(2, stateAfterUndo.currentCount)
        assertFalse(stateAfterUndo.isTargetReached)
        assertEquals(SessionStatus.ACTIVE, stateAfterUndo.sessionStatus)

        // Increment back to target
        viewModel.incrementCount()
        assertEquals(2, events.filterIsInstance<CounterUiEvent.TriggerCompletionHaptic>().size)

        job.cancel()
    }

    @Test
    fun testHapticFeedback_whenDisabled_emitsNoHapticEvents() = runTest {
        userPreferencesRepository.saveVibrationPreference(false)

        val events = mutableListOf<CounterUiEvent>()
        val job = launch(testDispatcher) {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.incrementCount()

        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun testSelectDhikr_updatesSelectedDhikr() = runTest {
        val preset = DhikrPreset.PRESETS.find { it.title == "Alhamdulillah" }!!
        viewModel.selectDhikr(preset)

        val state = viewModel.uiState.value
        assertEquals("Alhamdulillah", state.selectedDhikr.title)
        assertFalse(state.isCustomDhikr)
    }

    @Test
    fun testSetCustomDhikr_updatesState() = runTest {
        viewModel.setCustomDhikr("SubhanAllah wa Bihamdihi", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Glory be to Allah and His is the praise")

        val state = viewModel.uiState.value
        assertEquals("SubhanAllah wa Bihamdihi", state.selectedDhikr.title)
        assertTrue(state.isCustomDhikr)
    }
}
