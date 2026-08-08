package com.coconutchunks.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupCodecTest {

    @Test
    fun roundTripPreservesChunkData() {
        val original = listOf(
            ChunkEntity(
                id = 17L,
                chunkText = "Mir ist aufgefallen, dass …",
                example1 = "Mir ist aufgefallen, dass es ruhiger geworden ist.",
                example2 = "Ein zweites Beispiel.",
                example3 = "",
                groupName = "Wahrnehmung",
                status = ChunkStatus.SPECIAL,
                createdAt = 100L,
                updatedAt = 200L,
            )
        )

        val restored = BackupCodec.decode(BackupCodec.encode(original))

        assertEquals(original, restored)
    }

    @Test
    fun unsupportedBackupFormatIsRejected() {
        val result = runCatching {
            BackupCodec.decode("""{"formatVersion":99,"chunks":[]}""")
        }

        assertTrue(result.isFailure)
    }
}
