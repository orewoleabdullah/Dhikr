package com.abdullah.dhikr.data.local.dao

import androidx.room.*
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrSessionDao {
    @Query("SELECT * FROM dhikr_sessions WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY lastUpdatedAt DESC LIMIT 1")
    fun getActiveSessionStream(): Flow<DhikrSessionEntity?>

    @Query("SELECT * FROM dhikr_sessions WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY lastUpdatedAt DESC LIMIT 1")
    fun getActiveSession(): DhikrSessionEntity?

    @Query("SELECT * FROM dhikr_sessions ORDER BY lastUpdatedAt DESC")
    fun getAllSessionsStream(): Flow<List<DhikrSessionEntity>>

    @Query("SELECT * FROM dhikr_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC, lastUpdatedAt DESC")
    fun getAllCompletedSessionsStream(): Flow<List<DhikrSessionEntity>>

    @Query("SELECT * FROM dhikr_sessions WHERE lastUpdatedAt >= :startOfDay ORDER BY lastUpdatedAt DESC")
    fun getTodaySessionsStream(startOfDay: Long): Flow<List<DhikrSessionEntity>>

    @Query("SELECT * FROM dhikr_sessions WHERE lastUpdatedAt >= :startTime ORDER BY lastUpdatedAt ASC")
    fun getSessionsSinceStream(startTime: Long): Flow<List<DhikrSessionEntity>>

    @Query("SELECT * FROM dhikr_sessions WHERE id = :id")
    fun getSessionByIdStream(id: Long): Flow<DhikrSessionEntity?>

    @Query("SELECT * FROM dhikr_sessions WHERE id = :id")
    fun getSessionById(id: Long): DhikrSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: DhikrSessionEntity): Long

    @Update
    fun updateSession(session: DhikrSessionEntity): Int

    @Delete
    fun deleteSession(session: DhikrSessionEntity): Int

    @Query("UPDATE dhikr_sessions SET status = 'PAUSED' WHERE status = 'ACTIVE' AND id != :exceptSessionId")
    fun pauseOtherActiveSessions(exceptSessionId: Long)
}
