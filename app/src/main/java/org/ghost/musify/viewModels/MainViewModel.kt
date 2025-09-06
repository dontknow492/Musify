package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.ghost.musify.data.AppSettings
import org.ghost.musify.data.OnboardingManager
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.repository.SettingsRepository
import javax.inject.Inject


sealed class MainUiState {
    data object Loading : MainUiState()
    data object NavigateToOnboarding : MainUiState()
    data object NavigateToMainApp : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val settingsState: StateFlow<AppSettings> = settingsRepository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stay active for 5s after UI stops observing
            initialValue = AppSettings() // Start with default values
        )


    init {
        // This block runs when the ViewModel is created
        viewModelScope.launch {
            Log.d("MainViewModel", "Initialization started.")

            // 1. Define all startup tasks to run in parallel.
            val syncTask = async {
                val settings = settingsRepository.appSettingsFlow.first()
                musicRepository.syncMediaStore(
                    allowedFolders = settings.musicFolders.toList(),
                    minDurationSec = if(settings.ignoreShortTracks)
                        settings.ignoreShortTracksDuration
                        else null,
                    excludedFolders = settings.excludedFolders.toList(),
                    separators = settings.artistNameSeparator.toSet()
                )
            }
            val onboardingTask = async {
                onboardingManager.isOnboardingCompleted.first()
            }

            // 2. Wait for both tasks to complete.
            val isOnboardingCompleted = onboardingTask.await()
            syncTask.await() // Wait for sync to finish, but its result isn't needed here.

            Log.d("MainViewModel", "Sync and onboarding check complete. Onboarding status: $isOnboardingCompleted")

            // 3. Update the state to trigger navigation.
            if (isOnboardingCompleted) {
                _uiState.value = MainUiState.NavigateToMainApp
            } else {
                _uiState.value = MainUiState.NavigateToOnboarding
            }
        }
        observeSettingsForResync()
    }

    @OptIn(FlowPreview::class)
    private fun observeSettingsForResync() {
        viewModelScope.launch {
            // We define the base flow once to use it in the zip
            val settingsFlow = settingsRepository.appSettingsFlow

            settingsFlow
                // 1. Zip the flow with a version of itself that skips the first element.
                //    This creates the desired (old, new) pair.
                .zip(settingsFlow.drop(1)) { oldSettings, newSettings ->
                    oldSettings to newSettings // The 'to' keyword creates a Pair
                }
                // 2. Wait for 500ms of silence before continuing.
                .debounce(500L)
                // 3. When a new pair is emitted, run our smart comparison logic.
                .collect { (oldSettings, newSettings) ->

                    // --- Smart Comparison Logic (no changes here) ---

                    // A) Check for changes that require a FULL file system sync.
                    if (oldSettings.musicFolders != newSettings.musicFolders ||
                        oldSettings.ignoreShortTracksDuration != newSettings.ignoreShortTracksDuration ||
                        oldSettings.excludedFolders != newSettings.excludedFolders ||
                        oldSettings.ignoreShortTracks != newSettings.ignoreShortTracks
                        ) {

                        Log.d("MainViewModel", "File scan settings changed, triggering FULL re-sync.")
                        musicRepository.syncMediaStore(
                            allowedFolders = newSettings.musicFolders.toList(),
                            minDurationSec = if(newSettings.ignoreShortTracks)
                                newSettings.ignoreShortTracksDuration
                            else null,
                            excludedFolders = newSettings.excludedFolders.toList(),
                            separators = newSettings.artistNameSeparator.toSet()
                        )
                    }
                    // B) If no full sync was needed, check for changes that only require a LIGHTWEIGHT re-parse.
                    else if (oldSettings.artistNameSeparator != newSettings.artistNameSeparator) {
                        Log.d("MainViewModel", "Artist separators changed, triggering re-parse.")
                        musicRepository.reparseArtists(
                            separators = newSettings.artistNameSeparator.toSet()
                        )
                    }
                }
        }
    }

    fun syncMedia() {
        Log.d("MainViewModel", "Manual syncMedia triggered.")
        viewModelScope.launch {
            try {
                // Get the most recent settings value from the flow.
                val currentSettings = settingsRepository.appSettingsFlow.first()

                Log.d("MainViewModel", "Starting sync with settings: $currentSettings")

                // Call the repository's sync function with the current settings.
                musicRepository.syncMediaStore(
                    allowedFolders = currentSettings.musicFolders.toList(),
                    minDurationSec = currentSettings.ignoreShortTracksDuration,
                    excludedFolders = currentSettings.excludedFolders.toList(),
                    separators = currentSettings.artistNameSeparator.toSet(),
                )

                Log.d("MainViewModel", "Manual sync completed successfully.")

            } catch (e: Exception) {
                // Log any errors that occur during the sync process.
                Log.e("MainViewModel", "Error during manual sync", e)
            }
        }
    }


    fun setOnboardingCompleted() {
        viewModelScope.launch {
            onboardingManager.setOnboardingCompleted()
        }

    }
}