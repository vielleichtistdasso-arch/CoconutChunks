package com.coconutchunks.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coconutchunks.app.data.BackupCodec
import com.coconutchunks.app.data.ChunkRepository
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: ChunkRepository,
) : ViewModel() {

    val databaseItemCount: StateFlow<Int> =
        repository.observeCount().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val json = BackupCodec.encode(repository.getAllOnce())
                    outputStream.bufferedWriter().use { writer ->
                        writer.write(json)
                    }
                }
            }.onSuccess {
                _backupMessage.value = "Backup exported."
            }.onFailure {
                _backupMessage.value = "Could not export backup."
            }
        }
    }

    fun restoreBackup(inputStream: InputStream) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val json = inputStream.bufferedReader().use { it.readText() }
                    val chunks = BackupCodec.decode(json)
                    repository.replaceAll(chunks)
                    chunks.size
                }
            }.onSuccess { restoredCount ->
                _backupMessage.value = "Restored $restoredCount chunks."
            }.onFailure {
                _backupMessage.value = "Could not restore backup. Existing data was kept."
            }
        }
    }

    companion object {
        fun factory(repository: ChunkRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
