package com.coconutchunks.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coconutchunks.app.ui.CoconutApp
import com.coconutchunks.app.ui.CoconutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoconutTheme {
                val vm: AppViewModel = viewModel()
                CoconutApp(vm)
            }
        }
    }
}
