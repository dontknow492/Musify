package org.ghost.musify.ui.states

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

data class ArtistSongsUiState(
    val artist: ArtistImageEntity? = null,
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