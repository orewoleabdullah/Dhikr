package com.abdullah.dhikr.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a Dhikr session in Room database.
 * Retains [dhikrNameSnapshot] so historical sessions remain fully preserved
 * even if the custom preset is modified or deleted.
 */
@Entity(
    tableName = "dhikr_sessions",
    indices = [
        Index(value = ["presetId"]),
        Index(value = ["status"]),
        Index(value = ["lastUpdatedAt"])
    ]
)
data class DhikrSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val presetId: Int? = null,
    val dhikrNameSnapshot: String,
    val count: Int = 0,
    val target: Int = 33,
    val startedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: String = SESSION_STATUS_ACTIVE // ACTIVE, PAUSED, COMPLETED
) {
    companion object {
        const val SESSION_STATUS_ACTIVE = "ACTIVE"
        const val SESSION_STATUS_PAUSED = "PAUSED"
        const val SESSION_STATUS_COMPLETED = "COMPLETED"
    }
}
