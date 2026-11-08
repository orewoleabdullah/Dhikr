package com.abdullah.dhikr.data.repository

import com.abdullah.dhikr.data.local.dao.DhikrSessionDao
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineSessionRepository(private val dhikrSessionDao: DhikrSessionDao) : SessionRepository {

    override fun getActiveSessionStream(): Flow<DhikrSessionEntity?> = dhikrSessionDao.getActiveSessionStream()

    override fun getAllSessionsStream(): Flow<List<DhikrSessionEntity>> = dhikrSessionDao.getAllSessionsStream()

    override fun getCompletedSessionsStream(): Flow<List<DhikrSessionEntity>> = dhikrSessionDao.getAllCompletedSessionsStream()

    override fun getTodaySessionsStream(startOfDay: Long): Flow<List<DhikrSessionEntity>> = dhikrSessionDao.getTodaySessionsStream(startOfDay)

    override fun getSessionsSinceStream(startTime: Long): Flow<List<DhikrSessionEntity>> = dhikrSessionDao.getSessionsSinceStream(startTime)

    override fun getSessionByIdStream(id: Long): Flow<DhikrSessionEntity?> = dhikrSessionDao.getSessionByIdStream(id)

    override suspend fun getActiveSession(): DhikrSessionEntity? {
        return withContext(Dispatchers.IO) {
            try {
                dhikrSessionDao.getActiveSession()
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun createNewSession(presetId: Int?, nameSnapshot: String, target: Int): Long {
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val newSession = DhikrSessionEntity(
                    presetId = null,
                    dhikrNameSnapshot = nameSnapshot,
                    count = 0,
                    target = if (target > 0) target else 33,
                    startedAt = now,
                    lastUpdatedAt = now,
                    status = DhikrSessionEntity.SESSION_STATUS_ACTIVE
                )
                val sessionId = dhikrSessionDao.insertSession(newSession)
                dhikrSessionDao.pauseOtherActiveSessions(sessionId)
                sessionId
            } catch (_: Exception) {
                -1L
            }
        }
    }

    override suspend fun incrementSessionCount(sessionId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext false
                if (session.count >= Int.MAX_VALUE) return@withContext false

                val newCount = session.count + 1
                val isCompleted = newCount >= session.target
                val now = System.currentTimeMillis()

                val updatedSession = session.copy(
                    count = newCount,
                    lastUpdatedAt = now,
                    status = if (isCompleted) DhikrSessionEntity.SESSION_STATUS_COMPLETED else DhikrSessionEntity.SESSION_STATUS_ACTIVE,
                    completedAt = if (isCompleted) now else session.completedAt
                )
                dhikrSessionDao.updateSession(updatedSession) > 0
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun undoSessionCount(sessionId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext false
                if (session.count <= 0) return@withContext false

                val newCount = session.count - 1
                val isCompleted = newCount >= session.target
                val now = System.currentTimeMillis()

                val updatedSession = session.copy(
                    count = newCount,
                    lastUpdatedAt = now,
                    status = if (isCompleted) DhikrSessionEntity.SESSION_STATUS_COMPLETED else DhikrSessionEntity.SESSION_STATUS_ACTIVE,
                    completedAt = if (isCompleted) session.completedAt else null
                )
                dhikrSessionDao.updateSession(updatedSession) > 0
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun pauseSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext
                if (session.status == DhikrSessionEntity.SESSION_STATUS_ACTIVE) {
                    dhikrSessionDao.updateSession(
                        session.copy(
                            status = DhikrSessionEntity.SESSION_STATUS_PAUSED,
                            lastUpdatedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    override suspend fun resumeSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext
                dhikrSessionDao.pauseOtherActiveSessions(sessionId)
                val isCompleted = session.count >= session.target
                dhikrSessionDao.updateSession(
                    session.copy(
                        status = if (isCompleted) DhikrSessionEntity.SESSION_STATUS_COMPLETED else DhikrSessionEntity.SESSION_STATUS_ACTIVE,
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override suspend fun completeSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext
                val now = System.currentTimeMillis()
                dhikrSessionDao.updateSession(
                    session.copy(
                        status = DhikrSessionEntity.SESSION_STATUS_COMPLETED,
                        lastUpdatedAt = now,
                        completedAt = now
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override suspend fun deleteSession(session: DhikrSessionEntity) {
        withContext(Dispatchers.IO) {
            try {
                dhikrSessionDao.deleteSession(session)
            } catch (_: Exception) {}
        }
    }

    override suspend fun resetSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val session = dhikrSessionDao.getSessionById(sessionId) ?: return@withContext
                val now = System.currentTimeMillis()
                dhikrSessionDao.updateSession(
                    session.copy(
                        count = 0,
                        status = DhikrSessionEntity.SESSION_STATUS_ACTIVE,
                        lastUpdatedAt = now,
                        completedAt = null
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
