package com.abdullah.dhikr.features.history.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.features.counter.presentation.SessionStatus
import com.abdullah.dhikr.ui.AppViewModelProvider
import com.abdullah.dhikr.ui.components.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onStartDhikrClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SectionHeader(
            title = "Session History",
            modifier = Modifier.padding(top = 16.dp)
        )

        if (uiState.sessionsGroupedByDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.History,
                        message = "No Dhikr history recorded yet.\nStart counting to build your daily habit!"
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
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                uiState.sessionsGroupedByDate.forEach { (dateHeader, sessions) ->
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }

                    items(sessions, key = { it.id }) { session ->
                        DhikrHistoryCard(
                            session = session,
                            onClick = { viewModel.selectSessionForDetails(session) },
                            onDelete = { viewModel.requestDeleteSession(session) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    uiState.deleteConfirmationSession?.let { session ->
        DhikrConfirmationDialog(
            onDismissRequest = { viewModel.cancelDeleteSession() },
            onConfirm = { viewModel.confirmDeleteSession() },
            title = "Delete Session Record?",
            text = "Are you sure you want to delete this historical record for '${session.dhikrNameSnapshot}' (Count: ${session.count})?",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel"
        )
    }

    // Session Details Dialog
    uiState.selectedDetailSession?.let { session ->
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a, MMM d, yyyy")
        val zoneId = ZoneId.systemDefault()
        val startTimeFormatted = Instant.ofEpochMilli(session.startedAt).atZone(zoneId).format(timeFormatter)
        val completedTimeFormatted = session.completedAt?.let {
            Instant.ofEpochMilli(it).atZone(zoneId).format(timeFormatter)
        } ?: Instant.ofEpochMilli(session.lastUpdatedAt).atZone(zoneId).format(timeFormatter)

        AlertDialog(
            onDismissRequest = { viewModel.selectSessionForDetails(null) },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(session.dhikrNameSnapshot, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Achieved Count:", style = MaterialTheme.typography.bodyMedium)
                        Text("${session.count} / ${session.target}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status:", style = MaterialTheme.typography.bodyMedium)
                        Text(session.status, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Started:", style = MaterialTheme.typography.bodySmall)
                        Text(startTimeFormatted, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Active:", style = MaterialTheme.typography.bodySmall)
                        Text(completedTimeFormatted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.selectSessionForDetails(null) }) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun DhikrHistoryCard(
    session: DhikrSessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val zoneId = ZoneId.systemDefault()
    val formattedTime = Instant.ofEpochMilli(session.completedAt ?: session.lastUpdatedAt)
        .atZone(zoneId)
        .format(timeFormatter)

    val sessionStatusEnum = when (session.status) {
        DhikrSessionEntity.SESSION_STATUS_COMPLETED -> SessionStatus.COMPLETED
        DhikrSessionEntity.SESSION_STATUS_PAUSED -> SessionStatus.IDLE
        else -> SessionStatus.ACTIVE
    }

    DhikrCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.dhikrNameSnapshot,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SessionBadge(status = sessionStatusEnum)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Time: $formattedTime • Target: ${session.target}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete session record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
