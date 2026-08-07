package com.coconutchunks.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert
    suspend fun insertChunk(chunk: ChunkEntity): Long

    @Update
    suspend fun updateChunk(chunk: ChunkEntity)

    @Query("DELETE FROM chunks WHERE id = :id")
    suspend fun deleteChunk(id: Long)

    @Query("""
        SELECT c.id, c.chunkText, c.example1, c.example2, c.example3, c.groupId,
               c.status, c.note, c.createdAt, c.editedAt, c.lastReviewedAt,
               c.totalReviewCount, COALESCE(g.name, 'Ungrouped') AS groupName
        FROM chunks c LEFT JOIN groups g ON c.groupId = g.id
        WHERE c.id = :id
    """)
    suspend fun getChunk(id: Long): ChunkWithGroup?

    @Query("""
        SELECT c.id, c.chunkText, c.example1, c.example2, c.example3, c.groupId,
               c.status, c.note, c.createdAt, c.editedAt, c.lastReviewedAt,
               c.totalReviewCount, COALESCE(g.name, 'Ungrouped') AS groupName
        FROM chunks c LEFT JOIN groups g ON c.groupId = g.id
        WHERE (:query = '' OR c.chunkText LIKE '%' || :query || '%' COLLATE NOCASE
               OR c.example1 LIKE '%' || :query || '%' COLLATE NOCASE
               OR c.example2 LIKE '%' || :query || '%' COLLATE NOCASE
               OR c.example3 LIKE '%' || :query || '%' COLLATE NOCASE
               OR c.note LIKE '%' || :query || '%' COLLATE NOCASE)
          AND (:groupId IS NULL OR c.groupId = :groupId)
          AND (:status IS NULL OR c.status = :status)
        ORDER BY
          CASE WHEN :sort = 'NEWEST' THEN c.createdAt END DESC,
          CASE WHEN :sort = 'OLDEST' THEN c.createdAt END ASC,
          CASE WHEN :sort = 'ALPHABETICAL' THEN c.chunkText END COLLATE NOCASE ASC,
          CASE WHEN :sort = 'RECENTLY_REVIEWED' THEN c.lastReviewedAt END DESC,
          CASE WHEN :sort = 'STATUS' THEN c.status END ASC,
          c.id DESC
    """)
    fun observeLibrary(
        query: String,
        groupId: Long?,
        status: ReviewStatus?,
        sort: String
    ): Flow<List<ChunkWithGroup>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Query("UPDATE groups SET name = :name WHERE id = :id")
    suspend fun renameGroup(id: Long, name: String)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: Long)

    @Query("UPDATE chunks SET groupId = :newGroupId, editedAt = :now WHERE groupId = :oldGroupId")
    suspend fun moveGroupChunks(oldGroupId: Long, newGroupId: Long?, now: Long)

    @Query("SELECT * FROM groups ORDER BY CASE WHEN name = 'Ungrouped' THEN 0 ELSE 1 END, name COLLATE NOCASE")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups ORDER BY CASE WHEN name = 'Ungrouped' THEN 0 ELSE 1 END, name COLLATE NOCASE")
    suspend fun getGroups(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE name = 'Ungrouped' LIMIT 1")
    suspend fun getUngrouped(): GroupEntity?

    @Query("""
        SELECT g.id, g.name,
          COUNT(c.id) AS total,
          COALESCE(SUM(CASE WHEN c.status = 'SPECIAL' THEN 1 ELSE 0 END), 0) AS specialCount,
          COALESCE(SUM(CASE WHEN c.status = 'MASTERED' THEN 1 ELSE 0 END), 0) AS masteredCount
        FROM groups g LEFT JOIN chunks c ON c.groupId = g.id
        GROUP BY g.id
        ORDER BY CASE WHEN g.name = 'Ungrouped' THEN 0 ELSE 1 END, g.name COLLATE NOCASE
    """)
    fun observeGroupStats(): Flow<List<GroupStats>>

    @Query("""
        SELECT id, status FROM chunks
        WHERE (:groupId IS NULL OR groupId = :groupId)
          AND (:specialOnly = 0 OR status = 'SPECIAL')
    """)
    suspend fun getReviewCandidates(groupId: Long?, specialOnly: Boolean): List<ReviewCandidate>

    @Query("""
        UPDATE chunks SET
          status = COALESCE(:newStatus, status),
          lastReviewedAt = :now,
          totalReviewCount = totalReviewCount + 1
        WHERE id = :id
    """)
    suspend fun recordReview(id: Long, newStatus: ReviewStatus?, now: Long)

    @Query("""
        UPDATE chunks SET
          status = :status,
          lastReviewedAt = :lastReviewedAt,
          totalReviewCount = :totalReviewCount
        WHERE id = :id
    """)
    suspend fun undoReview(
        id: Long,
        status: ReviewStatus,
        lastReviewedAt: Long?,
        totalReviewCount: Int
    )

    @Query("""
        SELECT COUNT(*) AS total,
          COALESCE(SUM(CASE WHEN status = 'REVIEW' THEN 1 ELSE 0 END), 0) AS reviewCount,
          COALESCE(SUM(CASE WHEN status = 'SPECIAL' THEN 1 ELSE 0 END), 0) AS specialCount,
          COALESCE(SUM(CASE WHEN status = 'MASTERED' THEN 1 ELSE 0 END), 0) AS masteredCount
        FROM chunks
    """)
    fun observeOverview(): Flow<OverviewStats>

    @Query("SELECT COUNT(*) FROM chunks WHERE lastReviewedAt BETWEEN :start AND :end")
    fun observeReviewedBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM chunks ORDER BY id")
    suspend fun getAllChunksForBackup(): List<ChunkEntity>

    @Query("DELETE FROM chunks")
    suspend fun clearChunks()

    @Query("DELETE FROM groups")
    suspend fun clearGroups()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreGroups(groups: List<GroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreChunks(chunks: List<ChunkEntity>)
}
