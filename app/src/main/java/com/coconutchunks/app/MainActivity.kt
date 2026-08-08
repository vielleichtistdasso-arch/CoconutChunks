package com.coconutchunks.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.coconutchunks.app.ui.navigation.CoconutChunksApp
import com.coconutchunks.app.ui.theme.CoconutChunksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CoconutChunksTheme {
                CoconutChunksApp()
            }
        }
    }
}
