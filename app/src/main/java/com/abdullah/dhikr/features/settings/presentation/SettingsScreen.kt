package com.abdullah.dhikr.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.dhikr.data.preferences.ThemeMode
import com.abdullah.dhikr.ui.AppViewModelProvider
import com.abdullah.dhikr.ui.components.DhikrConfirmationDialog
import com.abdullah.dhikr.ui.components.SectionHeader
import com.abdullah.dhikr.ui.components.SettingsRow
import com.abdullah.dhikr.ui.components.SettingsToggleRow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isCompletionFeedbackEnabled by viewModel.isCompletionFeedbackEnabled.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showDataStorageInfoDialog by remember { mutableStateOf(false) }
    var showDeleteAllDataConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showAcknowledgementsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Top Header (Fixed at top)
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SectionHeader(
                    title = "Settings",
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            }
        }

        // Scrollable Options Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Appearance Section
            SectionHeader(title = "Appearance")

            val themeDisplayValue = when (themeMode) {
                ThemeMode.SYSTEM -> "System Default"
                ThemeMode.LIGHT -> "Light Theme"
                ThemeMode.DARK -> "Dark Theme"
            }

            SettingsRow(
                label = "Theme Mode",
                icon = Icons.Default.Palette,
                value = themeDisplayValue,
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Counting Preferences Section
            SectionHeader(title = "Counting Preferences")

            SettingsToggleRow(
                label = "Haptic Feedback (Vibration)",
                icon = Icons.Default.Vibration,
                checked = isVibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration(it) }
            )

            SettingsToggleRow(
                label = "Sound Feedback",
                icon = Icons.Default.VolumeUp,
                checked = isSoundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )

            SettingsToggleRow(
                label = "Target Completion Alert",
                icon = Icons.Default.CheckCircle,
                checked = isCompletionFeedbackEnabled,
                onCheckedChange = { viewModel.toggleCompletionFeedback(it) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Data Management Section
            SectionHeader(title = "Data Management")

            SettingsRow(
                label = "Local Storage Info",
                icon = Icons.Default.Storage,
                value = "Offline & Local",
                onClick = { showDataStorageInfoDialog = true }
            )

            SettingsRow(
                label = "Reset All Application Data",
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteAllDataConfirmDialog = true }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // About Section
            SectionHeader(title = "About")

            SettingsRow(
                label = "About Dhikr",
                icon = Icons.Default.Info,
                value = "1.0.0",
                onClick = { showAboutAppDialog = true }
            )

            SettingsRow(
                label = "Privacy Information",
                icon = Icons.Default.PrivacyTip,
                onClick = { showPrivacyPolicyDialog = true }
            )

            SettingsRow(
                label = "Acknowledgements",
                icon = Icons.Default.Code,
                onClick = { showAcknowledgementsDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Appearance", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> "System Default"
                            ThemeMode.LIGHT -> "Light Theme"
                            ThemeMode.DARK -> "Dark Theme"
                        }
                        TextButton(
                            onClick = {
                                viewModel.setThemeMode(mode)
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Local Storage Info Dialog
    if (showDataStorageInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDataStorageInfoDialog = false },
            icon = { Icon(Icons.Default.Storage, contentDescription = null) },
            title = { Text("Local Data Storage", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "All your Dhikr counting sessions, custom presets, daily statistics, and preferences are stored 100% locally on your device in an encrypted Room SQLite database.\n\nQibla direction calculations and GPS location queries are processed strictly on-device and are never uploaded or saved to external servers.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showDataStorageInfoDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Delete All Data Double-Confirmation Dialog
    if (showDeleteAllDataConfirmDialog) {
        DhikrConfirmationDialog(
            onDismissRequest = { showDeleteAllDataConfirmDialog = false },
            onConfirm = {
                viewModel.deleteAllApplicationData()
                showDeleteAllDataConfirmDialog = false
            },
            title = "Reset All Application Data?",
            text = "This action will permanently delete all your recorded Dhikr sessions, history records, custom presets, and daily statistics.\n\nBuilt-in presets and application preferences will be reset to default settings.\n\nThis action CANNOT be undone.",
            confirmButtonText = "Delete All Data",
            dismissButtonText = "Cancel"
        )
    }

    // About App Dialog
    if (showAboutAppDialog) {
        AlertDialog(
            onDismissRequest = { showAboutAppDialog = false },
            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Dhikr & Qibla Finder v1.0.0", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "An Islamic digital tasbeeh counter & Qibla finder designed for daily dhikr, target tracking, session history, and real-time offline Qibla direction.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Built with modern Jetpack Compose, Material 3 & Kotlin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutAppDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            title = { Text("Privacy Policy", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "Dhikr Counter is committed to 100% user privacy:\n\n• Zero Data Collection: We do not collect, track, or share any personal information.\n• 100% Offline: The app functions entirely without internet access (android.permission.INTERNET is omitted).\n• Location Privacy: Location access is used strictly on-device to calculate Qibla direction and is never transmitted anywhere.\n• Local Control: All data remains stored exclusively on your physical device.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Acknowledgements Dialog
    if (showAcknowledgementsDialog) {
        AlertDialog(
            onDismissRequest = { showAcknowledgementsDialog = false },
            icon = { Icon(Icons.Default.Code, contentDescription = null) },
            title = { Text("Acknowledgements", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Android Open Source Project (AOSP)", fontWeight = FontWeight.SemiBold)
                    Text("• Jetpack Compose & Material 3 (Google)", fontWeight = FontWeight.SemiBold)
                    Text("• Room Persistence Library & DataStore (Google)", fontWeight = FontWeight.SemiBold)
                    Text("• Android Hardware Sensors & Location APIs (Google)", fontWeight = FontWeight.SemiBold)
                    Text("• Kotlin Coroutines & StateFlow (JetBrains)", fontWeight = FontWeight.SemiBold)
                    Text("• Material Design Icons Extended (Google)", fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAcknowledgementsDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
