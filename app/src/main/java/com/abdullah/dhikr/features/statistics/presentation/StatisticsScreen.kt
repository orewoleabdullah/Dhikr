package com.abdullah.dhikr.features.statistics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.dhikr.ui.AppViewModelProvider
import com.abdullah.dhikr.ui.components.*

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onStartDhikrClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "Statistics & Analytics",
            modifier = Modifier.padding(top = 16.dp)
        )

        if (!uiState.hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.BarChart,
                        message = "No Dhikr statistics recorded yet.\nStart counting to track your progress over time!"
                    )
                    if (onStartDhikrClick != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DhikrPrimaryButton(
                            text = "Start Dhikr Counter",
                            onClick = onStartDhikrClick,
                            modifier = Modifier.width(220.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticsCard(
                        label = "Today's Count",
                        value = "${uiState.countToday}",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                    StatisticsCard(
                        label = "Completed Today",
                        value = "${uiState.completedSessionsToday}",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticsCard(
                        label = "This Week",
                        value = "${uiState.countThisWeek}",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    StatisticsCard(
                        label = "Avg / Session",
                        value = "${uiState.averageCountPerSession}",
                        icon = Icons.Default.QueryStats,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Weekly Activity Bar Chart
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Weekly Activity (Last 7 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val maxDayCount = uiState.weeklyChartData.maxOfOrNull { it.totalCount }?.coerceAtLeast(1) ?: 1

                        // Bars Area
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.weeklyChartData.forEach { bar ->
                                val targetFraction = (bar.totalCount.toFloat() / maxDayCount.toFloat()).coerceIn(0.08f, 1f)
                                val animatedHeightFraction by animateFloatAsState(
                                    targetValue = if (bar.totalCount > 0) targetFraction else 0.05f,
                                    animationSpec = tween(durationMillis = 500),
                                    label = "barHeight"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    if (bar.totalCount > 0) {
                                        Text(
                                            text = "${bar.totalCount}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(animatedHeightFraction)
                                            .width(20.dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (bar.isToday) MaterialTheme.colorScheme.secondary
                                                else if (bar.totalCount > 0) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Day Labels Beneath Each Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.weeklyChartData.forEach { bar ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bar.dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (bar.isToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                // All Time Summary Card
                DhikrCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "All-Time Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Dhikr Count:", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.totalAllTimeCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Completed Sessions:", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.totalCompletedSessionsAllTime}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
