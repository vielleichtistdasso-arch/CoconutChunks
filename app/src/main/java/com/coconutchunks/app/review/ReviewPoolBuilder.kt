package com.coconutchunks.app.review

import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkStatus
import kotlin.random.Random

const val REVIEW_ALL = "All"

data class ReviewSelection(
    val selectedGroup: String = REVIEW_ALL,
    val includeMastered: Boolean = false,
)

fun buildReviewPool(
    chunks: List<ChunkEntity>,
    selection: ReviewSelection,
    random: Random = Random.Default,
): List<ChunkEntity> {
    val eligible = chunks.filter { chunk ->
        val matchesGroup =
            selection.selectedGroup == REVIEW_ALL ||
                chunk.groupName == selection.selectedGroup

        val matchesStatus =
            chunk.status != ChunkStatus.MASTERED ||
                selection.includeMastered

        matchesGroup && matchesStatus
    }

    val weighted = buildList {
        eligible.forEach { chunk ->
            when (chunk.status) {
                ChunkStatus.REVIEW -> add(chunk)
                ChunkStatus.SPECIAL -> {
                    add(chunk)
                    add(chunk)
                }
                ChunkStatus.MASTERED -> {
                    if (selection.includeMastered) {
                        add(chunk)
                    }
                }
            }
        }
    }

    return weighted.shuffled(random)
}
