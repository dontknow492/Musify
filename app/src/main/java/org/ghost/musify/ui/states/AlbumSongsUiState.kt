package org.ghost.musify.ui.states

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder


data class AlbumSongsUiState(
    val album: AlbumEntity? = null,
    val albumImageUri: Uri? = null,
    val songs: Flow<PagingData<SongWithAlbumAndArtist>> = emptyFlow(),

    val searchQuery: String = "",
    val songsCount: Int = 0,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val sortBy: SortBy = SortBy.TITLE,
    val isPlaying: Boolean = false,
    val isShuffled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)