package org.ghost.musify.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class OnboardingManager(private val context: Context) {

    // Define a key for our boolean flag
    companion object {
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    // A Flow that emits true if onboarding is completed, false otherwise.
    // The .map operator transforms the data from a nullable Boolean to a non-nullable Boolean.
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    // A suspend function to update the flag in DataStore.
    // This must be called from a coroutine.
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { settings ->
            settings[ONBOARDING_COMPLETED_KEY] = true
        }
    }
}