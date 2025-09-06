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
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.states.PlaylistSongsUiState
import javax.inject.Inject

@HiltViewModel
class PlaylistSongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val availableSortBy = listOf(
        SortBy.TITLE,
        SortBy.DURATION,
        SortBy.ADDED_AT,
        SortBy.DATE_MODIFIED,
        SortBy.DATE_ADDED,
        SortBy.YEAR
    )
    val playlistIdFlow: StateFlow<Long?> = savedStateHandle.getStateFlow("playlistId", null)

    private val _isPlaying = MutableStateFlow(false)
    private val _isShuffled = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")

    private val _sortBy = MutableStateFlow(SortBy.TITLE)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val playlistSongsPagingFLow: Flow<PagingData<SongWithAlbumAndArtist>> =
        combine(
            playlistIdFlow.filterNotNull(),
            _searchQuery,
            _sortBy,
            _sortOrder
        ) { id, query, sortBy, sortOrder ->
            SongRequestParameters(
                id = id,
                query = query,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
        }
            .filterNotNull()
            .flatMapLatest { songRequestParameters ->
                repository.filterSongs(
                    query = _searchQuery.value,
                    sortBy = _sortBy.value,
                    sortOrder = _sortOrder.value,
                    playlistId = songRequestParameters.id,
                )
            }
            .cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlaylistSongsUiState> = playlistIdFlow
        .flatMapLatest { playlistId ->
            if (playlistId == null) {
                return@flatMapLatest flowOf(PlaylistSongsUiState(isLoading = true))
            }
            combine(
                repository.getPlaylistById(playlistId), // Trigger updates when the ID changes
                _isPlaying,
                _isShuffled,
                _searchQuery,
                _sortBy,
                _sortOrder,
                repository.getPlaylistSongsCount(playlistId),
            ) { values ->
                val playlist = values[0] as PlaylistEntity?
                val isPlaying = values[1] as Boolean
                val isShuffled = values[2] as Boolean
                val searchQuery = values[3] as String
                val sortBy = values[4] as SortBy
                val sortOrder = values[5] as SortOrder
                val count = values[6] as Int

                PlaylistSongsUiState(
                    playlist = playlist,
                    songsCount = count,
                    songs = playlistSongsPagingFLow, // Assign the paging flow here
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    sortBy = sortBy,
                    isPlaying = isPlaying,
                    isShuffled = isShuffled,
                    isLoading = false // Data has loaded
                )
            }
        }
        // Use combine to merge multiple data sources
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

    fun setSearchQuery(query: String) {
        _searchQuery.update {
            query
        }
    }

    fun changeSortOrder(sortOrder: SortOrder) {
        _sortOrder.update { sortOrder }
    }

    fun changeSortBy(sortBy: SortBy) {
        _sortBy.update { sortBy }

    }

}