package com.coconutchunks.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReviewSelectorTest {
    @Test fun completeShuffleKeepsEveryChunkOnce() {
        val candidates = (1L..100L).map { ReviewCandidate(it, ReviewStatus.REVIEW) }
        val result = ReviewSelector.shuffledAll(candidates, Random(1))
        assertEquals(100, result.size)
        assertEquals(100, result.toSet().size)
    }

    @Test fun dailyTargetIsRespected() {
        val candidates = (1L..100L).map { ReviewCandidate(it, ReviewStatus.REVIEW) }
        assertEquals(20, ReviewSelector.select(candidates, 20, random = Random(2)).size)
    }

    @Test fun masteredChunksRemainEligible() {
        val candidates = listOf(
            ReviewCandidate(1, ReviewStatus.MASTERED),
            ReviewCandidate(2, ReviewStatus.REVIEW)
        )
        val seen = (0..100).flatMap {
            ReviewSelector.select(candidates, 2, random = Random(it))
        }.toSet()
        assertTrue(1L in seen)
    }

    @Test fun specialHasHigherSelectionRateThanMastered() {
        val candidates = listOf(
            ReviewCandidate(1, ReviewStatus.SPECIAL),
            ReviewCandidate(2, ReviewStatus.MASTERED)
        )
        var specialWins = 0
        repeat(1000) { seed ->
            if (ReviewSelector.select(candidates, 1, random = Random(seed)).first() == 1L) specialWins++
        }
        assertTrue("Special should dominate weighted selection", specialWins > 750)
    }

    @Test fun selectionHasNoDuplicates() {
        val candidates = (1L..30L).map { ReviewCandidate(it, ReviewStatus.REVIEW) }
        val result = ReviewSelector.select(candidates, 20, random = Random(3))
        assertEquals(result.size, result.toSet().size)
    }
}
