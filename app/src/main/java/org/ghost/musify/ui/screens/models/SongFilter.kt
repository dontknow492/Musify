package org.ghost.musify.ui.screens.models

import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

sealed interface SongsCategory {
    data class Artist(val artistName: String) : SongsCategory
    data class Album(val albumId: Long) : SongsCategory
    object AllSongs : SongsCategory
    data class Playlist(val playlistId: Long) : SongsCategory
    object LikedSongs : SongsCategory
}

data class SongFilter(
    val category: SongsCategory,
    val searchQuery: String? = null,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val sortBy: SortBy = SortBy.TITLE
)
