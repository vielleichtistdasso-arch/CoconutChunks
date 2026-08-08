package com.coconutchunks.app.review

import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkStatus
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPoolBuilderTest {

    @Test
    fun emptyDatabaseProducesEmptyPool() {
        val pool = buildReviewPool(
            chunks = emptyList(),
            selection = ReviewSelection(),
            random = Random(1),
        )

        assertTrue(pool.isEmpty())
    }

    @Test
    fun reviewChunkAppearsOnce() {
        val chunk = chunk(
            id = 1,
            text = "Guten Morgen",
            status = ChunkStatus.REVIEW,
        )

        val pool = buildReviewPool(
            chunks = listOf(chunk),
            selection = ReviewSelection(),
            random = Random(1),
        )

        assertEquals(listOf(1L), pool.map { it.id })
    }

    @Test
    fun specialChunkAppearsTwice() {
        val chunk = chunk(
            id = 2,
            text = "Keine Ahnung",
            status = ChunkStatus.SPECIAL,
        )

        val pool = buildReviewPool(
            chunks = listOf(chunk),
            selection = ReviewSelection(),
            random = Random(1),
        )

        assertEquals(2, pool.size)
        assertEquals(2, pool.count { it.id == 2L })
    }

    @Test
    fun masteredIsExcludedByDefault() {
        val chunk = chunk(
            id = 3,
            text = "Schon gut",
            status = ChunkStatus.MASTERED,
        )

        val pool = buildReviewPool(
            chunks = listOf(chunk),
            selection = ReviewSelection(),
            random = Random(1),
        )

        assertTrue(pool.isEmpty())
    }

    @Test
    fun masteredAppearsOnceWhenIncluded() {
        val chunk = chunk(
            id = 3,
            text = "Schon gut",
            status = ChunkStatus.MASTERED,
        )

        val pool = buildReviewPool(
            chunks = listOf(chunk),
            selection = ReviewSelection(includeMastered = true),
            random = Random(1),
        )

        assertEquals(1, pool.size)
        assertEquals(3L, pool.single().id)
    }

    @Test
    fun selectedGroupRestrictsPool() {
        val basics = chunk(
            id = 1,
            text = "Hallo",
            group = "Basics",
            status = ChunkStatus.REVIEW,
        )
        val travel = chunk(
            id = 2,
            text = "Wo ist der Bahnhof?",
            group = "Travel",
            status = ChunkStatus.REVIEW,
        )

        val pool = buildReviewPool(
            chunks = listOf(basics, travel),
            selection = ReviewSelection(selectedGroup = "Travel"),
            random = Random(1),
        )

        assertEquals(listOf(2L), pool.map { it.id })
    }

    @Test
    fun reviewAllIncludesEligibleChunksFromAllGroups() {
        val chunks = listOf(
            chunk(1, "Hallo", "Basics", ChunkStatus.REVIEW),
            chunk(2, "Danke", "Travel", ChunkStatus.REVIEW),
        )

        val pool = buildReviewPool(
            chunks = chunks,
            selection = ReviewSelection(selectedGroup = REVIEW_ALL),
            random = Random(1),
        )

        assertEquals(setOf(1L, 2L), pool.map { it.id }.toSet())
    }

    @Test
    fun weightingMatchesSpecification() {
        val chunks = listOf(
            chunk(1, "A", status = ChunkStatus.REVIEW),
            chunk(2, "B", status = ChunkStatus.SPECIAL),
            chunk(3, "C", status = ChunkStatus.MASTERED),
        )

        val defaultPool = buildReviewPool(
            chunks = chunks,
            selection = ReviewSelection(includeMastered = false),
            random = Random(1),
        )

        assertEquals(3, defaultPool.size)
        assertEquals(1, defaultPool.count { it.id == 1L })
        assertEquals(2, defaultPool.count { it.id == 2L })
        assertFalse(defaultPool.any { it.id == 3L })

        val withMastered = buildReviewPool(
            chunks = chunks,
            selection = ReviewSelection(includeMastered = true),
            random = Random(1),
        )

        assertEquals(4, withMastered.size)
        assertEquals(1, withMastered.count { it.id == 1L })
        assertEquals(2, withMastered.count { it.id == 2L })
        assertEquals(1, withMastered.count { it.id == 3L })
    }

    @Test
    fun builderDoesNotMutateInputList() {
        val chunks = listOf(
            chunk(1, "A", status = ChunkStatus.REVIEW),
            chunk(2, "B", status = ChunkStatus.SPECIAL),
        )
        val originalIds = chunks.map { it.id }

        buildReviewPool(
            chunks = chunks,
            selection = ReviewSelection(),
            random = Random(2),
        )

        assertEquals(originalIds, chunks.map { it.id })
    }

    private fun chunk(
        id: Long,
        text: String,
        group: String = "Ungrouped",
        status: ChunkStatus,
    ) = ChunkEntity(
        id = id,
        chunkText = text,
        example1 = "",
        example2 = "",
        example3 = "",
        groupName = group,
        status = status,
        createdAt = id,
        updatedAt = id,
    )
}
