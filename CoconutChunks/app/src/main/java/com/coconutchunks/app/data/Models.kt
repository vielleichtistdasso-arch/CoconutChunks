package com.coconutchunks.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReviewStatus { REVIEW, SPECIAL, MASTERED }
enum class ChunkSort { NEWEST, OLDEST, ALPHABETICAL, RECENTLY_REVIEWED, STATUS }

@Entity(
    tableName = "groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chunks",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("groupId"),
        Index("status"),
        Index("createdAt"),
        Index("lastReviewedAt")
    ]
)
data class ChunkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chunkText: String,
    val example1: String = "",
    val example2: String = "",
    val example3: String = "",
    val groupId: Long? = null,
    val status: ReviewStatus = ReviewStatus.REVIEW,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val totalReviewCount: Int = 0
)

data class ChunkWithGroup(
    val id: Long,
    val chunkText: String,
    val example1: String,
    val example2: String,
    val example3: String,
    val groupId: Long?,
    val status: ReviewStatus,
    val note: String,
    val createdAt: Long,
    val editedAt: Long,
    val lastReviewedAt: Long?,
    val totalReviewCount: Int,
    val groupName: String
)

data class GroupStats(
    val id: Long,
    val name: String,
    val total: Int,
    val specialCount: Int,
    val masteredCount: Int
)

data class ReviewCandidate(val id: Long, val status: ReviewStatus)

data class OverviewStats(
    val total: Int,
    val reviewCount: Int,
    val specialCount: Int,
    val masteredCount: Int
)
