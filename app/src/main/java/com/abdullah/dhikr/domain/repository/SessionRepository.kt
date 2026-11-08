package com.abdullah.dhikr.domain.repository

import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Dhikr counting sessions.
 */
interface SessionRepository {
    /**
     * Stream of the current active session (status ACTIVE or PAUSED).
     */
    fun getActiveSessionStream(): Flow<DhikrSessionEntity?>

    /**
     * Stream of all sessions (active, paused, completed).
     */
    fun getAllSessionsStream(): Flow<List<DhikrSessionEntity>>

    /**
     * Stream of all completed sessions.
     */
    fun getCompletedSessionsStream(): Flow<List<DhikrSessionEntity>>

    /**
     * Stream of today's sessions (updated since startOfDay).
     */
    fun getTodaySessionsStream(startOfDay: Long): Flow<List<DhikrSessionEntity>>

    /**
     * Stream of sessions since startTime.
     */
    fun getSessionsSinceStream(startTime: Long): Flow<List<DhikrSessionEntity>>

    /**
     * Stream of a specific session by ID.
     */
    fun getSessionByIdStream(id: Long): Flow<DhikrSessionEntity?>

    /**
     * Get current active session.
     */
    suspend fun getActiveSession(): DhikrSessionEntity?

    /**
     * Create a new session and set it as active (pausing other active sessions).
     */
    suspend fun createNewSession(presetId: Int?, nameSnapshot: String, target: Int): Long

    /**
     * Increment current count by 1 for the session.
     */
    suspend fun incrementSessionCount(sessionId: Long): Boolean

    /**
     * Undo last increment for the session (cannot go below 0).
     */
    suspend fun undoSessionCount(sessionId: Long): Boolean

    /**
     * Pause an active session.
     */
    suspend fun pauseSession(sessionId: Long)

    /**
     * Resume a paused session.
     */
    suspend fun resumeSession(sessionId: Long)

    /**
     * Mark a session as completed.
     */
    suspend fun completeSession(sessionId: Long)

    /**
     * Delete a session record.
     */
    suspend fun deleteSession(session: DhikrSessionEntity)

    /**
     * Reset a session count to 0 and set status to ACTIVE.
     */
    suspend fun resetSession(sessionId: Long)
}
