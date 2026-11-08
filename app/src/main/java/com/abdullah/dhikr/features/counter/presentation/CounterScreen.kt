package com.abdullah.dhikr.features.counter.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.dhikr.domain.model.AfterSalahChain
import com.abdullah.dhikr.domain.model.DhikrPreset
import com.abdullah.dhikr.ui.AppViewModelProvider
import com.abdullah.dhikr.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CounterScreen(
    viewModel: CounterViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current

    val triggerTapFeedback: () -> Unit = {
        FeedbackManager.performHapticVibration(context, uiState.isVibrationEnabled)
        FeedbackManager.playClickSound(view, uiState.isSoundEnabled)
    }

    val triggerCompletionFeedback: () -> Unit = {
        FeedbackManager.performHapticVibration(context, uiState.isVibrationEnabled, isCompletion = true)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CounterUiEvent.TriggerTapHaptic -> {
                    triggerTapFeedback()
                }
                is CounterUiEvent.TriggerCompletionHaptic -> {
                    triggerCompletionFeedback()
                }
                is CounterUiEvent.TriggerSoundEffect -> {
                    FeedbackManager.playClickSound(view, uiState.isSoundEnabled)
                }
            }
        }
    }

    // Main layout fitted for single-screen view with scroll safety
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Header & Dhikr Selector Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dhikr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SessionBadge(status = uiState.sessionStatus)
                    if (uiState.sessionStatus != SessionStatus.IDLE) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                triggerTapFeedback()
                                viewModel.finishSession()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Finish Session",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dhikr Selection Entry Point Card
            Surface(
                onClick = {
                    triggerTapFeedback()
                    viewModel.toggleDhikrSelection(true)
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.selectedDhikr.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Dhikr",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (uiState.selectedDhikr.translation.isNotEmpty()) {
                            Text(
                                text = uiState.selectedDhikr.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (uiState.selectedDhikr.arabicText.isNotEmpty()) {
                        ArabicText(
                            text = uiState.selectedDhikr.arabicText,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Mode Selection Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = !uiState.isAfterSalahMode && uiState.selectedDhikr.id == DhikrPreset.FREE_COUNTER.id,
                    onClick = {
                        triggerTapFeedback()
                        viewModel.selectDhikr(DhikrPreset.FREE_COUNTER)
                    },
                    label = {
                        Text(
                            "⚡ Quick Counter",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.isAfterSalahMode,
                    onClick = {
                        triggerTapFeedback()
                        viewModel.selectDhikr(DhikrPreset.AFTER_SALAH_PRESET)
                    },
                    label = {
                        Text(
                            "🕌 After-Salah (33x3)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // After-Salah 3-Step Progress Stepper Card
            if (uiState.isAfterSalahMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "After-Salah Tasbeeh (33x SubhanAllah ➔ 33x Alhamdulillah ➔ 33x Allahu Akbar)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AfterSalahChain.STEPS.forEachIndexed { index, step ->
                                val isCurrent = uiState.afterSalahStep == index
                                val isDone = uiState.afterSalahStep > index || (uiState.afterSalahStep == 2 && uiState.isTargetReached)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isDone -> MaterialTheme.colorScheme.secondary
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isDone) "✓ ${step.title}" else "${index + 1}. ${step.title}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isDone -> MaterialTheme.colorScheme.onSecondary
                                                isCurrent -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (isCurrent) "${uiState.currentCount}/33" else if (isDone) "33/33" else "0/33",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = when {
                                                isDone -> MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                                                isCurrent -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Target Progress Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val progressRatio = (uiState.currentCount.toFloat() / uiState.target.toFloat()).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progressRatio,
                    animationSpec = tween(durationMillis = 200),
                    label = "progress"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.currentCount} / ${uiState.target}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isTargetReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (uiState.isTargetReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                if (uiState.isTargetReached) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎉 Target reached! Keep going or start a new session.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Selector Chips (33, 100, 500, 1000, Custom)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(33, 100, 500, 1000).forEach { presetTarget ->
                        FilterChip(
                            selected = uiState.target == presetTarget,
                            onClick = {
                                triggerTapFeedback()
                                viewModel.setTarget(presetTarget)
                            },
                            label = { Text("$presetTarget", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    FilterChip(
                        selected = uiState.target !in listOf(33, 100, 500, 1000),
                        onClick = {
                            triggerTapFeedback()
                            viewModel.toggleCustomTargetDialog(true)
                        },
                        label = {
                            Text(
                                if (uiState.target !in listOf(33, 100, 500, 1000)) "Custom (${uiState.target})" else "Custom",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Middle Section: Prominent Circular Counting Button
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "buttonScale"
            )

            val borderColor by animateColorAsState(
                targetValue = if (uiState.isTargetReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 300),
                label = "borderColor"
            )

            val buttonBgColor by animateColorAsState(
                targetValue = if (uiState.isTargetReached) MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                animationSpec = tween(durationMillis = 300),
                label = "bgColor"
            )

            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .semantics {
                        role = androidx.compose.ui.semantics.Role.Button
                        contentDescription = "Main counter button. Current count ${uiState.currentCount}, target ${uiState.target}. Tap to increment count."
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 100.dp),
                        onClick = {
                            triggerTapFeedback()
                            viewModel.incrementCount()
                        }
                    ),
                color = buttonBgColor,
                shape = CircleShape,
                border = BorderStroke(3.dp, borderColor.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(175.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.2f))
                    ) {}

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.currentCount}",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                            color = if (uiState.isTargetReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TAP TO COUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Section: Undo & Reset Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DhikrIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo last count",
                    enabled = uiState.isUndoAvailable,
                    onClick = {
                        triggerTapFeedback()
                        viewModel.undoCount()
                    },
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Undo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.isUndoAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DhikrIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Reset counter",
                    onClick = {
                        triggerTapFeedback()
                        viewModel.showResetDialog(true)
                    },
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Reset Confirmation Dialog
    if (uiState.showResetDialog) {
        DhikrConfirmationDialog(
            onDismissRequest = { viewModel.showResetDialog(false) },
            onConfirm = { viewModel.resetCount() },
            title = "Reset Counter?",
            text = "Are you sure you want to reset your current count of ${uiState.currentCount}? This action cannot be undone.",
            confirmButtonText = "Reset",
            dismissButtonText = "Cancel"
        )
    }

    // Dhikr Selection Dialog (Popup directly over main screen)
    if (uiState.showDhikrSelection) {
        DhikrSelectionDialog(
            onDismissRequest = { viewModel.toggleDhikrSelection(false) },
            onSelectPreset = { preset ->
                triggerTapFeedback()
                viewModel.selectDhikr(preset)
            },
            onCustomDhikrClick = {
                triggerTapFeedback()
                viewModel.toggleCustomDhikrDialog(true)
            },
            onDeletePreset = { preset ->
                viewModel.deleteDhikrPreset(preset)
            },
            dhikrList = uiState.availableDhikrs,
            currentSelectedId = uiState.selectedDhikr.id
        )
    }

    // Custom Target Dialog
    if (uiState.showCustomTargetDialog) {
        CustomTargetDialog(
            onDismissRequest = { viewModel.toggleCustomTargetDialog(false) },
            onConfirm = { customTarget -> viewModel.setTarget(customTarget) }
        )
    }

    // Custom Dhikr Dialog
    if (uiState.showCustomDhikrDialog) {
        CustomDhikrDialog(
            onDismissRequest = { viewModel.toggleCustomDhikrDialog(false) },
            onConfirm = { title, arabic, translation, targetCount ->
                viewModel.setCustomDhikr(title, arabic, translation, targetCount)
            }
        )
    }
}
