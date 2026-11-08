package com.abdullah.dhikr.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abdullah.dhikr.data.local.dao.DhikrPresetDao
import com.abdullah.dhikr.data.local.dao.DhikrSessionDao
import com.abdullah.dhikr.data.local.database.DhikrDatabase
import com.abdullah.dhikr.data.local.entity.DhikrPresetEntity
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.data.repository.OfflineSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DhikrDatabaseTest {

    private lateinit var db: DhikrDatabase
    private lateinit var presetDao: DhikrPresetDao
    private lateinit var sessionDao: DhikrSessionDao
    private lateinit var sessionRepository: OfflineSessionRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DhikrDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        presetDao = db.dhikrPresetDao()
        sessionDao = db.dhikrSessionDao()
        sessionRepository = OfflineSessionRepository(sessionDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCreatePreset_andSearch() = runBlocking {
        val preset = DhikrPresetEntity(
            name = "Test Dhikr",
            arabicText = "اختبار",
            transliteration = "Test",
            translation = "Test Translation",
            defaultTarget = 33,
            isBuiltIn = false
        )
        val id = presetDao.insertPreset(preset)
        assertTrue(id > 0)

        val results = presetDao.searchPresetsStream("Test").first()
        assertEquals(1, results.size)
        assertEquals("Test Dhikr", results.first().name)
    }

    @Test
    fun testUpdateCustomPreset() = runBlocking {
        val preset = DhikrPresetEntity(
            name = "Original Name",
            defaultTarget = 33,
            isBuiltIn = false
        )
        val id = presetDao.insertPreset(preset).toInt()
        val original = presetDao.getPresetById(id)!!

        val updated = original.copy(name = "Updated Name", defaultTarget = 100)
        presetDao.updatePreset(updated)

        val fetched = presetDao.getPresetById(id)
        assertEquals("Updated Name", fetched?.name)
        assertEquals(100, fetched?.defaultTarget)
    }

    @Test
    fun testDeleteCustomPreset_preservesHistoricalSession() = runBlocking {
        val preset = DhikrPresetEntity(
            name = "Temporary Dhikr",
            defaultTarget = 33,
            isBuiltIn = false
        )
        val presetId = presetDao.insertPreset(preset).toInt()

        val sessionId = sessionRepository.createNewSession(
            presetId = presetId,
            nameSnapshot = "Temporary Dhikr",
            target = 33
        )

        val fetchedPreset = presetDao.getPresetById(presetId)!!
        presetDao.deletePreset(fetchedPreset)

        val deletedPreset = presetDao.getPresetById(presetId)
        assertNull(deletedPreset)

        // Verify historical session retains original name and is not deleted
        val session = sessionDao.getSessionById(sessionId)
        assertNotNull(session)
        assertNull(session?.presetId) // Foreign key SET_NULL
        assertEquals("Temporary Dhikr", session?.dhikrNameSnapshot)
    }

    @Test
    fun testSessionLifecycle_create_increment_undo_pause_resume_complete() = runBlocking {
        // 1. Create Session
        val sessionId = sessionRepository.createNewSession(
            presetId = null,
            nameSnapshot = "SubhanAllah",
            target = 3
        )
        assertTrue(sessionId > 0)

        val initialActive = sessionRepository.getActiveSession()
        assertNotNull(initialActive)
        assertEquals(0, initialActive?.count)
        assertEquals(DhikrSessionEntity.SESSION_STATUS_ACTIVE, initialActive?.status)

        // 2. Increment count
        sessionRepository.incrementSessionCount(sessionId) // 1
        sessionRepository.incrementSessionCount(sessionId) // 2

        val incrementedSession = sessionDao.getSessionById(sessionId)
        assertEquals(2, incrementedSession?.count)
        assertEquals(DhikrSessionEntity.SESSION_STATUS_ACTIVE, incrementedSession?.status)

        // 3. Undo count
        sessionRepository.undoSessionCount(sessionId) // 1
        val undoneSession = sessionDao.getSessionById(sessionId)
        assertEquals(1, undoneSession?.count)

        // 4. Pause session
        sessionRepository.pauseSession(sessionId)
        val pausedSession = sessionDao.getSessionById(sessionId)
        assertEquals(DhikrSessionEntity.SESSION_STATUS_PAUSED, pausedSession?.status)

        // 5. Resume session
        sessionRepository.resumeSession(sessionId)
        val resumedSession = sessionDao.getSessionById(sessionId)
        assertEquals(DhikrSessionEntity.SESSION_STATUS_ACTIVE, resumedSession?.status)

        // 6. Complete session (increment to target 3)
        sessionRepository.incrementSessionCount(sessionId) // 2
        sessionRepository.incrementSessionCount(sessionId) // 3 -> COMPLETED

        val completedSession = sessionDao.getSessionById(sessionId)
        assertEquals(3, completedSession?.count)
        assertEquals(DhikrSessionEntity.SESSION_STATUS_COMPLETED, completedSession?.status)
        assertNotNull(completedSession?.completedAt)
    }

    @Test
    fun testDeleteSession() = runBlocking {
        val sessionId = sessionRepository.createNewSession(
            presetId = null,
            nameSnapshot = "ToDelete",
            target = 33
        )
        val session = sessionDao.getSessionById(sessionId)!!

        sessionRepository.deleteSession(session)

        val deleted = sessionDao.getSessionById(sessionId)
        assertNull(deleted)
    }

    @Test
    fun testRestoringActiveSessionAfterAppRecreation() = runBlocking {
        val sessionId = sessionRepository.createNewSession(
            presetId = 1,
            nameSnapshot = "SubhanAllah",
            target = 33
        )
        sessionRepository.incrementSessionCount(sessionId)
        sessionRepository.incrementSessionCount(sessionId)

        // Simulate app recreation by querying active session stream from repo
        val restoredActive = sessionRepository.getActiveSessionStream().first()
        assertNotNull(restoredActive)
        assertEquals(sessionId, restoredActive?.id)
        assertEquals(2, restoredActive?.count)
        assertEquals("SubhanAllah", restoredActive?.dhikrNameSnapshot)
    }
}
