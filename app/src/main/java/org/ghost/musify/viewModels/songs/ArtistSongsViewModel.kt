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
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.states.ArtistSongsUiState
import javax.inject.Inject


data class SongRequestNameParameters(
    val name: String,
    val query: String,
    val sortBy: SortBy,
    val sortOrder: SortOrder
)

@HiltViewModel
class ArtistSongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    val availableSortBy = listOf(
        SortBy.TITLE,
        SortBy.DURATION,
        SortBy.ADDED_AT,
        SortBy.DATE_MODIFIED,
        SortBy.DATE_ADDED,
        SortBy.YEAR
    )
    val artistNameFlow: StateFlow<String?> = state.getStateFlow("artistName", null)
    private val _isPlaying = MutableStateFlow(false)
    private val _isShuffled = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")

    private val _sortBy = MutableStateFlow(SortBy.TITLE)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistSongsPagingFlow: Flow<PagingData<SongWithAlbumAndArtist>> =
        combine(
            artistNameFlow.filterNotNull(),
            _searchQuery,
            _sortBy,
            _sortOrder
        ) { name, query, sortBy, sortOrder ->
            SongRequestNameParameters(
                name = name,
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
                    artist = songRequestParameters.name
                )
            }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArtistSongsUiState> = artistNameFlow
        .flatMapLatest { artistName ->
            if (artistName == null) {
                return@flatMapLatest flowOf(ArtistSongsUiState(isLoading = true))
            }
            combine(
                repository.getArtistByName(artistName), // Trigger updates when the ID changes
                _isPlaying,
                _isShuffled,
                _searchQuery,
                _sortBy,
                _sortOrder,
                repository.getArtistSongsCount(artistName),
            ) { values ->
                val artist = values[0] as ArtistImageEntity?
                val isPlaying = values[1] as Boolean
                val isShuffled = values[2] as Boolean
                val searchQuery = values[3] as String
                val sortBy = values[4] as SortBy
                val sortOrder = values[5] as SortOrder
                val count = values[6] as Int

                ArtistSongsUiState(
                    artist = artist,
                    songsCount = count,
                    songs = artistSongsPagingFlow, // Assign the paging flow here
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
            emit(ArtistSongsUiState(isLoading = false, error = e.localizedMessage))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // The initial state before any flows have emitted
            initialValue = ArtistSongsUiState(isLoading = true)
        )

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