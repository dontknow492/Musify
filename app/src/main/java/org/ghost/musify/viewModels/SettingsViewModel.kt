package org.ghost.musify.viewModels

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ghost.musify.data.AppSettings
import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction
import org.ghost.musify.enums.NotificationStyle
import org.ghost.musify.enums.StartScreen
import org.ghost.musify.enums.Theme
import org.ghost.musify.repository.SettingsRepository

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    @param: ApplicationContext private val context: Context,
) : ViewModel() {

    // Expose the settings as a StateFlow for the UI to observe.
    // It will automatically update when the DataStore changes.
    val settingsState: StateFlow<AppSettings> = repository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stay active for 5s after UI stops observing
            initialValue = AppSettings() // Start with default values
        )



    // --- Public Functions for UI to Call ---

    // General
    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            repository.updateTheme(theme)
        }
    }

    fun setUseMaterialYou(use: Boolean) {
        viewModelScope.launch {
            repository.updateUseMaterialYou(use)
        }
    }

    fun setAccentColor(color: Int) {
        viewModelScope.launch {
            repository.updateAccentColor(color)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun setStartScreen(screen: StartScreen) {
        viewModelScope.launch {
            repository.updateStartScreen(screen)
        }
    }

    // Audio & Playback
    fun setCrossfadeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateCrossfadeEnabled(enabled)
        }
    }

    fun setCrossfadeDuration(seconds: Int) {
        viewModelScope.launch {
            repository.updateCrossfadeDuration(seconds)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateGaplessPlayback(enabled)
        }
    }

    fun setAudioFocusSetting(setting: AudioFocus) {
        viewModelScope.launch {
            repository.updateAudioFocusSetting(setting)
        }
    }

    fun setHeadsetPlugAction(action: HeadsetPlugAction) {
        viewModelScope.launch {
            repository.updateHeadsetPlugAction(action)
        }
    }

    fun setBluetoothAutoplay(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBluetoothAutoplay(enabled)
        }
    }

    // Library & Metadata
    fun setMusicFolders(folders: Set<String>) {
        viewModelScope.launch {
            repository.updateMusicFolders(folders)
        }
    }

    fun addMusicFolder(folder: String){
        viewModelScope.launch {
            val musicFolders = settingsState.value.musicFolders.toMutableSet()
            musicFolders.add(folder)
            repository.updateMusicFolders(musicFolders)
        }
    }

    fun removeMusicFolder(folder: String){
        viewModelScope.launch {
            val musicFolders = settingsState.value.musicFolders.toMutableSet()
            musicFolders.remove(folder)
            repository.updateMusicFolders(musicFolders)
        }
    }

    fun setAutomaticScanning(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutomaticScanning(enabled)
        }
    }

    fun setIgnoreShortTracks(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateIgnoreShortTracks(enabled)
        }
    }

    fun setIgnoreShortTracksDuration(seconds: Int) {
        viewModelScope.launch {
            repository.updateIgnoreShortTracksDuration(seconds)
        }
    }

    fun setDownloadAlbumArt(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDownloadAlbumArt(enabled)
        }
    }

    fun setPreferEmbeddedArt(prefer: Boolean) {
        viewModelScope.launch {
            repository.updatePreferEmbeddedArt(prefer)
        }
    }

    fun setDownloadOnWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            repository.updateDownloadOnWifiOnly(wifiOnly)
        }
    }

    fun setArtistNameSeparator(separator: String) {
        viewModelScope.launch {
            repository.updateArtistNameSeparator(separator)
        }
    }

    // Notifications & Widgets
    fun setNotificationStyle(style: NotificationStyle) {
        viewModelScope.launch {
            repository.updateNotificationStyle(style)
        }
    }

    fun setUseColorizedNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUseColorizedNotification(enabled)
        }
    }

    fun setShowSeekBarInNotification(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowSeekBarInNotification(show)
        }
    }

    // Advanced
    fun setExcludedFolders(folders: Set<String>) {
        viewModelScope.launch {
            repository.updateExcludedFolders(folders)
        }
    }

    fun addExcludedFolder(folder: String){
        viewModelScope.launch {
            val excludedFolders = settingsState.value.excludedFolders.toMutableSet()
            excludedFolders.add(folder)
            repository.updateExcludedFolders(excludedFolders)
        }
    }

    fun removeExcludedFolder(folder: String) {
        viewModelScope.launch {
            val excludedFolders = settingsState.value.excludedFolders.toMutableSet()
            excludedFolders.remove(folder)
            repository.updateExcludedFolders(excludedFolders)
        }
    }

    fun backupSettings(destinationUri: Uri) {
        viewModelScope.launch {
            val success = repository.backupSettings(destinationUri)
            when(success){
                true -> Toast.makeText(context, "Settings backed up", Toast.LENGTH_SHORT).show()
                false -> Toast.makeText(context, "Settings backup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restoreSettings(sourceUri: Uri) {
        viewModelScope.launch {
            val success = repository.restoreSettings(sourceUri)
            when(success){
                true -> Toast.makeText(context, "Settings restored(restart required)", Toast.LENGTH_SHORT).show()
                false -> Toast.makeText(context, "Settings restore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // --- ViewModel Factory (for manual dependency injection if not using Hilt/Koin) ---

    companion object {
        fun provideFactory(repository: SettingsRepository, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        return SettingsViewModel(repository, context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}