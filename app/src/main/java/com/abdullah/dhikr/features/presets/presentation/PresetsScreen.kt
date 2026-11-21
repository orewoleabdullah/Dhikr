package com.abdullah.dhikr.features.presets.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.ui.AppViewModelProvider
import com.abdullah.dhikr.ui.components.*

@Composable
fun PresetsScreen(
    viewModel: PresetsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onDhikrSelected: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Custom Dhikr") },
                text = { Text("Add Dhikr") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(
                title = "Dhikr Presets",
                modifier = Modifier.padding(top = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search by name, Arabic, or translation...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            // Filter Tabs (All, Built-in, Custom)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                PresetFilterTab.entries.forEach { tab ->
                    val isSelected = uiState.selectedFilterTab == tab
                    val label = when (tab) {
                        PresetFilterTab.ALL -> "All"
                        PresetFilterTab.BUILT_IN -> "Built-in"
                        PresetFilterTab.CUSTOM -> "Custom"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSelectFilterTab(tab) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of Dhikr items
            if (uiState.dhikrList.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Search,
                    message = if (uiState.searchQuery.isNotEmpty()) "No Dhikr phrases match '${uiState.searchQuery}'." else "No Dhikr items found."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(uiState.dhikrList, key = { it.id }) { item ->
                        val isSelected = item.title.equals(uiState.activeSelectedTitle, ignoreCase = true)
                        DhikrPresetCard(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.selectDhikr(item)
                                onDhikrSelected?.invoke()
                            },
                            onEdit = { viewModel.openEditDialog(item) },
                            onDelete = { viewModel.requestDeleteDhikr(item) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Custom Dhikr Dialog
    if (uiState.isAddEditDialogVisible) {
        AddEditDhikrDialog(
            editingItem = uiState.editingDhikr,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { title, arabic, translation, description, target ->
                viewModel.saveDhikr(title, arabic, translation, description, target)
            }
        )
    }

    // Delete Confirmation Dialog for Custom Dhikrs
    uiState.deleteConfirmationDhikr?.let { dhikrToDelete ->
        DhikrConfirmationDialog(
            onDismissRequest = { viewModel.cancelDeleteDhikr() },
            onConfirm = { viewModel.confirmDeleteDhikr() },
            title = "Delete Custom Dhikr?",
            text = "Are you sure you want to delete '${dhikrToDelete.title}'? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel"
        )
    }
}

@Composable
fun DhikrPresetCard(
    item: DhikrItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title, Selected Badge, Type Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (item.isCustom) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (item.isCustom) "Custom" else "Built-in",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isCustom) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Arabic Text
            if (item.arabicText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ArabicText(
                    text = item.arabicText,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Translation & Description
            if (item.translation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Target badge and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Default Target: ${item.defaultTarget}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (item.isCustom) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Custom Dhikr",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Custom Dhikr",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Protected built-in preset",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Built-in Preset",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditDhikrDialog(
    editingItem: DhikrItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, arabic: String, translation: String, description: String?, defaultTarget: Int) -> Unit
) {
    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var arabic by remember { mutableStateOf(editingItem?.arabicText ?: "") }
    var translation by remember { mutableStateOf(editingItem?.translation ?: "") }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var targetText by remember { mutableStateOf(editingItem?.defaultTarget?.toString() ?: "33") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingItem != null) "Edit Custom Dhikr" else "Add Custom Dhikr",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Dhikr Name / Transliteration *") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text("Arabic Text (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text("English Translation (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        targetText = input.filter { it.isDigit() }
                        targetError = null
                    },
                    label = { Text("Default Target *") },
                    isError = targetError != null,
                    supportingText = targetError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var isValid = true
                    if (title.isBlank()) {
                        titleError = "Dhikr name cannot be empty"
                        isValid = false
                    }
                    val targetNum = targetText.toIntOrNull()
                    if (targetNum == null || targetNum <= 0) {
                        targetError = "Enter a valid target (> 0)"
                        isValid = false
                    }

                    if (isValid && targetNum != null) {
                        onSave(title, arabic, translation, description.ifBlank { null }, targetNum)
                    }
                }
            ) {
                Text(
                    text = if (editingItem != null) "Update" else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
