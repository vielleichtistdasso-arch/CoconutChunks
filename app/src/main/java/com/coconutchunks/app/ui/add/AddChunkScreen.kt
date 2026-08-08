package com.coconutchunks.app.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChunkScreen(
    viewModel: AddChunkViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState
    val existingGroups by viewModel.existingGroups.collectAsState()
    var groupMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Chunk") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isSaving,
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                enabled = !state.isSaving,
            )

            OutlinedTextField(
                value = state.example1,
                onValueChange = viewModel::updateExample1,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Example 1") },
                enabled = !state.isSaving,
            )

            OutlinedTextField(
                value = state.example2,
                onValueChange = viewModel::updateExample2,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Example 2") },
                enabled = !state.isSaving,
            )

            OutlinedTextField(
                value = state.example3,
                onValueChange = viewModel::updateExample3,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Example 3") },
                enabled = !state.isSaving,
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = state.groupName,
                    onValueChange = viewModel::updateGroupName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Group") },
                    placeholder = { Text("Ungrouped") },
                    enabled = !state.isSaving,
                    trailingIcon = {
                        if (existingGroups.isNotEmpty()) {
                            IconButton(
                                onClick = { groupMenuExpanded = true },
                                enabled = !state.isSaving,
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
                    existingGroups.forEach { group ->
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

            state.errorMessage?.let { message ->
                Text(message)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving,
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { viewModel.save(onSaved = onBack) },
                    modifier = Modifier.weight(1f),
                    enabled = state.canSave,
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save")
                }
            }
        }
    }
}
