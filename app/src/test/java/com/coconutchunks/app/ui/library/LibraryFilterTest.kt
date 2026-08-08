package com.coconutchunks.app.ui.library

import com.coconutchunks.app.data.ChunkEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterTest {

    private val chunks = listOf(
        chunk(id = 1, text = "Wie geht's?", group = "Basics"),
        chunk(id = 2, text = "Keine Ahnung", group = "Ungrouped"),
        chunk(id = 3, text = "Guten Morgen", group = "Basics"),
    )

    @Test
    fun searchMatchesChunkTextOnly() {
        val result = filterLibraryChunks(
            chunks = chunks,
            query = "ahnung",
            selectedGroup = ALL_GROUPS,
        )

        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun searchIsCaseInsensitive() {
        val result = filterLibraryChunks(
            chunks = chunks,
            query = "GUTEN",
            selectedGroup = ALL_GROUPS,
        )

        assertEquals(listOf(3L), result.map { it.id })
    }

    @Test
    fun groupFilterMatchesOnlySelectedGroup() {
        val result = filterLibraryChunks(
            chunks = chunks,
            query = "",
            selectedGroup = "Basics",
        )

        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test
    fun searchAndGroupFilterWorkTogether() {
        val result = filterLibraryChunks(
            chunks = chunks,
            query = "morgen",
            selectedGroup = "Basics",
        )

        assertEquals(listOf(3L), result.map { it.id })
    }

    @Test
    fun groupOptionsAlwaysContainAllAndUngrouped() {
        val options = buildGroupOptions(listOf("Travel", "Basics"))

        assertEquals(ALL_GROUPS, options[0])
        assertEquals(UNGROUPED, options[1])
        assertTrue("Basics" in options)
        assertTrue("Travel" in options)
    }

    private fun chunk(
        id: Long,
        text: String,
        group: String,
    ) = ChunkEntity(
        id = id,
        chunkText = text,
        example1 = "",
        example2 = "",
        example3 = "",
        groupName = group,
        createdAt = id,
        updatedAt = id,
    )
}
