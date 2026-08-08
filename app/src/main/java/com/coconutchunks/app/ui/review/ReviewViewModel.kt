package com.coconutchunks.app.ui.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkRepository
import com.coconutchunks.app.data.ChunkStatus
import com.coconutchunks.app.review.REVIEW_ALL
import com.coconutchunks.app.review.ReviewSelection
import com.coconutchunks.app.review.buildReviewPool
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ReviewPhase {
    SETUP,
    ACTIVE,
    COMPLETE,
}

data class ReviewUiState(
    val phase: ReviewPhase = ReviewPhase.SETUP,
    val availableGroups: List<String> = emptyList(),
    val selectedGroup: String = REVIEW_ALL,
    val includeMastered: Boolean = false,
    val reviewItems: List<ChunkEntity> = emptyList(),
    val currentIndex: Int = 0,
    val revealed: Boolean = false,
    val reviewedCount: Int = 0,
    val specialCount: Int = 0,
    val masteredCount: Int = 0,
    val isUpdating: Boolean = false,
    val isLoadingSession: Boolean = false,
    val message: String? = null,
) {
    val currentChunk: ChunkEntity?
        get() = reviewItems.getOrNull(currentIndex)
}

class ReviewViewModel(
    private val repository: ChunkRepository,
) : ViewModel() {

    var uiState by mutableStateOf(ReviewUiState())
        private set

    init {
        viewModelScope.launch {
            repository.observeGroups().collectLatest { groups ->
                val cleanGroups = groups
                    .map { it.trim().ifEmpty { "Ungrouped" } }
                    .distinct()
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)

                val safeSelection =
                    if (
                        uiState.selectedGroup == REVIEW_ALL ||
                        uiState.selectedGroup in cleanGroups
                    ) {
                        uiState.selectedGroup
                    } else {
                        REVIEW_ALL
                    }

                uiState = uiState.copy(
                    availableGroups = cleanGroups,
                    selectedGroup = safeSelection,
                )
            }
        }
    }

    fun selectGroup(group: String) {
        if (uiState.phase != ReviewPhase.SETUP || uiState.isLoadingSession) return
        uiState = uiState.copy(
            selectedGroup = group,
            message = null,
        )
    }

    fun setIncludeMastered(include: Boolean) {
        if (uiState.phase != ReviewPhase.SETUP || uiState.isLoadingSession) return
        uiState = uiState.copy(
            includeMastered = include,
            message = null,
        )
    }

    fun startReview() {
        if (uiState.phase != ReviewPhase.SETUP || uiState.isLoadingSession) return
        buildSession(
            emptyResultReturnsToSetup = false,
        )
    }

    fun revealExamples() {
        if (uiState.phase != ReviewPhase.ACTIVE) return
        if (uiState.currentChunk == null) return
        uiState = uiState.copy(revealed = true)
    }

    fun next() {
        if (uiState.phase != ReviewPhase.ACTIVE) return
        if (!uiState.revealed || uiState.isUpdating) return

        advance(
            specialIncrement = 0,
            masteredIncrement = 0,
        )
    }

    fun markSpecial() {
        updateCurrentStatus(
            status = ChunkStatus.SPECIAL,
            specialIncrement = 1,
            masteredIncrement = 0,
        )
    }

    fun markMastered() {
        updateCurrentStatus(
            status = ChunkStatus.MASTERED,
            specialIncrement = 0,
            masteredIncrement = 1,
        )
    }

    fun reviewAgain() {
        if (uiState.phase != ReviewPhase.COMPLETE || uiState.isLoadingSession) return
        buildSession(
            emptyResultReturnsToSetup = true,
        )
    }

    private fun buildSession(
        emptyResultReturnsToSetup: Boolean,
    ) {
        uiState = uiState.copy(
            isLoadingSession = true,
            message = null,
        )

        viewModelScope.launch {
            try {
                val chunks = repository.getAllOnce()
                val pool = buildReviewPool(
                    chunks = chunks,
                    selection = ReviewSelection(
                        selectedGroup = uiState.selectedGroup,
                        includeMastered = uiState.includeMastered,
                    ),
                )

                if (pool.isEmpty()) {
                    uiState = uiState.copy(
                        phase = if (emptyResultReturnsToSetup) {
                            ReviewPhase.SETUP
                        } else {
                            uiState.phase
                        },
                        reviewItems = emptyList(),
                        currentIndex = 0,
                        revealed = false,
                        reviewedCount = 0,
                        specialCount = 0,
                        masteredCount = 0,
                        isLoadingSession = false,
                        message = "No chunks available for review.",
                    )
                } else {
                    uiState = uiState.copy(
                        phase = ReviewPhase.ACTIVE,
                        reviewItems = pool,
                        currentIndex = 0,
                        revealed = false,
                        reviewedCount = 0,
                        specialCount = 0,
                        masteredCount = 0,
                        isUpdating = false,
                        isLoadingSession = false,
                        message = null,
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoadingSession = false,
                    message = "Could not load review chunks.",
                )
            }
        }
    }

    private fun updateCurrentStatus(
        status: ChunkStatus,
        specialIncrement: Int,
        masteredIncrement: Int,
    ) {
        val current = uiState.currentChunk ?: return
        if (uiState.phase != ReviewPhase.ACTIVE) return
        if (!uiState.revealed || uiState.isUpdating) return

        uiState = uiState.copy(
            isUpdating = true,
            message = null,
        )

        viewModelScope.launch {
            try {
                val updated = current.copy(
                    status = status,
                    updatedAt = System.currentTimeMillis(),
                )

                repository.update(updated)

                val updatedSessionItems = uiState.reviewItems.map { chunk ->
                    if (chunk.id == updated.id) {
                        chunk.copy(
                            status = updated.status,
                            updatedAt = updated.updatedAt,
                        )
                    } else {
                        chunk
                    }
                }

                uiState = uiState.copy(
                    reviewItems = updatedSessionItems,
                    isUpdating = false,
                )

                advance(
                    specialIncrement = specialIncrement,
                    masteredIncrement = masteredIncrement,
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isUpdating = false,
                    message = "Could not update status.",
                )
            }
        }
    }

    private fun advance(
        specialIncrement: Int,
        masteredIncrement: Int,
    ) {
        val nextReviewed = uiState.reviewedCount + 1
        val nextIndex = uiState.currentIndex + 1
        val finished = nextIndex >= uiState.reviewItems.size

        uiState = uiState.copy(
            phase = if (finished) ReviewPhase.COMPLETE else ReviewPhase.ACTIVE,
            currentIndex = nextIndex,
            revealed = false,
            reviewedCount = nextReviewed,
            specialCount = uiState.specialCount + specialIncrement,
            masteredCount = uiState.masteredCount + masteredIncrement,
            isUpdating = false,
            message = null,
        )
    }

    companion object {
        fun factory(repository: ChunkRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(ReviewViewModel::class.java))
                    return ReviewViewModel(repository) as T
                }
            }
    }
}
