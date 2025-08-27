package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {
    val playlists = repository.getAllPlaylists().cachedIn(viewModelScope)


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