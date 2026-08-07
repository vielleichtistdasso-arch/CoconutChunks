package com.coconutchunks.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromStatus(value: ReviewStatus?): String? = value?.name

    @TypeConverter
    fun toStatus(value: String): ReviewStatus = ReviewStatus.valueOf(value)
}

@Database(
    entities = [ChunkEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coconut_chunks.db"
                ).build().also { INSTANCE = it }
            }
    }
}
