package com.coconutchunks.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChunkDaoTest {
    private lateinit var database: ChunkDatabase
    private lateinit var dao: ChunkDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ChunkDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.chunkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadChunk() = runBlocking {
        val id = dao.insert(sampleChunk(chunkText = "Guten Morgen"))
        val chunks = dao.observeAll().first()

        assertEquals(1, chunks.size)
        assertEquals(id, chunks.single().id)
        assertEquals("Guten Morgen", chunks.single().chunkText)
        assertEquals(ChunkStatus.REVIEW, chunks.single().status)
    }

    @Test
    fun chunkWithoutExamplesPersistsEmptyExamples() = runBlocking {
        dao.insert(sampleChunk(chunkText = "Bis später"))
        val chunk = dao.observeAll().first().single()

        assertEquals("", chunk.example1)
        assertEquals("", chunk.example2)
        assertEquals("", chunk.example3)
    }

    @Test
    fun ungroupedChunkPersistsUngroupedGroup() = runBlocking {
        dao.insert(sampleChunk(chunkText = "Keine Ahnung"))
        val chunk = dao.observeAll().first().single()

        assertEquals("Ungrouped", chunk.groupName)
    }

    @Test
    fun updateChunkPersistsChanges() = runBlocking {
        val id = dao.insert(sampleChunk(chunkText = "Alt"))
        val original = dao.observeAll().first().single()

        dao.update(
            original.copy(
                chunkText = "Neu",
                status = ChunkStatus.SPECIAL,
                updatedAt = 2L,
            )
        )

        val updated = dao.observeById(id).first()
        assertEquals("Neu", updated?.chunkText)
        assertEquals(ChunkStatus.SPECIAL, updated?.status)
        assertEquals(2L, updated?.updatedAt)
    }

    @Test
    fun deleteChunkRemovesIt() = runBlocking {
        val id = dao.insert(sampleChunk(chunkText = "Löschen"))
        val chunk = dao.observeById(id).first()
        requireNotNull(chunk)

        dao.delete(chunk)

        assertNull(dao.observeById(id).first())
        assertEquals(0, dao.observeCount().first())
    }

    @Test
    fun searchMatchesChunkTextOnly() = runBlocking {
        dao.insert(
            sampleChunk(
                chunkText = "Guten Morgen",
                example1 = "Das Beispiel enthält Zug.",
                groupName = "Basics",
            )
        )
        dao.insert(
            sampleChunk(
                chunkText = "Der Zug fährt ab",
                groupName = "Travel",
                now = 2L,
            )
        )

        val results = dao.searchByChunkText("Zug").first()

        assertEquals(1, results.size)
        assertEquals("Der Zug fährt ab", results.single().chunkText)
    }


    @Test
    fun getAllOnceReturnsCurrentSnapshot() = runBlocking {
        dao.insert(sampleChunk(chunkText = "A"))
        dao.insert(sampleChunk(chunkText = "B", now = 2L))

        val snapshot = dao.getAllOnce()

        assertEquals(listOf("B", "A"), snapshot.map { it.chunkText })
    }

    @Test
    fun groupsAreDerivedFromChunks() = runBlocking {
        dao.insert(sampleChunk(chunkText = "A", groupName = "Travel"))
        dao.insert(sampleChunk(chunkText = "B", groupName = "Basics", now = 2L))
        dao.insert(sampleChunk(chunkText = "C", groupName = "Travel", now = 3L))

        assertEquals(listOf("Basics", "Travel"), dao.observeGroups().first())
    }

    @Test
    fun germanCharactersPersistCorrectly() = runBlocking {
        val text = "ä ö ü Ä Ö Ü ß"
        dao.insert(sampleChunk(chunkText = text))

        assertEquals(text, dao.observeAll().first().single().chunkText)
    }

    private fun sampleChunk(
        chunkText: String,
        example1: String = "",
        example2: String = "",
        example3: String = "",
        groupName: String = "Ungrouped",
        status: ChunkStatus = ChunkStatus.REVIEW,
        now: Long = 1L,
    ) = ChunkEntity(
        chunkText = chunkText,
        example1 = example1,
        example2 = example2,
        example3 = example3,
        groupName = groupName,
        status = status,
        createdAt = now,
        updatedAt = now,
    )
}
