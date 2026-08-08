package com.coconutchunks.app.ui.edit

import com.coconutchunks.app.data.ChunkEntity
import com.coconutchunks.app.data.ChunkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditChunkSemanticsTest {

    @Test
    fun copyingForEditKeepsIdAndCreatedAt() {
        val original = ChunkEntity(
            id = 42L,
            chunkText = "Alt",
            example1 = "",
            example2 = "",
            example3 = "",
            groupName = "Basics",
            status = ChunkStatus.REVIEW,
            createdAt = 100L,
            updatedAt = 100L,
        )

        val edited = original.copy(
            chunkText = "Neu",
            groupName = "Ungrouped",
            status = ChunkStatus.MASTERED,
            updatedAt = 200L,
        )

        assertEquals(42L, edited.id)
        assertEquals(100L, edited.createdAt)
        assertEquals("Neu", edited.chunkText)
        assertEquals("Ungrouped", edited.groupName)
        assertEquals(ChunkStatus.MASTERED, edited.status)
        assertTrue(edited.updatedAt > original.updatedAt)
    }
}
