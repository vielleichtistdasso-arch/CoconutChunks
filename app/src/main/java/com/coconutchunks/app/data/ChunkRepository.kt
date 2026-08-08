package com.coconutchunks.app.data

class ChunkRepository(
    private val dao: ChunkDao,
) {
    fun observeAll() = dao.observeAll()

    suspend fun getAllOnce() = dao.getAllOnce()

    fun observeById(id: Long) = dao.observeById(id)

    fun searchByChunkText(query: String) = dao.searchByChunkText(query)

    fun observeGroups() = dao.observeGroups()

    fun observeCount() = dao.observeCount()

    suspend fun insert(chunk: ChunkEntity) = dao.insert(chunk)

    suspend fun update(chunk: ChunkEntity) = dao.update(chunk)

    suspend fun delete(chunk: ChunkEntity) = dao.delete(chunk)

    suspend fun replaceAll(chunks: List<ChunkEntity>) = dao.replaceAll(chunks)
}
