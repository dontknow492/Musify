package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject


data class SearchUiState(
    val searchQuery: String = "",
    val searchHistory: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val songs: List<SongWithAlbumAndArtist> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val artists: List<ArtistImageEntity> = emptyList(),
    val albums: List<AlbumEntity> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // A separate flow for the search query to apply debouncing.
    private val searchQueryFlow = MutableStateFlow("")

    init {
        // This block listens to the query flow, waits for 300ms of inactivity,
        // and then triggers the actual search. This is the debounce mechanism.
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L) // Wait for 300ms of silence before searching
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    /**
     * Called by the UI whenever the search input text changes.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    /**
     * Called when the user explicitly submits the search (e.g., from the keyboard).
     * This also adds the query to the search history.
     */
    fun onSearchTriggered(query: String) {
        if (query.isBlank()) return

        val currentHistory = _uiState.value.searchHistory.toMutableSet()
        currentHistory.add(query.trim())
        _uiState.update { it.copy(searchHistory = currentHistory.toList()) }

        // Trigger the search immediately without waiting for the debounce.
        performSearch(query)
    }

    /**
     * Performs the actual search by calling repository functions concurrently.
     */
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            // If the query is empty, clear the results.
            _uiState.update {
                it.copy(
                    songs = emptyList(),
                    playlists = emptyList(),
                    artists = emptyList(),
                    albums = emptyList(),
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Run all search queries in parallel for better performance.
                coroutineScope {
                    val songsDeferred = async { musicRepository.getAllSongsList(query) }
                    val playlistsDeferred = async { musicRepository.getAllPlaylistAsList(query) }
                    val artistsDeferred = async { musicRepository.getAllArtistImageAsList(query) }
                    val albumsDeferred = async { musicRepository.getAllAlbumsAsList(query) }

                    // Await the results and update the UI state.
                    _uiState.update {
                        it.copy(
                            songs = songsDeferred.await(),
                            playlists = playlistsDeferred.await(),
                            artists = artistsDeferred.await(),
                            albums = albumsDeferred.await(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load search results.",
                        isLoading = false
                    )
                }
            }
        }
    }
}