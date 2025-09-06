package org.ghost.musify.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.utils.getAlbumArtUri
import org.ghost.musify.viewModels.home.AlbumWithCover
import org.ghost.musify.viewModels.home.ArtistWithCover
import org.ghost.musify.viewModels.home.PlaylistWithCover
import javax.inject.Inject


data class SearchUiState(
    val searchQuery: String = "",
    val searchHistory: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val songs: List<SongWithAlbumAndArtist> = emptyList(),
    val playlists: List<PlaylistWithCover> = emptyList(),
    val artists: List<ArtistWithCover> = emptyList(),
    val albums: List<AlbumWithCover> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    @param: ApplicationContext private val context: Context
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
                    val songsDeferred = async { musicRepository.filterSongsList(query = query) }
                    val playlistsDeferred = async {
                        musicRepository.getAllPlaylistAsList(query).map { playlist ->
                            PlaylistWithCover(
                                playlist = playlist,
                                cover = ImageRequest.Builder(context)
                                    .data(
                                        playlist.playlistImageUriId ?: playlist.playlistImageUrl
                                        ?: null
                                    )
                                    .crossfade(true)
                                    .placeholder(R.drawable.playlist_placeholder)
                                    .error(R.drawable.playlist_placeholder)
                                    .build()
                            )

                        }
                    }
                    val artistsDeferred = async {
                        musicRepository.getAllArtistImageAsList(query).map { artist ->
                            ArtistWithCover(
                                artist = artist,
                                cover = ImageRequest.Builder(context)
                                    .data(artist.imageUriId ?: artist.imageUrl)
                                    .crossfade(true)
                                    .placeholder(R.drawable.artist_placeholder)
                                    .error(R.drawable.artist_placeholder)
                                    .build()
                            )
                        }
                    }
                    val albumsDeferred = async {
                        musicRepository.getAllAlbumsAsList(query).map { album ->
                            AlbumWithCover(
                                album = album,
                                cover = ImageRequest.Builder(context)
                                    .data(
                                        album.albumImageUriId ?: album.albumImageUrl
                                        ?: getAlbumArtUri(album.id)
                                    )
                                    .crossfade(true)
                                    .placeholder(R.drawable.music_album_icon_2)
                                    .error(R.drawable.music_album_icon_2)
                                    .build()
                            )
                        }

                    }

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