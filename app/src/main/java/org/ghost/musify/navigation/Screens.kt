package org.ghost.musify.navigation

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
}