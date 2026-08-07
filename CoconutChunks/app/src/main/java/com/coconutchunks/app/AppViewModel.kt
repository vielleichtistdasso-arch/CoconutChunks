package com.coconutchunks.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val container = app as CoconutChunksApplication
    val repository = container.repository
    val settings = container.settings.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings()
    )

    private val search = MutableStateFlow("")
    private val groupFilter = MutableStateFlow<Long?>(null)
    private val statusFilter = MutableStateFlow<ReviewStatus?>(null)
    private val sort = MutableStateFlow(ChunkSort.NEWEST)

    val selectedGroupFilter = groupFilter.asStateFlow()
    val selectedStatusFilter = statusFilter.asStateFlow()
    val selectedSort = sort.asStateFlow()

    val library = combine(search, groupFilter, statusFilter, sort) { q, g, s, so ->
        LibraryQuery(q, g, s, so)
    }.flatMapLatest { q -> repository.observeLibrary(q.text, q.groupId, q.status, q.sort) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val groups = repository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val groupStats = repository.observeGroupStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val overview = repository.observeOverview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewStats(0,0,0,0))

    val reviewedToday: StateFlow<Int> = run {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        repository.observeReviewedBetween(start, end)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    }

    init {
        viewModelScope.launch { repository.ensureDefaultGroup() }
    }

    fun setSearch(value: String) { search.value = value }
    fun setGroupFilter(value: Long?) { groupFilter.value = value }
    fun setStatusFilter(value: ReviewStatus?) { statusFilter.value = value }
    fun setSort(value: ChunkSort) { sort.value = value }

    fun saveChunk(
        id: Long = 0, text: String, e1: String, e2: String, e3: String,
        groupId: Long?, note: String, status: ReviewStatus,
        onDone: (Long) -> Unit = {}
    ) = viewModelScope.launch {
        val saved = repository.saveChunk(id, text, e1, e2, e3, groupId, note, status)
        onDone(saved)
    }

    fun deleteChunk(id: Long, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.deleteChunk(id); onDone()
    }

    fun createGroup(name: String) = viewModelScope.launch { repository.createGroup(name) }
    fun renameGroup(id: Long, name: String) = viewModelScope.launch { repository.renameGroup(id, name) }
    fun deleteGroup(id: Long, destinationId: Long?) = viewModelScope.launch {
        repository.deleteGroupAndMove(id, destinationId)
    }

    suspend fun getChunk(id: Long) = repository.getChunk(id)

    fun recordReview(id: Long, newStatus: ReviewStatus?) = viewModelScope.launch {
        repository.recordReview(id, newStatus)
    }

    fun undoReview(previous: ChunkWithGroup) = viewModelScope.launch {
        repository.undoReview(previous)
    }

    fun setDailyTarget(value: Int) = viewModelScope.launch { container.settings.setDailyTarget(value) }
    fun setMasteredWeight(value: Double) = viewModelScope.launch { container.settings.setMasteredWeight(value) }
    fun setSwipeRight(value: Boolean) = viewModelScope.launch { container.settings.setSwipeRight(value) }

    fun exportBackup(uri: Uri, onDone: (Result<Unit>) -> Unit) = viewModelScope.launch {
        onDone(runCatching { container.backupManager.exportJson(uri) })
    }
    fun exportCsv(uri: Uri, onDone: (Result<Unit>) -> Unit) = viewModelScope.launch {
        onDone(runCatching { container.backupManager.exportCsv(uri) })
    }
    fun importBackup(uri: Uri, onDone: (Result<Unit>) -> Unit) = viewModelScope.launch {
        onDone(runCatching { container.backupManager.importJson(uri) })
    }
}

data class LibraryQuery(
    val text: String,
    val groupId: Long?,
    val status: ReviewStatus?,
    val sort: ChunkSort
)
