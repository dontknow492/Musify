package org.ghost.musify.viewModels.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ghost.musify.R
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject


data class PlaylistWithCover(
    val playlist: PlaylistEntity,
    val cover: ImageRequest
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: MusicRepository,
    @param: ApplicationContext private val context: Context
) : ViewModel() {
    val playlists = repository.getAllPlaylists().map { pagingData ->
        pagingData.map { playlist ->
            PlaylistWithCover(
                playlist = playlist,
                cover = ImageRequest.Builder(context)
                    .data(playlist.playlistImageUriId ?: playlist.playlistImageUrl ?: null)
                    .crossfade(true)
                    .placeholder(R.drawable.playlist_placeholder)
                    .error(R.drawable.playlist_placeholder)
                    .build()
            )
        }
    }.cachedIn(viewModelScope)


    fun createPlaylist(
        name: String,
        description: String,
        playlistImageUriId: Long?,
        playlistImageUrl: String?,

        ) {
        viewModelScope.launch {
            repository.createPlaylist(
                PlaylistEntity(
                    name = name,
                    description = description,
                    playlistImageUriId = playlistImageUriId,
                    playlistImageUrl = playlistImageUrl
                )
            )
        }
    }

}