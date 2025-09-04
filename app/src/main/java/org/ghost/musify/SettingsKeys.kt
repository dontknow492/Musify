package org.ghost.musify

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object SettingsKeys {
    val THEME = stringPreferencesKey("theme")
    val MATERIAL_YOU = booleanPreferencesKey("material_you")
    val ACCENT_COLOR = intPreferencesKey("accent_color")
    val LANGUAGE = stringPreferencesKey("language")
    val START_SCREEN = stringPreferencesKey("start_screen")

    val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
    val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
    val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
    val AUDIO_FOCUS = stringPreferencesKey("audio_focus")
    val HEADSET_PLUG_ACTION = stringPreferencesKey("headset_plug_action")
    val BLUETOOTH_AUTOPLAY = booleanPreferencesKey("bluetooth_autoplay")

    val MUSIC_FOLDERS = stringSetPreferencesKey("music_folders")
    val AUTOMATIC_SCANNING = booleanPreferencesKey("automatic_scanning")
    val IGNORE_SHORT_TRACKS = booleanPreferencesKey("ignore_short_tracks")
    val IGNORE_SHORT_TRACKS_DURATION = intPreferencesKey("ignore_short_tracks_duration")
    val DOWNLOAD_ALBUM_ART = booleanPreferencesKey("download_album_art")
    val PREFER_EMBEDDED_ART = booleanPreferencesKey("prefer_embedded_art")
    val DOWNLOAD_ON_WIFI_ONLY = booleanPreferencesKey("download_on_wifi_only")

    val NOTIFICATION_STYLE = stringPreferencesKey("notification_style")
    val COLORIZED_NOTIFICATION = booleanPreferencesKey("colorized_notification")
    val SEEK_BAR_IN_NOTIFICATION = booleanPreferencesKey("seek_bar_in_notification")

    val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
}