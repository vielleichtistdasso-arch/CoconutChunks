package com.coconutchunks.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val chunks: List<ChunkEntity> = emptyList(),
    val query: String = "",
    val selectedGroup: String = ALL_GROUPS,
    val groupOptions: List<String> = listOf(ALL_GROUPS, UNGROUPED),
)

class LibraryViewModel(
    repository: ChunkRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedGroup = MutableStateFlow(ALL_GROUPS)

    val uiState: StateFlow<LibraryUiState> =
        combine(
            repository.observeAll(),
            repository.observeGroups(),
            query,
            selectedGroup,
        ) { chunks, groups, currentQuery, currentGroup ->
            val options = buildGroupOptions(groups)
            val safeGroup =
                if (currentGroup in options) currentGroup else ALL_GROUPS

            LibraryUiState(
                chunks = filterLibraryChunks(
                    chunks = chunks,
                    query = currentQuery,
                    selectedGroup = safeGroup,
                ),
                query = currentQuery,
                selectedGroup = safeGroup,
                groupOptions = options,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )

    fun updateQuery(value: String) {
        query.value = value
    }

    fun selectGroup(value: String) {
        selectedGroup.value = value
    }

    companion object {
        fun factory(repository: ChunkRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
                    return LibraryViewModel(repository) as T
                }
            }
    }
}
