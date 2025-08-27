package org.ghost.musify.viewModels.songs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.screens.states.PlaylistSongsUiState
import javax.inject.Inject

@HiltViewModel
class PlaylistSongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val playlistIdFlow: StateFlow<Long?> = savedStateHandle.getStateFlow("playlistId", null)

    private val _isPlaying = MutableStateFlow(false)
    private val _isShuffled = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val playlistSongsPagingFLow: Flow<PagingData<SongWithAlbumAndArtist>> = playlistIdFlow
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getPlaylistSongs(playlistId = id)
        }
        .cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlaylistSongsUiState> =
        // Use combine to merge multiple data sources
        combine(
            playlistIdFlow, // Trigger updates when the ID changes
            _isPlaying,
            _isShuffled
        ) { id, isPlaying, isShuffled ->
            // Create a temporary state holder
            Triple(id, isPlaying, isShuffled)
        }
            .flatMapLatest { (id, isPlaying, isShuffled) ->
                // If ID is null, we are in a loading or uninitialized state
                if (id == null) {
                    return@flatMapLatest flowOf(PlaylistSongsUiState(isLoading = true))
                }

                // When we have an ID, combine the flows from the repository
                combine(
                    repository.getPlaylistById(id),
                    repository.getPlaylistSongsCount(id)
                ) { playlist, count ->
                    PlaylistSongsUiState(
                        playlist = playlist,
                        songsCount = count,
                        songs = playlistSongsPagingFLow, // Assign the paging flow here
                        isPlaying = isPlaying,
                        isShuffled = isShuffled,
                        isLoading = false // Data has loaded
                    )
                }
            }
            .catch { e ->
                // In case of an error from the repository flows
                emit(PlaylistSongsUiState(isLoading = false, error = e.localizedMessage))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                // The initial state before any flows have emitted
                initialValue = PlaylistSongsUiState(isLoading = true)
            )

    // --- Public functions to modify the state from the UI ---

    fun togglePlayPause() {
        _isPlaying.update { !it }
    }

    fun toggleShuffle() {
        _isShuffled.update { !it }
    }

}