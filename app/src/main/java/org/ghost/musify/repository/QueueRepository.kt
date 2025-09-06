package org.ghost.musify.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.ghost.musify.data.QueueState
import javax.inject.Inject

private val Context.queueRepository: DataStore<Preferences> by preferencesDataStore(name = "queue_state")

class QueueRepository @Inject constructor(@param: ApplicationContext private val context: Context) {

    private val QUEUE_STATE_KEY = stringPreferencesKey("queue_state")
    private val dataStore = context.queueRepository

    val queueStateFlow: Flow<QueueState> = dataStore.data.map { preferences ->
        val jsonString = preferences[QUEUE_STATE_KEY]
        jsonString?.let { Json.decodeFromString<QueueState>(it) } ?: QueueState()
    }

    suspend fun saveQueueState(queueState: QueueState) {
        dataStore.edit { preferences ->
            preferences[QUEUE_STATE_KEY] = Json.encodeToString(queueState)
        }
    }
}