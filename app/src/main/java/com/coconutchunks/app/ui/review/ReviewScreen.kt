package com.coconutchunks.app.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.review.REVIEW_ALL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isUpdating,
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
        when (state.phase) {
            ReviewPhase.SETUP -> ReviewSetup(
                state = state,
                onSelectGroup = viewModel::selectGroup,
                onIncludeMasteredChanged = viewModel::setIncludeMastered,
                onStart = viewModel::startReview,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            ReviewPhase.ACTIVE -> ReviewSession(
                state = state,
                onReveal = viewModel::revealExamples,
                onSpecial = viewModel::markSpecial,
                onNext = viewModel::next,
                onMastered = viewModel::markMastered,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            ReviewPhase.COMPLETE -> ReviewComplete(
                state = state,
                onFinish = onBack,
                onReviewAgain = viewModel::reviewAgain,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ReviewSetup(
    state: ReviewUiState,
    onSelectGroup: (String) -> Unit,
    onIncludeMasteredChanged: (Boolean) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var groupMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Choose what to review",
            fontWeight = FontWeight.SemiBold,
        )

        Column {
            TextButton(
                onClick = { groupMenuExpanded = true },
                enabled = !state.isLoadingSession,
            ) {
                Text(
                    if (state.selectedGroup == REVIEW_ALL) {
                        "Group: All"
                    } else {
                        "Group: ${state.selectedGroup}"
                    }
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Choose group",
                )
            }

            DropdownMenu(
                expanded = groupMenuExpanded,
                onDismissRequest = { groupMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("All") },
                    onClick = {
                        onSelectGroup(REVIEW_ALL)
                        groupMenuExpanded = false
                    },
                )

                state.availableGroups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = {
                            onSelectGroup(group)
                            groupMenuExpanded = false
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = state.includeMastered,
                onCheckedChange = onIncludeMasteredChanged,
                enabled = !state.isLoadingSession,
            )
            Text(
                text = "Include mastered chunks",
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        state.message?.let { message ->
            Text(message)
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoadingSession,
        ) {
            Text(if (state.isLoadingSession) "Loading…" else "Start Review")
        }
    }
}

@Composable
private fun ReviewSession(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onSpecial: () -> Unit,
    onNext: () -> Unit,
    onMastered: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chunk = state.currentChunk ?: return

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ReviewCard(
            chunk = chunk,
            revealed = state.revealed,
            onReveal = onReveal,
        )

        state.message?.let { message ->
            Text(message)
        }

        if (state.revealed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onSpecial,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isUpdating,
                ) {
                    Text("Special")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isUpdating,
                ) {
                    Text("Next")
                }

                OutlinedButton(
                    onClick = onMastered,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isUpdating,
                ) {
                    Text("Mastered")
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    chunk: ChunkEntity,
    revealed: Boolean,
    onReveal: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !revealed,
                onClick = onReveal,
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = chunk.chunkText,
                fontWeight = FontWeight.Bold,
            )

            Text(chunk.groupName)
            Text(chunk.status.name)

            if (!revealed) {
                Text("Tap to reveal examples")
            } else {
                nonEmptyExamples(chunk).forEachIndexed { index, example ->
                    Text("${index + 1}. $example")
                }
            }
        }
    }
}

fun nonEmptyExamples(chunk: ChunkEntity): List<String> =
    listOf(
        chunk.example1,
        chunk.example2,
        chunk.example3,
    ).map { it.trim() }
        .filter { it.isNotEmpty() }

@Composable
private fun ReviewComplete(
    state: ReviewUiState,
    onFinish: () -> Unit,
    onReviewAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Review complete",
            fontWeight = FontWeight.Bold,
        )

        Text("Reviewed: ${state.reviewedCount}")
        Text("Marked Special: ${state.specialCount}")
        Text("Marked Mastered: ${state.masteredCount}")

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Finish")
        }

        OutlinedButton(
            onClick = onReviewAgain,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Review Again")
        }
    }
}
