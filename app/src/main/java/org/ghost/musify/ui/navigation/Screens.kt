package org.ghost.musify.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavScreen {

    // Group 1: Screens shown before the main app is visible
    @Serializable
    sealed interface Launch : NavScreen {
        @Serializable
        data object Splash : Launch
        @Serializable
        data object Onboarding : Launch
    }

    // Group 2: The main screens of the app, likely in a BottomNavBar
    @Serializable
    sealed interface Main : NavScreen {
        @Serializable
        data object Home : Main
        @Serializable
        data object Search : Main
        @Serializable
        data object History : Main
    }

    // Group 3: Screens for displaying lists of songs
    @Serializable
    sealed interface Songs : NavScreen {
        @Serializable
        data class Album(val albumId: Long) : Songs
        @Serializable
        data class Artist(val artistName: String) : Songs
        @Serializable
        data class Playlist(val playlistId: Long) : Songs
    }

    // Group 4: The full-screen media player
    @Serializable
    data class Player(val songId: Long? = null) : NavScreen

    // Group 5: Dialogs that can be shown over other screens
    @Serializable
    sealed interface Dialogs : NavScreen {
        @Serializable
        data class SongMenu(val songId: Long) : Dialogs
        @Serializable
        data class AddToPlaylist(val songId: Long) : Dialogs
    }

    // Group 6: The settings section, which can have its own nested navigation
    @Serializable
    sealed interface Settings : NavScreen {
        @Serializable
        data object Main : Settings // The main settings screen
        @Serializable
        data object General : Settings
        @Serializable
        data object Audio : Settings
        @Serializable
        data object Library : Settings
        @Serializable
        data object Notifications : Settings
        @Serializable
        data object Advanced : Settings
    }
}


