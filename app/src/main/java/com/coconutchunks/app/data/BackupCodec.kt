package com.coconutchunks.app.data

import org.json.JSONArray
import org.json.JSONObject

object BackupCodec {
    private const val FORMAT_VERSION = 1

    fun encode(chunks: List<ChunkEntity>): String {
        val items = JSONArray()
        chunks.forEach { chunk ->
            items.put(
                JSONObject()
                    .put("id", chunk.id)
                    .put("chunkText", chunk.chunkText)
                    .put("example1", chunk.example1)
                    .put("example2", chunk.example2)
                    .put("example3", chunk.example3)
                    .put("groupName", chunk.groupName)
                    .put("status", chunk.status.name)
                    .put("createdAt", chunk.createdAt)
                    .put("updatedAt", chunk.updatedAt)
            )
        }

        return JSONObject()
            .put("formatVersion", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("chunks", items)
            .toString(2)
    }

    fun decode(json: String): List<ChunkEntity> {
        val root = JSONObject(json)
        require(root.getInt("formatVersion") == FORMAT_VERSION) {
            "Unsupported backup format."
        }

        val items = root.getJSONArray("chunks")
        return buildList(items.length()) {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    ChunkEntity(
                        id = item.getLong("id"),
                        chunkText = item.getString("chunkText"),
                        example1 = item.getString("example1"),
                        example2 = item.getString("example2"),
                        example3 = item.getString("example3"),
                        groupName = item.getString("groupName"),
                        status = ChunkStatus.valueOf(item.getString("status")),
                        createdAt = item.getLong("createdAt"),
                        updatedAt = item.getLong("updatedAt"),
                    )
                )
            }
        }
    }
}
