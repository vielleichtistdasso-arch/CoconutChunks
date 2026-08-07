package com.coconutchunks.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRepositoryInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: AppRepository

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = AppRepository(db, db.dao())
    }

    @After fun close() = db.close()

    @Test fun addEditDeleteAndPartialExamples() = runTest {
        repo.ensureDefaultGroup()
        val id = repo.saveChunk(text="im Laufe der Zeit", example1="Eins", example2="", example3="",
            groupId=null, note="", status=ReviewStatus.REVIEW)
        assertNotNull(repo.getChunk(id))
        repo.saveChunk(id, "im Laufe der Zeit", "Eins", "Zwei", "Drei", null, "note", ReviewStatus.SPECIAL)
        assertEquals("Zwei", repo.getChunk(id)?.example2)
        repo.deleteChunk(id)
        assertNull(repo.getChunk(id))
    }

    @Test fun groupDeletionMovesChunksInsteadOfDeletingThem() = runTest {
        repo.ensureDefaultGroup()
        val a = repo.createGroup("Arbeit")
        val ungrouped = repo.groups().first { it.name == "Ungrouped" }.id
        val id = repo.saveChunk(text="Bescheid geben", example1="", example2="", example3="",
            groupId=a, note="", status=ReviewStatus.REVIEW)
        repo.deleteGroupAndMove(a, ungrouped)
        assertNotNull(repo.getChunk(id))
        assertEquals(ungrouped, repo.getChunk(id)?.groupId)
    }

    @Test fun searchMatchesChunkAndExamplesAndNote() = runTest {
        repo.saveChunk(text="zur Verfügung", example1="Das steht bereit.", example2="", example3="",
            groupId=null, note="useful office phrase", status=ReviewStatus.REVIEW)
        assertEquals(1, repo.observeLibrary("Verfügung").first().size)
        assertEquals(1, repo.observeLibrary("bereit").first().size)
        assertEquals(1, repo.observeLibrary("office").first().size)
    }

    @Test fun reviewActionsUpdateStatusAndMetadata() = runTest {
        val id = repo.saveChunk(text="darauf ankommen", example1="", example2="", example3="",
            groupId=null, note="", status=ReviewStatus.REVIEW)
        repo.recordReview(id, ReviewStatus.MASTERED)
        val c = repo.getChunk(id)!!
        assertEquals(ReviewStatus.MASTERED, c.status)
        assertEquals(1, c.totalReviewCount)
        assertNotNull(c.lastReviewedAt)
    }

    @Test fun tenThousandChunksCanBeQueried() = runTest {
        repeat(10_000) { i ->
            repo.saveChunk(text="chunk $i", example1="example $i", example2="", example3="",
                groupId=null, note="", status=ReviewStatus.REVIEW)
        }
        val first = repo.observeLibrary("chunk 9999").first()
        assertEquals(1, first.size)
    }

    @Test fun reviewUndoRestoresExactPreviousMetadata() = runTest {
        val id = repo.saveChunk(
            text="wieder rückgängig machen",
            example1="",
            example2="",
            example3="",
            groupId=null,
            note="",
            status=ReviewStatus.REVIEW
        )
        val before = repo.getChunk(id)!!
        repo.recordReview(id, ReviewStatus.MASTERED)
        assertEquals(ReviewStatus.MASTERED, repo.getChunk(id)!!.status)

        repo.undoReview(before)
        val restored = repo.getChunk(id)!!
        assertEquals(before.status, restored.status)
        assertEquals(before.totalReviewCount, restored.totalReviewCount)
        assertEquals(before.lastReviewedAt, restored.lastReviewedAt)
    }


    @Test fun dataSurvivesDatabaseReopen() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fileName = "reopen_${System.nanoTime()}.db"
        db.close()

        var diskDb = Room.databaseBuilder(context, AppDatabase::class.java, fileName).build()
        var diskRepo = AppRepository(diskDb, diskDb.dao())
        val id = diskRepo.saveChunk(
            text="dauerhaft gespeichert",
            example1="Das bleibt auf dem Gerät.",
            example2="",
            example3="",
            groupId=null,
            note="persistence",
            status=ReviewStatus.REVIEW
        )
        diskDb.close()

        diskDb = Room.databaseBuilder(context, AppDatabase::class.java, fileName).build()
        diskRepo = AppRepository(diskDb, diskDb.dao())
        assertEquals("dauerhaft gespeichert", diskRepo.getChunk(id)?.chunkText)
        diskDb.close()
        context.deleteDatabase(fileName)

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = AppRepository(db, db.dao())
    }


    @Test fun saveChunkCreatesUngroupedInvariantWhenNeeded() = runTest {
        val id = repo.saveChunk(
            text="ohne Gruppe",
            example1="",
            example2="",
            example3="",
            groupId=null,
            note="",
            status=ReviewStatus.REVIEW
        )
        val chunk = repo.getChunk(id)!!
        val ungrouped = repo.groups().first { it.name == "Ungrouped" }
        assertEquals(ungrouped.id, chunk.groupId)
    }

    @Test fun duplicateGroupNamesAreIdempotentCaseInsensitively() = runTest {
        val first = repo.createGroup("Arbeit")
        val second = repo.createGroup("arbeit")
        assertEquals(first, second)
        assertEquals(1, repo.groups().count { it.name.equals("Arbeit", ignoreCase = true) })
    }

    @Test fun deletingGroupWithoutDestinationFallsBackToUngrouped() = runTest {
        val source = repo.createGroup("Temporär")
        val id = repo.saveChunk(
            text="verschieben",
            example1="",
            example2="",
            example3="",
            groupId=source,
            note="",
            status=ReviewStatus.REVIEW
        )
        repo.deleteGroupAndMove(source, null)
        val ungrouped = repo.groups().first { it.name == "Ungrouped" }
        assertEquals(ungrouped.id, repo.getChunk(id)?.groupId)
    }

}
