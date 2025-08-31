package org.ghost.musify.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ghost.musify.Song
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.screens.components.AlbumItem
import org.ghost.musify.ui.screens.components.ArtistItem
import org.ghost.musify.ui.screens.components.PlaylistItem
import org.ghost.musify.ui.screens.components.SongItem
import org.ghost.musify.viewModels.SearchUiState
import org.ghost.musify.viewModels.SearchViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier) {
        SearchBar(
            modifier = Modifier.fillMaxWidth().padding(WindowInsets.statusBars.asPaddingValues()),
            query = uiState.searchQuery,
            // FIX 1: Correctly wire up the ViewModel events
            onSearch = viewModel::onSearchTriggered,
            onQueryChange = viewModel::onSearchQueryChanged,
            searchResults = uiState.searchHistory,
        )

        // Only show results if not loading
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SearchResultsContent(uiState)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultsContent(uiState: SearchUiState) {
    val cardSize: DpSize = DpSize(140.dp, 170.dp)
    val hasResults = uiState.songs.isNotEmpty() || uiState.playlists.isNotEmpty() ||
            uiState.artists.isNotEmpty() || uiState.albums.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        // Show "No Results" message if applicable
        if (!hasResults && uiState.searchQuery.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found for \"${uiState.searchQuery}\"")
                }
            }
        }

        // Section for Songs
        songsSection(
            songs = uiState.songs,
            onSongClick = { /* TODO: Handle song click */ },
            onSongMenuClick = { /* TODO: Handle song menu click */ }
        )

        // Section for Playlists
        horizontalScrollableSection(
            title = "Playlists",
            items = uiState.playlists,
            key = { it.id },
            itemContent = { playlist ->
                PlaylistItem(
                    modifier = Modifier.size(cardSize),
                    playlist = playlist,
                )
            }
        )

        // Section for Artists
        horizontalScrollableSection(
            title = "Artists",
            items = uiState.artists,
            key = { it.name },
            itemContent = { artist ->
                ArtistItem(
                    modifier = Modifier.size(cardSize),
                    artist = artist,
                )
            }
        )

        // Section for Albums
        horizontalScrollableSection(
            title = "Albums",
            items = uiState.albums,
            key = { it.id },
            itemContent = { album ->
                AlbumItem(
                    modifier = Modifier.size(cardSize),
                    album = album,
                )
            }
        )
    }
}

/**
 * A reusable helper to display the vertical list of songs.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun LazyListScope.songsSection(
    songs: List<SongWithAlbumAndArtist>,
    onSongClick: (SongWithAlbumAndArtist) -> Unit,
    onSongMenuClick: (SongWithAlbumAndArtist) -> Unit
) {
    if (songs.isNotEmpty()) {
        item {
            SearchTitle(title = "Songs")
        }
        items(items = songs, key = { it.song.id }) { song ->
            SongItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                songWithAlbumAndArtist = song,
                onCardClick = { },
                onMenuCLick = { }
            )
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
        }
    }
}

/**
 * A generic, reusable helper to display a horizontal LazyRow for any type of content.
 */
private fun <T> LazyListScope.horizontalScrollableSection(
    title: String,
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isNotEmpty()) {
        item {
            SearchTitle(title = title)
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(items = items, key = key) { item ->
                    itemContent(item)
                }
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier
            .semantics { traversalIndex = 0f },
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    onSearch(query)
                    expanded = false
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Search songs, artists, albums...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            // FIX 1: Make the clear icon clickable
                            modifier = Modifier.clickable { onQueryChange("") }
                        )
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = { expanded = it },
    ) {
        // FIX 3: Use LazyColumn for better performance with lists
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(300.dp),
        ) {
            items(searchResults) { result ->
                ListItem(
                    headlineContent = { Text(result) },
                    modifier = Modifier
                        .clickable {
                            // FIX 2: Correctly handle the click on a search result
                            onQueryChange(result)
                            onSearch(result)
                            expanded = false
                        }
                        .fillMaxWidth()
                )
            }
        }
    }

}

@Composable
private fun SearchTitle(modifier: Modifier = Modifier, title: String) {
    Text(
        text = title,
        modifier = modifier.padding(bottom = 8.dp),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
