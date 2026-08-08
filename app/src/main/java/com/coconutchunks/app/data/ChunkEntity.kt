package com.coconutchunks.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chunks")
data class ChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chunkText: String,
    val example1: String = "",
    val example2: String = "",
    val example3: String = "",
    val groupName: String = "Ungrouped",
    val status: ChunkStatus = ChunkStatus.REVIEW,
    val createdAt: Long,
    val updatedAt: Long,
)
