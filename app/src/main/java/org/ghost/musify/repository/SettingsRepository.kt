package org.ghost.musify.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghost.musify.AppSettings
import org.ghost.musify.SettingsKeys
import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction
import org.ghost.musify.enums.NotificationStyle
import org.ghost.musify.enums.StartScreen
import org.ghost.musify.enums.Theme
import javax.inject.Inject


// Create a DataStore instance using a property delegate
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository @Inject constructor(private val context: Context) {

    private val dataStore = context.settingsDataStore

    // A single Flow that emits the complete AppSettings object whenever any setting changes
    val appSettingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        mapAppSettings(preferences)
    }

    // --- Update Functions ---

    // General
    suspend fun updateTheme(theme: Theme) {
        dataStore.edit { it[SettingsKeys.THEME] = theme.name }
    }

    suspend fun updateUseMaterialYou(use: Boolean) {
        dataStore.edit { it[SettingsKeys.MATERIAL_YOU] = use }
    }

    suspend fun updateAccentColor(color: Int) {
        dataStore.edit { it[SettingsKeys.ACCENT_COLOR] = color }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { it[SettingsKeys.LANGUAGE] = language }
    }

    suspend fun updateStartScreen(screen: StartScreen) {
        dataStore.edit { it[SettingsKeys.START_SCREEN] = screen.name }
    }

    // Audio & Playback
    suspend fun updateCrossfadeEnabled(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.CROSSFADE_ENABLED] = enabled }
    }

    suspend fun updateCrossfadeDuration(seconds: Int) {
        dataStore.edit { it[SettingsKeys.CROSSFADE_DURATION] = seconds }
    }

    suspend fun updateGaplessPlayback(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun updateAudioFocusSetting(setting: AudioFocus) {
        dataStore.edit { it[SettingsKeys.AUDIO_FOCUS] = setting.name }
    }

    suspend fun updateHeadsetPlugAction(action: HeadsetPlugAction) {
        dataStore.edit { it[SettingsKeys.HEADSET_PLUG_ACTION] = action.name }
    }

    suspend fun updateBluetoothAutoplay(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.BLUETOOTH_AUTOPLAY] = enabled }
    }

    // Library & Metadata
    suspend fun updateMusicFolders(folders: Set<String>) {
        dataStore.edit { it[SettingsKeys.MUSIC_FOLDERS] = folders }
    }

    suspend fun updateAutomaticScanning(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.AUTOMATIC_SCANNING] = enabled }
    }

    suspend fun updateIgnoreShortTracks(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.IGNORE_SHORT_TRACKS] = enabled }
    }

    suspend fun updateIgnoreShortTracksDuration(seconds: Int) {
        dataStore.edit { it[SettingsKeys.IGNORE_SHORT_TRACKS_DURATION] = seconds }
    }

    suspend fun updateDownloadAlbumArt(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.DOWNLOAD_ALBUM_ART] = enabled }
    }

    suspend fun updatePreferEmbeddedArt(prefer: Boolean) {
        dataStore.edit { it[SettingsKeys.PREFER_EMBEDDED_ART] = prefer }
    }

    suspend fun updateDownloadOnWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { it[SettingsKeys.DOWNLOAD_ON_WIFI_ONLY] = wifiOnly }
    }

    // Notifications & Widgets
    suspend fun updateNotificationStyle(style: NotificationStyle) {
        dataStore.edit { it[SettingsKeys.NOTIFICATION_STYLE] = style.name }
    }

    suspend fun updateUseColorizedNotification(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.COLORIZED_NOTIFICATION] = enabled }
    }

    suspend fun updateShowSeekBarInNotification(show: Boolean) {
        dataStore.edit { it[SettingsKeys.SEEK_BAR_IN_NOTIFICATION] = show }
    }

    // Advanced
    suspend fun updateExcludedFolders(folders: Set<String>) {
        dataStore.edit { it[SettingsKeys.EXCLUDED_FOLDERS] = folders }
    }

    // --- Private Mapper ---

    // A private function to map the raw Preferences object to our typed AppSettings data class
    private fun mapAppSettings(preferences: Preferences): AppSettings {
        val default = AppSettings() // Get default values

        // General
        val theme = Theme.valueOf(preferences[SettingsKeys.THEME] ?: default.theme.name)
        val useMaterialYou = preferences[SettingsKeys.MATERIAL_YOU] ?: default.useMaterialYou
        val accentColor = preferences[SettingsKeys.ACCENT_COLOR] ?: default.accentColor
        val language = preferences[SettingsKeys.LANGUAGE] ?: default.language
        val startScreen = StartScreen.valueOf(preferences[SettingsKeys.START_SCREEN] ?: default.startScreen.name)

        // Audio & Playback
        val crossfadeEnabled = preferences[SettingsKeys.CROSSFADE_ENABLED] ?: default.crossfadeEnabled
        val crossfadeDuration = preferences[SettingsKeys.CROSSFADE_DURATION] ?: default.crossfadeDuration
        val useGaplessPlayback = preferences[SettingsKeys.GAPLESS_PLAYBACK] ?: default.useGaplessPlayback
        val audioFocusSetting = AudioFocus.valueOf(preferences[SettingsKeys.AUDIO_FOCUS] ?: default.audioFocusSetting.name)
        val headsetPlugAction = HeadsetPlugAction.valueOf(preferences[SettingsKeys.HEADSET_PLUG_ACTION] ?: default.headsetPlugAction.name)
        val bluetoothAutoplay = preferences[SettingsKeys.BLUETOOTH_AUTOPLAY] ?: default.bluetoothAutoplay

        // Library & Metadata
        val musicFolders = preferences[SettingsKeys.MUSIC_FOLDERS] ?: default.musicFolders
        val automaticScanning = preferences[SettingsKeys.AUTOMATIC_SCANNING] ?: default.automaticScanning
        val ignoreShortTracks = preferences[SettingsKeys.IGNORE_SHORT_TRACKS] ?: default.ignoreShortTracks
        val ignoreShortTracksDuration = preferences[SettingsKeys.IGNORE_SHORT_TRACKS_DURATION] ?: default.ignoreShortTracksDuration
        val downloadAlbumArt = preferences[SettingsKeys.DOWNLOAD_ALBUM_ART] ?: default.downloadAlbumArt
        val preferEmbeddedArt = preferences[SettingsKeys.PREFER_EMBEDDED_ART] ?: default.preferEmbeddedArt
        val downloadOnWifiOnly = preferences[SettingsKeys.DOWNLOAD_ON_WIFI_ONLY] ?: default.downloadOnWifiOnly

        // Notifications & Widgets
        val notificationStyle = NotificationStyle.valueOf(preferences[SettingsKeys.NOTIFICATION_STYLE] ?: default.notificationStyle.name)
        val useColorizedNotification = preferences[SettingsKeys.COLORIZED_NOTIFICATION] ?: default.useColorizedNotification
        val showSeekBarInNotification = preferences[SettingsKeys.SEEK_BAR_IN_NOTIFICATION] ?: default.showSeekBarInNotification

        // Advanced
        val excludedFolders = preferences[SettingsKeys.EXCLUDED_FOLDERS] ?: default.excludedFolders

        return AppSettings(
            theme = theme,
            useMaterialYou = useMaterialYou,
            accentColor = accentColor,
            language = language,
            startScreen = startScreen,
            crossfadeEnabled = crossfadeEnabled,
            crossfadeDuration = crossfadeDuration,
            useGaplessPlayback = useGaplessPlayback,
            audioFocusSetting = audioFocusSetting,
            headsetPlugAction = headsetPlugAction,
            bluetoothAutoplay = bluetoothAutoplay,
            musicFolders = musicFolders,
            automaticScanning = automaticScanning,
            ignoreShortTracks = ignoreShortTracks,
            ignoreShortTracksDuration = ignoreShortTracksDuration,
            downloadAlbumArt = downloadAlbumArt,
            preferEmbeddedArt = preferEmbeddedArt,
            downloadOnWifiOnly = downloadOnWifiOnly,
            notificationStyle = notificationStyle,
            useColorizedNotification = useColorizedNotification,
            showSeekBarInNotification = showSeekBarInNotification,
            excludedFolders = excludedFolders
        )
    }
}