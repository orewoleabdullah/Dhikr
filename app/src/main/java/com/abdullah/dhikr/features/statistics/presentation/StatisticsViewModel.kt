package com.abdullah.dhikr.features.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StatisticsViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.getAllSessionsStream().collect { allSessions ->
                computeStatistics(allSessions)
            }
        }
    }

    private fun computeStatistics(sessions: List<DhikrSessionEntity>) {
        val zoneId = ZoneId.systemDefault()
        val todayDate = LocalDate.now(zoneId)
        val startOfToday = todayDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val startOfWeek = todayDate.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val todaySessions = sessions.filter { (it.completedAt ?: it.lastUpdatedAt) >= startOfToday }
        val countToday = todaySessions.sumOf { it.count }
        val completedToday = todaySessions.count { it.status == DhikrSessionEntity.SESSION_STATUS_COMPLETED }

        val weekSessions = sessions.filter { (it.completedAt ?: it.lastUpdatedAt) >= startOfWeek }
        val countThisWeek = weekSessions.sumOf { it.count }

        val totalAllTime = sessions.sumOf { it.count }
        val completedAllTime = sessions.count { it.status == DhikrSessionEntity.SESSION_STATUS_COMPLETED }
        val avgPerSession = if (completedAllTime > 0) totalAllTime / completedAllTime else if (sessions.isNotEmpty()) totalAllTime / sessions.size else 0

        // Build last 7 days chart data
        val dayFormatter = DateTimeFormatter.ofPattern("EEE")
        val chartBars = (6 downTo 0).map { daysAgo ->
            val date = todayDate.minusDays(daysAgo.toLong())
            val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val dayTotal = sessions
                .filter { session ->
                    val timestamp = session.completedAt ?: session.lastUpdatedAt
                    timestamp in dayStart..<dayEnd
                }
                .sumOf { it.count }

            DailyActivityBar(
                dayLabel = date.format(dayFormatter),
                date = date,
                totalCount = dayTotal,
                isToday = date == todayDate
            )
        }

        _uiState.update {
            StatisticsUiState(
                countToday = countToday,
                completedSessionsToday = completedToday,
                countThisWeek = countThisWeek,
                averageCountPerSession = avgPerSession,
                totalAllTimeCount = totalAllTime,
                totalCompletedSessionsAllTime = completedAllTime,
                weeklyChartData = chartBars,
                hasData = sessions.isNotEmpty()
            )
        }
    }
}
