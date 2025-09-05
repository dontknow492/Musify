package org.ghost.musify.data

import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction
import org.ghost.musify.enums.NotificationStyle
import org.ghost.musify.enums.StartScreen
import org.ghost.musify.enums.Theme

data class AppSettings(
    // General
    val theme: Theme = Theme.SYSTEM_DEFAULT,
    val useMaterialYou: Boolean = true,
    val accentColor: Int = 0xFF6200EE.toInt(), // Default purple color
    val language: String = "system",
    val startScreen: StartScreen = StartScreen.Home,

    // Audio & Playback
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 5, // in seconds
    val useGaplessPlayback: Boolean = true,
    val audioFocusSetting: AudioFocus = AudioFocus.PAUSE_ON_INTERRUPTION,
    val headsetPlugAction: HeadsetPlugAction = HeadsetPlugAction.RESUME_PLAYBACK,
    val bluetoothAutoplay: Boolean = true,

    // Library & Metadata
    val musicFolders: Set<String> = emptySet(),
    val automaticScanning: Boolean = true,
    val ignoreShortTracks: Boolean = true,
    val ignoreShortTracksDuration: Int = 30, // in seconds
    val downloadAlbumArt: Boolean = true,
    val preferEmbeddedArt: Boolean = true,
    val downloadOnWifiOnly: Boolean = true,

    // Notifications & Widgets
    val notificationStyle: NotificationStyle = NotificationStyle.STANDARD,
    val useColorizedNotification: Boolean = true,
    val showSeekBarInNotification: Boolean = true,

    // Advanced
    val excludedFolders: Set<String> = emptySet()
)