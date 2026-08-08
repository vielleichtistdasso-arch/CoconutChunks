package com.coconutchunks.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ChunkEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ChunkConverters::class)
abstract class ChunkDatabase : RoomDatabase() {
    abstract fun chunkDao(): ChunkDao

    companion object {
        @Volatile
        private var instance: ChunkDatabase? = null

        fun getInstance(context: Context): ChunkDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChunkDatabase::class.java,
                    "coconut_chunks.db",
                ).build().also { instance = it }
            }
    }
}
