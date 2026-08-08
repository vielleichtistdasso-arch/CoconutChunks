package com.coconutchunks.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReviewClick: () -> Unit,
    onAddChunkClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coconut Chunks") },
            )
        },
    ) { innerPadding ->
        HomeContent(
            paddingValues = innerPadding,
            onReviewClick = onReviewClick,
            onAddChunkClick = onAddChunkClick,
            onLibraryClick = onLibraryClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun HomeContent(
    paddingValues: PaddingValues,
    onReviewClick: () -> Unit,
    onAddChunkClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeButton("Review", onReviewClick)
        HomeButton("Add Chunk", onAddChunkClick)
        HomeButton("Library", onLibraryClick)
        HomeButton("Settings", onSettingsClick)
    }
}

@Composable
private fun HomeButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 18.dp),
    ) {
        Text(text)
    }
}
