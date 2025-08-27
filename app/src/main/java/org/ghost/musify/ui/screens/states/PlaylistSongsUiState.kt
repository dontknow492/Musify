package org.ghost.musify.ui.screens.states

import android.graphics.Bitmap
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist

data class PlaylistSongsUiState(
    val playlist: PlaylistEntity? = null,
    val songs: Flow<PagingData<SongWithAlbumAndArtist>> = emptyFlow(),
    val playlistBitmap: Bitmap? = null,
    val songsCount: Int = 0,
    val isPlaying: Boolean = false,
    val isShuffled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)