package org.ghost.musify.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavScreen {

    @Serializable
    object Home : NavScreen

    @Serializable
    object Search : NavScreen

    @Serializable
    object History : NavScreen

    @Serializable
    object Setting : NavScreen

    @Serializable
    data class AlbumSongs(
        val albumId: Long
    ) : NavScreen

    @Serializable
    data class ArtistSongs(
        val artistName: String
    ) : NavScreen

    @Serializable
    data class PlaylistSongs(
        val playlistId: Long
    ) : NavScreen

    @Serializable
    data class PlayerScreen(val songId: Long? = null) : NavScreen

    @Serializable
    data class SongMenu(val songId: Long) : NavScreen

    @Serializable
    data class AddToPlaylist(val songId: Long) : NavScreen
}


interface SettingScreen : NavScreen {
    @Serializable
    object GeneralSettings : SettingScreen

    @Serializable
    object AudioSettings : SettingScreen

    @Serializable
    object LibrarySettings : SettingScreen

    @Serializable
    object NotificationsSettings : SettingScreen

    @Serializable
    object AdvancedSettings : SettingScreen
}