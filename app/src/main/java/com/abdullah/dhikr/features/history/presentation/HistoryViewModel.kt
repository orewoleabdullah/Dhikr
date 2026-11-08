package com.abdullah.dhikr.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.getAllSessionsStream().collect { sessions ->
                val grouped = groupSessionsByDate(sessions)
                _uiState.update { it.copy(sessionsGroupedByDate = grouped) }
            }
        }
    }

    fun requestDeleteSession(session: DhikrSessionEntity) {
        _uiState.update { it.copy(deleteConfirmationSession = session) }
    }

    fun cancelDeleteSession() {
        _uiState.update { it.copy(deleteConfirmationSession = null) }
    }

    fun confirmDeleteSession() {
        val toDelete = _uiState.value.deleteConfirmationSession ?: return
        viewModelScope.launch {
            sessionRepository.deleteSession(toDelete)
        }
        cancelDeleteSession()
    }

    fun selectSessionForDetails(session: DhikrSessionEntity?) {
        _uiState.update { it.copy(selectedDetailSession = session) }
    }

    private fun groupSessionsByDate(sessions: List<DhikrSessionEntity>): Map<String, List<DhikrSessionEntity>> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val yesterday = today.minusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

        return sessions.groupBy { session ->
            val timestamp = session.completedAt ?: session.lastUpdatedAt
            val sessionDate = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()

            when (sessionDate) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> sessionDate.format(dateFormatter)
            }
        }
    }
}
