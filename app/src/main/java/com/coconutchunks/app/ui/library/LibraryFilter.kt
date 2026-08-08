package com.coconutchunks.app.ui.library

import com.coconutchunks.app.data.ChunkEntity

const val ALL_GROUPS = "All"
const val UNGROUPED = "Ungrouped"

fun filterLibraryChunks(
    chunks: List<ChunkEntity>,
    query: String,
    selectedGroup: String,
): List<ChunkEntity> {
    val normalizedQuery = query.trim()

    return chunks.filter { chunk ->
        val matchesQuery =
            normalizedQuery.isEmpty() ||
                chunk.chunkText.contains(normalizedQuery, ignoreCase = true)

        val matchesGroup =
            selectedGroup == ALL_GROUPS ||
                chunk.groupName == selectedGroup

        matchesQuery && matchesGroup
    }
}

fun buildGroupOptions(existingGroups: List<String>): List<String> {
    val groups = existingGroups
        .map { it.trim().ifEmpty { UNGROUPED } }
        .filterNot { it == ALL_GROUPS || it == UNGROUPED }
        .distinct()
        .sortedWith(String.CASE_INSENSITIVE_ORDER)

    return listOf(ALL_GROUPS, UNGROUPED) + groups
}
