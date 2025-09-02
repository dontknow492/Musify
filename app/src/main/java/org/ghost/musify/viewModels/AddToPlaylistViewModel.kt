package org.ghost.musify.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

data class PlaylistForDialog(
    val id: Long,
    val name: String,
    val isSongInPlaylist: Boolean
)

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val repository: MusicRepository,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val songId: Long = savedStateHandle.get<Long>("songId")!!

    private val songPlaylistIdsFlow = repository.getPlaylistIdsForSong(songId)

    val playlistsPagingFlow: Flow<PagingData<PlaylistForDialog>> =
    // 1. Directly use the PagingData flow from the repository.
        //    No need to create a Pager here.
        repository.getAllPlaylists()

            // 2. The rest of the logic is identical: combine and map.
            .combine(songPlaylistIdsFlow) { pagingData, songPlaylistIds ->
                pagingData.map { playlistEntity ->
                    PlaylistForDialog(
                        id = playlistEntity.id,
                        name = playlistEntity.name,
                        isSongInPlaylist = songPlaylistIds.contains(playlistEntity.id)
                    )
                }
            }
            .cachedIn(viewModelScope)


    // This function will be called when a user clicks a checkbox
    fun onPlaylistToggled(playlistId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            if (isChecked) {
                repository.addSongToPlaylist(playlistId = playlistId, songId = songId)
            } else {
                repository.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
            }
        }
    }

    fun applyPlaylistChanges(finalSelections: Map<Long, Boolean>) {
        viewModelScope.launch {
            // We will get the original state to compare against
            val originalPlaylistIds = repository.getPlaylistIdsForSong(songId).first()

            // Determine what was added and what was removed
            finalSelections.forEach { (playlistId, isSelected) ->
                val wasOriginallyInPlaylist = originalPlaylistIds.contains(playlistId)

                if (isSelected && !wasOriginallyInPlaylist) {
                    // It was selected, but wasn't in the playlist before -> ADD
                    repository.addSongToPlaylist(playlistId = playlistId, songId = songId)
                } else if (!isSelected && wasOriginallyInPlaylist) {
                    // It's not selected, but was in the playlist before -> REMOVE
                    repository.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
                }
            }
        }
    }

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