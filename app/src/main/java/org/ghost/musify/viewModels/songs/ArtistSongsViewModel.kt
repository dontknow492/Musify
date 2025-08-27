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
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.screens.states.ArtistSongsUiState
import javax.inject.Inject


@HiltViewModel
class ArtistSongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val state: SavedStateHandle
) : ViewModel() {
    val artistNameFlow: StateFlow<String?> = state.getStateFlow("artistName", null)
    private val _isPlaying = MutableStateFlow(false)
    private val _isShuffled = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistSongsPagingFlow: Flow<PagingData<SongWithAlbumAndArtist>> = artistNameFlow
        .filterNotNull()
        .flatMapLatest { artistName ->
            repository.getAllSongs(artist = artistName)
        }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArtistSongsUiState> =
        // Use combine to merge multiple data sources
        combine(
            artistNameFlow, // Trigger updates when the ID changes
            _isPlaying,
            _isShuffled
        ) { artistName, isPlaying, isShuffled ->
            // Create a temporary state holder
            Triple(artistName, isPlaying, isShuffled)
        }
            .flatMapLatest { (artistName, isPlaying, isShuffled) ->
                // If ID is null, we are in a loading or uninitialized state
                if (artistName == null) {
                    return@flatMapLatest flowOf(ArtistSongsUiState(isLoading = true))
                }

                // When we have an ID, combine the flows from the repository
                combine(
                    repository.getArtistByName(artistName),
                    repository.getArtistSongsCount(artistName)
                ) { artist, count ->
                    ArtistSongsUiState(
                        artist = artist,
                        songsCount = count,
                        songs = artistSongsPagingFlow, // Assign the paging flow here
                        isPlaying = isPlaying,
                        isShuffled = isShuffled,
                        isLoading = false // Data has loaded
                    )
                }
            }
            .catch { e ->
                // In case of an error from the repository flows
                emit(ArtistSongsUiState(isLoading = false, error = e.localizedMessage))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                // The initial state before any flows have emitted
                initialValue = ArtistSongsUiState(isLoading = true)
            )
}