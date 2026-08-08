package com.coconutchunks.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coconutchunks.app.ui.theme.CoconutBrown
import com.coconutchunks.app.ui.theme.CoconutBrownDark
import com.coconutchunks.app.ui.theme.CoconutBrownSoft
import com.coconutchunks.app.ui.theme.CoconutInk
import com.coconutchunks.app.ui.theme.CoconutSand

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
        HomeButton("Review", CoconutBrownDark, Color.White, onReviewClick)
        HomeButton("Add Chunk", CoconutBrown, Color.White, onAddChunkClick)
        HomeButton("Library", CoconutBrownSoft, Color.White, onLibraryClick)
        HomeButton("Settings", CoconutSand, CoconutInk, onSettingsClick)
    }
}

@Composable
private fun HomeButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 10.dp,
            bottomEnd = 22.dp,
            bottomStart = 10.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(vertical = 18.dp),
    ) {
        Text(text)
    }
}
