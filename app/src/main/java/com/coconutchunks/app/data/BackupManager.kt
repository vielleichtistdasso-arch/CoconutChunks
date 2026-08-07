package com.coconutchunks.app.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupManager(
    private val context: Context,
    private val repository: AppRepository
) {
    suspend fun exportJson(uri: Uri) {
        val json = createJson()
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
            ?: error("Unable to open export destination.")
    }

    suspend fun exportCsv(uri: Uri) {
        val (groups, chunks) = repository.backupData()
        val names = groups.associate { it.id to it.name }
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { out ->
            out.appendLine("Chunk,Example 1,Example 2,Example 3,Group,Status,Note")
            chunks.forEach { c ->
                out.appendLine(
                    listOf(
                        c.chunkText, c.example1, c.example2, c.example3,
                        names[c.groupId] ?: "Ungrouped", c.status.name, c.note
                    ).joinToString(",") { csv(it) }
                )
            }
        } ?: error("Unable to open CSV destination.")
    }

    suspend fun importJson(uri: Uri) {
        // Internal safety copy before replacement.
        val safety = File(context.filesDir, "safety_backup_${System.currentTimeMillis()}.json")
        safety.writeText(createJson())

        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Unable to read backup.")
        val root = JSONObject(text)
        require(root.optInt("version", 0) == 1) { "Unsupported backup version." }

        val groupsJson = root.getJSONArray("groups")
        val chunksJson = root.getJSONArray("chunks")
        val groups = buildList {
            for (i in 0 until groupsJson.length()) {
                val o = groupsJson.getJSONObject(i)
                add(GroupEntity(o.getLong("id"), o.getString("name"), o.getLong("createdAt")))
            }
        }
        val chunks = buildList {
            for (i in 0 until chunksJson.length()) {
                val o = chunksJson.getJSONObject(i)
                add(
                    ChunkEntity(
                        id = o.getLong("id"),
                        chunkText = o.getString("chunkText"),
                        example1 = o.optString("example1"),
                        example2 = o.optString("example2"),
                        example3 = o.optString("example3"),
                        groupId = if (o.isNull("groupId")) null else o.getLong("groupId"),
                        status = ReviewStatus.valueOf(o.getString("status")),
                        note = o.optString("note"),
                        createdAt = o.getLong("createdAt"),
                        editedAt = o.getLong("editedAt"),
                        lastReviewedAt = if (o.isNull("lastReviewedAt")) null else o.getLong("lastReviewedAt"),
                        totalReviewCount = o.optInt("totalReviewCount", 0)
                    )
                )
            }
        }
        repository.restore(groups, chunks)
    }

    suspend fun createJson(): String {
        val (groups, chunks) = repository.backupData()
        return JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("groups", JSONArray().apply {
                groups.forEach { g ->
                    put(JSONObject().apply {
                        put("id", g.id); put("name", g.name); put("createdAt", g.createdAt)
                    })
                }
            })
            put("chunks", JSONArray().apply {
                chunks.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("chunkText", c.chunkText)
                        put("example1", c.example1)
                        put("example2", c.example2)
                        put("example3", c.example3)
                        put("groupId", c.groupId ?: JSONObject.NULL)
                        put("status", c.status.name)
                        put("note", c.note)
                        put("createdAt", c.createdAt)
                        put("editedAt", c.editedAt)
                        put("lastReviewedAt", c.lastReviewedAt ?: JSONObject.NULL)
                        put("totalReviewCount", c.totalReviewCount)
                    })
                }
            })
        }.toString(2)
    }

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
