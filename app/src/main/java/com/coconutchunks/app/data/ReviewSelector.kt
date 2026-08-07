package com.coconutchunks.app.data

import kotlin.math.ln
import kotlin.random.Random

object ReviewSelector {
    fun weight(status: ReviewStatus, masteredWeight: Double = 0.5): Double = when (status) {
        ReviewStatus.SPECIAL -> 5.0
        ReviewStatus.REVIEW -> 2.0
        ReviewStatus.MASTERED -> masteredWeight.coerceAtLeast(0.05)
    }

    /**
     * Weighted random sample without replacement.
     * Lower exponential-race keys are selected first.
     */
    fun select(
        candidates: List<ReviewCandidate>,
        count: Int,
        masteredWeight: Double = 0.5,
        random: Random = Random.Default
    ): List<Long> = candidates
        .map { candidate ->
            val u = random.nextDouble().coerceIn(1e-12, 1.0)
            val key = -ln(u) / weight(candidate.status, masteredWeight)
            candidate.id to key
        }
        .sortedBy { it.second }
        .take(count.coerceAtMost(candidates.size))
        .map { it.first }

    fun shuffledAll(candidates: List<ReviewCandidate>, random: Random = Random.Default): List<Long> =
        candidates.map { it.id }.shuffled(random)
}
