package org.ghost.musify.ui.screens.states

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist


data class AlbumSongsUiState(
    val album: AlbumEntity? = null,
    val albumImageUri: Uri? = null,
    val songs: Flow<PagingData<SongWithAlbumAndArtist>> = emptyFlow(),
    val songsCount: Int = 0,
    val isPlaying: Boolean = false,
    val isShuffled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)