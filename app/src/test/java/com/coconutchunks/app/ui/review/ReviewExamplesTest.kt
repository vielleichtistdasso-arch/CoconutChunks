package com.coconutchunks.app.ui.review

import com.coconutchunks.app.data.ChunkEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewExamplesTest {

    @Test
    fun emptyExamplesAreHidden() {
        val chunk = chunk(
            example1 = "",
            example2 = "   ",
            example3 = "",
        )

        assertTrue(nonEmptyExamples(chunk).isEmpty())
    }

    @Test
    fun nonEmptyExamplesKeepOrderAndAreTrimmed() {
        val chunk = chunk(
            example1 = "  Eins. ",
            example2 = "",
            example3 = " Drei. ",
        )

        assertEquals(
            listOf("Eins.", "Drei."),
            nonEmptyExamples(chunk),
        )
    }

    private fun chunk(
        example1: String,
        example2: String,
        example3: String,
    ) = ChunkEntity(
        id = 1L,
        chunkText = "Chunk",
        example1 = example1,
        example2 = example2,
        example3 = example3,
        groupName = "Ungrouped",
        createdAt = 1L,
        updatedAt = 1L,
    )
}
