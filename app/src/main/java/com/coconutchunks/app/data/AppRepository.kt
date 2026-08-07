package com.coconutchunks.app.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val db: AppDatabase,
    private val dao: AppDao
) {
    suspend fun ensureDefaultGroup() {
        if (dao.getUngrouped() == null) dao.insertGroup(GroupEntity(name = "Ungrouped"))
    }

    fun observeLibrary(
        query: String = "",
        groupId: Long? = null,
        status: ReviewStatus? = null,
        sort: ChunkSort = ChunkSort.NEWEST
    ) = dao.observeLibrary(query.trim(), groupId, status, sort.name)

    fun observeGroups() = dao.observeGroups()
    fun observeGroupStats() = dao.observeGroupStats()
    fun observeOverview() = dao.observeOverview()
    fun observeReviewedBetween(start: Long, end: Long): Flow<Int> = dao.observeReviewedBetween(start, end)

    suspend fun getChunk(id: Long) = dao.getChunk(id)
    suspend fun groups() = dao.getGroups()

    suspend fun saveChunk(
        id: Long = 0,
        text: String,
        example1: String,
        example2: String,
        example3: String,
        groupId: Long?,
        note: String,
        status: ReviewStatus = ReviewStatus.REVIEW
    ): Long {
        require(text.isNotBlank()) { "Chunk text cannot be empty." }
        val now = System.currentTimeMillis()
        ensureDefaultGroup()
        val resolvedGroupId = groupId ?: dao.getUngrouped()?.id
        if (id == 0L) {
            return dao.insertChunk(
                ChunkEntity(
                    chunkText = text.trim(),
                    example1 = example1.trim(),
                    example2 = example2.trim(),
                    example3 = example3.trim(),
                    groupId = resolvedGroupId,
                    note = note.trim(),
                    status = status,
                    createdAt = now,
                    editedAt = now
                )
            )
        }
        val old = dao.getChunk(id) ?: error("Chunk not found.")
        dao.updateChunk(
            ChunkEntity(
                id = id,
                chunkText = text.trim(),
                example1 = example1.trim(),
                example2 = example2.trim(),
                example3 = example3.trim(),
                groupId = resolvedGroupId,
                note = note.trim(),
                status = status,
                createdAt = old.createdAt,
                editedAt = now,
                lastReviewedAt = old.lastReviewedAt,
                totalReviewCount = old.totalReviewCount
            )
        )
        return id
    }

    suspend fun deleteChunk(id: Long) = dao.deleteChunk(id)

    suspend fun createGroup(name: String): Long {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return -1L

        ensureDefaultGroup()
        val groups = dao.getGroups()

        if (cleaned.equals("Ungrouped", ignoreCase = true)) {
            return groups.firstOrNull { it.name == "Ungrouped" }?.id ?: -1L
        }

        groups.firstOrNull { it.name.equals(cleaned, ignoreCase = true) }?.let {
            return it.id
        }

        return dao.insertGroup(GroupEntity(name = cleaned))
    }

    suspend fun renameGroup(id: Long, name: String) {
        val cleaned = name.trim()
        if (cleaned.isEmpty() || cleaned.equals("Ungrouped", ignoreCase = true)) return

        val duplicate = dao.getGroups().any {
            it.id != id && it.name.equals(cleaned, ignoreCase = true)
        }
        if (duplicate) return

        dao.renameGroup(id, cleaned)
    }

    suspend fun deleteGroupAndMove(id: Long, destinationGroupId: Long?) {
        ensureDefaultGroup()
        val allGroups = dao.getGroups()
        val group = allGroups.firstOrNull { it.id == id } ?: return
        require(group.name != "Ungrouped") { "Ungrouped cannot be deleted." }
        val safeDestination = destinationGroupId
            ?: allGroups.firstOrNull { it.name == "Ungrouped" }?.id
        db.withTransaction {
            dao.moveGroupChunks(id, safeDestination, System.currentTimeMillis())
            dao.deleteGroup(id)
        }
    }

    suspend fun buildReviewQueue(
        groupId: Long?,
        specialOnly: Boolean,
        count: Int?,
        completeGroup: Boolean,
        masteredWeight: Double
    ): List<Long> {
        val candidates = dao.getReviewCandidates(groupId, specialOnly)
        return if (completeGroup) {
            ReviewSelector.shuffledAll(candidates)
        } else {
            ReviewSelector.select(candidates, count ?: candidates.size, masteredWeight)
        }
    }

    suspend fun recordReview(id: Long, newStatus: ReviewStatus?) {
        dao.recordReview(id, newStatus, System.currentTimeMillis())
    }

    suspend fun undoReview(previous: ChunkWithGroup) {
        dao.undoReview(
            id = previous.id,
            status = previous.status,
            lastReviewedAt = previous.lastReviewedAt,
            totalReviewCount = previous.totalReviewCount
        )
    }

    suspend fun backupData(): Pair<List<GroupEntity>, List<ChunkEntity>> =
        dao.getGroups() to dao.getAllChunksForBackup()

    suspend fun restore(groups: List<GroupEntity>, chunks: List<ChunkEntity>) {
        db.withTransaction {
            dao.clearChunks()
            dao.clearGroups()
            dao.restoreGroups(groups)
            dao.restoreChunks(chunks)
        }
        ensureDefaultGroup()
    }
}
