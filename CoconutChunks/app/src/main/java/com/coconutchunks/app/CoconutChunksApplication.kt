package com.coconutchunks.app

import android.app.Application
import com.coconutchunks.app.data.AppDatabase
import com.coconutchunks.app.data.AppRepository
import com.coconutchunks.app.data.BackupManager
import com.coconutchunks.app.data.SettingsStore

class CoconutChunksApplication : Application() {
    val database by lazy { AppDatabase.get(this) }
    val repository by lazy { AppRepository(database, database.dao()) }
    val settings by lazy { SettingsStore(this) }
    val backupManager by lazy { BackupManager(this, repository) }
}
