package com.coconutchunks.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChunkDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(chunk: ChunkEntity): Long

    @Update
    suspend fun update(chunk: ChunkEntity): Int

    @Delete
    suspend fun delete(chunk: ChunkEntity): Int

    @Query("SELECT * FROM chunks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ChunkEntity?>

    @Query("SELECT * FROM chunks ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks ORDER BY createdAt DESC, id DESC")
    suspend fun getAllOnce(): List<ChunkEntity>

    @Query(
        """
        SELECT * FROM chunks
        WHERE chunkText LIKE '%' || :query || '%'
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun searchByChunkText(query: String): Flow<List<ChunkEntity>>

    @Query(
        """
        SELECT DISTINCT groupName
        FROM chunks
        ORDER BY groupName COLLATE NOCASE ASC
        """
    )
    fun observeGroups(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM chunks")
    fun observeCount(): Flow<Int>
}
