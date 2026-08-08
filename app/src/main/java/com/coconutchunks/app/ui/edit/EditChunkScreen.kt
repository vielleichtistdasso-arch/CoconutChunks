package com.coconutchunks.app.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coconutchunks.app.data.ChunkStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChunkScreen(
    viewModel: EditChunkViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var groupMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) {
                    showDeleteConfirmation = false
                }
            },
            title = { Text("Delete this chunk?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(
                            onDeleted = {
                                showDeleteConfirmation = false
                                onBack()
                            }
                        )
                    },
                    enabled = !state.isDeleting,
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    enabled = !state.isDeleting,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Chunk") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isSaving && !state.isDeleting,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Text(
                    text = "Loading…",
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp),
                )
            }

            state.chunk == null -> {
                Text(
                    text = "Chunk not found.",
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.chunkText,
                        onValueChange = viewModel::updateChunkText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Chunk") },
                        enabled = !state.isSaving && !state.isDeleting,
                    )

                    OutlinedTextField(
                        value = state.example1,
                        onValueChange = viewModel::updateExample1,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Example 1") },
                        enabled = !state.isSaving && !state.isDeleting,
                    )

                    OutlinedTextField(
                        value = state.example2,
                        onValueChange = viewModel::updateExample2,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Example 2") },
                        enabled = !state.isSaving && !state.isDeleting,
                    )

                    OutlinedTextField(
                        value = state.example3,
                        onValueChange = viewModel::updateExample3,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Example 3") },
                        enabled = !state.isSaving && !state.isDeleting,
                    )

                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.groupName,
                            onValueChange = viewModel::updateGroupName,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Group") },
                            enabled = !state.isSaving && !state.isDeleting,
                            trailingIcon = {
                                if (state.existingGroups.isNotEmpty()) {
                                    IconButton(
                                        onClick = { groupMenuExpanded = true },
                                        enabled = !state.isSaving && !state.isDeleting,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Choose existing group",
                                        )
                                    }
                                }
                            },
                        )

                        DropdownMenu(
                            expanded = groupMenuExpanded,
                            onDismissRequest = { groupMenuExpanded = false },
                        ) {
                            state.existingGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group) },
                                    onClick = {
                                        viewModel.updateGroupName(group)
                                        groupMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { statusMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving && !state.isDeleting,
                        ) {
                            Text("Status: ${state.status.name}")
                        }

                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false },
                        ) {
                            ChunkStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        viewModel.updateStatus(status)
                                        statusMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    state.errorMessage?.let { message ->
                        Text(message)
                    }

                    Button(
                        onClick = { viewModel.save(onSaved = onBack) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSave,
                    ) {
                        Text(if (state.isSaving) "Saving…" else "Save Changes")
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving && !state.isDeleting,
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
