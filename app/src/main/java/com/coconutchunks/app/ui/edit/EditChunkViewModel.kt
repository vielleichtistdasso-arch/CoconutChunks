package com.coconutchunks.app.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkRepository
import com.coconutchunks.app.data.ChunkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditChunkUiState(
    val isLoading: Boolean = true,
    val chunk: ChunkEntity? = null,
    val chunkText: String = "",
    val example1: String = "",
    val example2: String = "",
    val example3: String = "",
    val groupName: String = "",
    val status: ChunkStatus = ChunkStatus.REVIEW,
    val existingGroups: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean
        get() = chunk != null && chunkText.isNotBlank() && !isSaving && !isDeleting
}

private data class EditFormState(
    val chunkText: String = "",
    val example1: String = "",
    val example2: String = "",
    val example3: String = "",
    val groupName: String = "",
    val status: ChunkStatus = ChunkStatus.REVIEW,
)

private data class EditOperationState(
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
)

class EditChunkViewModel(
    private val chunkId: Long,
    private val repository: ChunkRepository,
) : ViewModel() {

    private val formState = MutableStateFlow(EditFormState())
    private val operationState = MutableStateFlow(EditOperationState())
    private var initializedChunkId: Long? = null

    val uiState: StateFlow<EditChunkUiState> =
        combine(
            repository.observeById(chunkId),
            repository.observeGroups(),
            formState,
            operationState,
        ) { storedChunk, groups, form, operation ->
            if (storedChunk != null && initializedChunkId != storedChunk.id) {
                initializedChunkId = storedChunk.id
                formState.value = EditFormState(
                    chunkText = storedChunk.chunkText,
                    example1 = storedChunk.example1,
                    example2 = storedChunk.example2,
                    example3 = storedChunk.example3,
                    groupName = storedChunk.groupName,
                    status = storedChunk.status,
                )
            }

            val visibleForm =
                if (storedChunk != null && initializedChunkId == storedChunk.id) {
                    formState.value
                } else {
                    form
                }

            EditChunkUiState(
                isLoading = false,
                chunk = storedChunk,
                chunkText = visibleForm.chunkText,
                example1 = visibleForm.example1,
                example2 = visibleForm.example2,
                example3 = visibleForm.example3,
                groupName = visibleForm.groupName,
                status = visibleForm.status,
                existingGroups = groups,
                isSaving = operation.isSaving,
                isDeleting = operation.isDeleting,
                errorMessage = operation.errorMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EditChunkUiState(),
        )

    fun updateChunkText(value: String) {
        formState.value = formState.value.copy(chunkText = value)
        clearError()
    }

    fun updateExample1(value: String) {
        formState.value = formState.value.copy(example1 = value)
    }

    fun updateExample2(value: String) {
        formState.value = formState.value.copy(example2 = value)
    }

    fun updateExample3(value: String) {
        formState.value = formState.value.copy(example3 = value)
    }

    fun updateGroupName(value: String) {
        formState.value = formState.value.copy(groupName = value)
        clearError()
    }

    fun updateStatus(value: ChunkStatus) {
        formState.value = formState.value.copy(status = value)
    }

    fun save(onSaved: () -> Unit) {
        val current = uiState.value
        val original = current.chunk ?: return
        if (!current.canSave) return

        operationState.value = EditOperationState(isSaving = true)

        viewModelScope.launch {
            try {
                repository.update(
                    original.copy(
                        chunkText = current.chunkText.trim(),
                        example1 = current.example1.trim(),
                        example2 = current.example2.trim(),
                        example3 = current.example3.trim(),
                        groupName = current.groupName.trim().ifEmpty { "Ungrouped" },
                        status = current.status,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                operationState.value = EditOperationState()
                onSaved()
            } catch (_: Exception) {
                operationState.value = EditOperationState(
                    errorMessage = "Could not save changes.",
                )
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val original = uiState.value.chunk ?: return
        val operation = operationState.value
        if (operation.isDeleting || operation.isSaving) return

        operationState.value = EditOperationState(isDeleting = true)

        viewModelScope.launch {
            try {
                repository.delete(original)
                operationState.value = EditOperationState()
                onDeleted()
            } catch (_: Exception) {
                operationState.value = EditOperationState(
                    errorMessage = "Could not delete.",
                )
            }
        }
    }

    private fun clearError() {
        if (operationState.value.errorMessage != null) {
            operationState.value = operationState.value.copy(errorMessage = null)
        }
    }

    companion object {
        fun factory(
            chunkId: Long,
            repository: ChunkRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(EditChunkViewModel::class.java))
                    return EditChunkViewModel(
                        chunkId = chunkId,
                        repository = repository,
                    ) as T
                }
            }
    }
}
