package com.abdullah.dhikr.features.statistics.presentation

import java.time.LocalDate

data class DailyActivityBar(
    val dayLabel: String,
    val date: LocalDate,
    val totalCount: Int,
    val isToday: Boolean
)

/**
 * UI State for the Dhikr Statistics Screen.
 */
data class StatisticsUiState(
    val countToday: Int = 0,
    val completedSessionsToday: Int = 0,
    val countThisWeek: Int = 0,
    val averageCountPerSession: Int = 0,
    val totalAllTimeCount: Int = 0,
    val totalCompletedSessionsAllTime: Int = 0,
    val weeklyChartData: List<DailyActivityBar> = emptyList(),
    val hasData: Boolean = false
)
