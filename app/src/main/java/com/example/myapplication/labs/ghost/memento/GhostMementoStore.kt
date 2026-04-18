package com.example.myapplication.labs.ghost.memento

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mementoDataStore: DataStore<Preferences> by preferencesDataStore(name = "ghost_memento")

/**
 * GhostMementoStore: A specialized DataStore repository for persistent command history.
 *
 * This store persists the application's undo and redo stacks as a serialized JSON string.
 * It provides the foundation for "Ghost Memento", allowing the teacher to resume their
 * work exactly where they left off, even after a full application termination.
 */
@Singleton
class GhostMementoStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val COMMAND_HISTORY = stringPreferencesKey("command_history")
    }

    /**
     * Reactive stream of the persisted command history.
     * Deserializes the JSON state into a [MementoHistory] object.
     */
    val commandHistoryFlow: Flow<MementoHistory> = context.mementoDataStore.data.map { prefs ->
        val json = prefs[Keys.COMMAND_HISTORY]
        if (json != null) {
            try {
                Json.decodeFromString<MementoHistory>(json)
            } catch (e: Exception) {
                MementoHistory()
            }
        } else {
            MementoHistory()
        }
    }

    /**
     * Atomically updates the persisted command history.
     * Serializes the [MementoHistory] into JSON before storage.
     */
    suspend fun saveHistory(history: MementoHistory) {
        val json = Json.encodeToString(history)
        context.mementoDataStore.edit { prefs ->
            prefs[Keys.COMMAND_HISTORY] = json
        }
    }

    /**
     * Clears all persisted history from the store.
     */
    suspend fun clearHistory() {
        context.mementoDataStore.edit { prefs ->
            prefs.remove(Keys.COMMAND_HISTORY)
        }
    }
}
