package com.coconutchunks.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.coconutchunks.app.data.ChunkDatabase
import com.coconutchunks.app.data.ChunkRepository
import com.coconutchunks.app.ui.add.AddChunkScreen
import com.coconutchunks.app.ui.add.AddChunkViewModel
import com.coconutchunks.app.ui.edit.EditChunkScreen
import com.coconutchunks.app.ui.edit.EditChunkViewModel
import com.coconutchunks.app.ui.home.HomeScreen
import com.coconutchunks.app.ui.library.LibraryScreen
import com.coconutchunks.app.ui.library.LibraryViewModel
import com.coconutchunks.app.ui.review.ReviewScreen
import com.coconutchunks.app.ui.review.ReviewViewModel
import com.coconutchunks.app.ui.settings.SettingsScreen
import com.coconutchunks.app.ui.settings.SettingsViewModel

@Composable
fun CoconutChunksApp() {
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext
    val repository = remember(appContext) {
        ChunkRepository(
            ChunkDatabase.getInstance(appContext).chunkDao()
        )
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onReviewClick = { navController.navigate(Routes.REVIEW) },
                onAddChunkClick = { navController.navigate(Routes.ADD) },
                onLibraryClick = { navController.navigate(Routes.LIBRARY) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.ADD) {
            val addViewModel: AddChunkViewModel = viewModel(
                factory = AddChunkViewModel.factory(repository),
            )

            AddChunkScreen(
                viewModel = addViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LIBRARY) {
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.factory(repository),
            )

            LibraryScreen(
                viewModel = libraryViewModel,
                onBack = { navController.popBackStack() },
                onChunkClick = { chunkId ->
                    navController.navigate(Routes.edit(chunkId))
                },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument(Routes.EDIT_ARGUMENT) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val chunkId = backStackEntry.arguments
                ?.getLong(Routes.EDIT_ARGUMENT)
                ?: return@composable

            val editViewModel: EditChunkViewModel = viewModel(
                factory = EditChunkViewModel.factory(
                    chunkId = chunkId,
                    repository = repository,
                ),
            )

            EditChunkScreen(
                viewModel = editViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.REVIEW) {
            val reviewViewModel: ReviewViewModel = viewModel(
                factory = ReviewViewModel.factory(repository),
            )

            ReviewScreen(
                viewModel = reviewViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(repository),
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
