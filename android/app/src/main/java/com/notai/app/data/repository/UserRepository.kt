package com.notai.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val FREE_CREDITS = intPreferencesKey("free_credits")
        private val PROCESSING_COUNT = intPreferencesKey("processing_count")
        private const val DEFAULT_CREDITS = 10
    }

    val credits: Flow<Int> = dataStore.data.map { it[FREE_CREDITS] ?: DEFAULT_CREDITS }

    val processingCount: Flow<Int> = dataStore.data.map { it[PROCESSING_COUNT] ?: 0 }

    suspend fun decrementCredits(): Boolean {
        val current = credits.first()
        if (current <= 0) return false
        dataStore.edit { it[FREE_CREDITS] = current - 1 }
        return true
    }

    suspend fun incrementProcessingCount() {
        dataStore.edit {
            it[PROCESSING_COUNT] = (it[PROCESSING_COUNT] ?: 0) + 1
        }
    }

    suspend fun initUser() {
        dataStore.edit { prefs ->
            if (!prefs.contains(FREE_CREDITS)) prefs[FREE_CREDITS] = DEFAULT_CREDITS
            if (!prefs.contains(PROCESSING_COUNT)) prefs[PROCESSING_COUNT] = 0
        }
    }
}
