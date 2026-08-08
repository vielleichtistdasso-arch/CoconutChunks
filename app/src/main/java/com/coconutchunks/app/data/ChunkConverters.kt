package com.coconutchunks.app.data

import androidx.room.TypeConverter

class ChunkConverters {
    @TypeConverter
    fun statusToString(status: ChunkStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): ChunkStatus =
        ChunkStatus.entries.firstOrNull { it.name == value } ?: ChunkStatus.REVIEW
}
