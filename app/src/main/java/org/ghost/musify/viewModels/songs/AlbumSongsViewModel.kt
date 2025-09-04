package org.ghost.musify.viewModels.songs

import android.util.Log
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
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.states.AlbumSongsUiState
import org.ghost.musify.utils.getAlbumArtUri
import javax.inject.Inject


data class SongRequestParameters(
    val id: Long,
    val query: String,
    val sortBy: SortBy,
    val sortOrder: SortOrder
)

@HiltViewModel
class AlbumSongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val availableSortBy = listOf(
        SortBy.TITLE,
        SortBy.DURATION,
        SortBy.ADDED_AT,
        SortBy.DATE_MODIFIED,
        SortBy.DATE_ADDED,
        SortBy.YEAR
    )

    private val albumIdFlow: StateFlow<Long?> = savedStateHandle.getStateFlow("albumId", null)

    // Internal state for UI actions
    private val _isPlaying = MutableStateFlow(false)
    private val _isShuffled = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")

    private val _sortBy = MutableStateFlow(SortBy.TITLE)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)

    // This is the PagingData flow, derived separately
    @OptIn(ExperimentalCoroutinesApi::class)
    private val albumSongsPagingFlow: Flow<PagingData<SongWithAlbumAndArtist>> =
        combine(
            albumIdFlow.filterNotNull(),
            _searchQuery,
            _sortBy,
            _sortOrder
        ){
            id, query, sortBy, sortOrder ->
            SongRequestParameters(
                id = id,
                query = query,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
        }
        .filterNotNull()
        .flatMapLatest { songRequestParameters ->
            repository.getAllSongs(
                query = _searchQuery.value,
                sortBy = _sortBy.value,
                sortOrder = _sortOrder.value,
                albumId = songRequestParameters.id,
            )
        }
        .cachedIn(viewModelScope)

    // The single StateFlow that the UI will observe

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AlbumSongsUiState> = albumIdFlow
        .flatMapLatest { albumId ->
            if (albumId == null) {
                return@flatMapLatest flowOf(AlbumSongsUiState(isLoading = true))
            }
            combine(
                repository.getAlbum(albumId), // Trigger updates when the ID changes
                _isPlaying,
                _isShuffled,
                _searchQuery,
                _sortBy,
                _sortOrder,
                repository.getAlbumSongsCount(albumId),
            ) { values ->
                val album = values[0] as AlbumEntity?
                val isPlaying = values[1] as Boolean
                val isShuffled = values[2] as Boolean
                val searchQuery = values[3] as String
                val sortBy = values[4] as SortBy
                val sortOrder = values[5] as SortOrder
                val count = values[6] as Int

                AlbumSongsUiState(
                    album = album,
                    albumImageUri = album?.id?.let { albumId ->
                        getAlbumArtUri(albumId)
                    },
                    songsCount = count,
                    songs = albumSongsPagingFlow, // Assign the paging flow here
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    sortBy = sortBy,
                    isPlaying = isPlaying,
                    isShuffled = isShuffled,
                    isLoading = false // Data has loaded
                )
            }
        }
        .catch { e ->
            // In case of an error from the repository flows
            emit(AlbumSongsUiState(isLoading = false, error = e.localizedMessage))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // The initial state before any flows have emitted
            initialValue = AlbumSongsUiState(isLoading = true)
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
        Log.d("AlbumSongsViewModel", "setSearchQuery: $query/ ${_searchQuery.value}")
    }

    fun changeSortOrder(sortOrder: SortOrder) {
        _sortOrder.update { sortOrder }
    }

    fun changeSortBy(sortBy: SortBy) {
        _sortBy.update { sortBy }

    }
}