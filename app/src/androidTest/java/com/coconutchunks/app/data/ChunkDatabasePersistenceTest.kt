package com.coconutchunks.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChunkDatabasePersistenceTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    private val databaseName = "chunk-persistence-test.db"

    @After
    fun cleanup() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun savedChunkStillExistsAfterDatabaseReopen() = runBlocking {
        context.deleteDatabase(databaseName)

        var db = Room.databaseBuilder(
            context,
            ChunkDatabase::class.java,
            databaseName,
        ).build()

        db.chunkDao().insert(
            ChunkEntity(
                chunkText = "Wieder da",
                example1 = "",
                example2 = "",
                example3 = "",
                groupName = "Ungrouped",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        db.close()

        db = Room.databaseBuilder(
            context,
            ChunkDatabase::class.java,
            databaseName,
        ).build()

        val chunks = db.chunkDao().observeAll().first()

        assertEquals(1, chunks.size)
        assertEquals("Wieder da", chunks.single().chunkText)

        db.close()
    }
}
