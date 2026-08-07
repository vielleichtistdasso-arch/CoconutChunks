package com.coconutchunks.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val dailyTarget: Int = 20,
    val masteredWeight: Double = 0.5,
    val swipeRightEnabled: Boolean = true
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val DAILY_TARGET = intPreferencesKey("daily_target")
        val MASTERED_WEIGHT = doublePreferencesKey("mastered_weight")
        val SWIPE_RIGHT = booleanPreferencesKey("swipe_right")
    }

    val settings = context.dataStore.data.map { p ->
        AppSettings(
            dailyTarget = p[Keys.DAILY_TARGET] ?: 20,
            masteredWeight = p[Keys.MASTERED_WEIGHT] ?: 0.5,
            swipeRightEnabled = p[Keys.SWIPE_RIGHT] ?: true
        )
    }

    suspend fun setDailyTarget(value: Int) =
        context.dataStore.edit { it[Keys.DAILY_TARGET] = value.coerceIn(1, 500) }

    suspend fun setMasteredWeight(value: Double) =
        context.dataStore.edit { it[Keys.MASTERED_WEIGHT] = value.coerceIn(0.05, 5.0) }

    suspend fun setSwipeRight(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SWIPE_RIGHT] = enabled }
}
