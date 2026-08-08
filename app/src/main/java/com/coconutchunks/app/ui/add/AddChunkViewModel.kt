package com.coconutchunks.app.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddChunkUiState(
    val chunkText: String = "",
    val example1: String = "",
    val example2: String = "",
    val example3: String = "",
    val groupName: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean
        get() = chunkText.isNotBlank() && !isSaving
}

class AddChunkViewModel(
    private val repository: ChunkRepository,
) : ViewModel() {

    var uiState by mutableStateOf(AddChunkUiState())
        private set

    val existingGroups: StateFlow<List<String>> =
        repository.observeGroups().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun updateChunkText(value: String) {
        uiState = uiState.copy(
            chunkText = value,
            errorMessage = null,
        )
    }

    fun updateExample1(value: String) {
        uiState = uiState.copy(example1 = value)
    }

    fun updateExample2(value: String) {
        uiState = uiState.copy(example2 = value)
    }

    fun updateExample3(value: String) {
        uiState = uiState.copy(example3 = value)
    }

    fun updateGroupName(value: String) {
        uiState = uiState.copy(
            groupName = value,
            errorMessage = null,
        )
    }

    fun save(onSaved: () -> Unit) {
        if (!uiState.canSave) return

        val snapshot = uiState
        uiState = uiState.copy(
            isSaving = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                repository.insert(
                    ChunkEntity(
                        chunkText = snapshot.chunkText.trim(),
                        example1 = snapshot.example1.trim(),
                        example2 = snapshot.example2.trim(),
                        example3 = snapshot.example3.trim(),
                        groupName = snapshot.groupName.trim().ifEmpty { "Ungrouped" },
                        createdAt = now,
                        updatedAt = now,
                    )
                )

                uiState = uiState.copy(isSaving = false)
                onSaved()
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Could not save.",
                )
            }
        }
    }

    companion object {
        fun factory(repository: ChunkRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(AddChunkViewModel::class.java))
                    return AddChunkViewModel(repository) as T
                }
            }
    }
}
