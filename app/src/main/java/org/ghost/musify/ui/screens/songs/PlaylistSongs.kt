package org.ghost.musify.ui.screens.songs

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.ghost.musify.ui.components.SongGroupWindow
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.models.SongWindowData
import org.ghost.musify.ui.models.SongsCategory
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.songs.PlaylistSongsViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlaylistSongs(
    modifier: Modifier = Modifier,
    viewModel: PlaylistSongsViewModel = hiltViewModel(),
    onSongClick: (Long, SongFilter, Boolean, Int) -> Unit,
    onMenuClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val imageData =
        uiState.playlist?.playlistImageUriId ?: uiState.playlist?.playlistImageUrl ?: Any()
    DynamicThemeFromImage(
        imageData,
    ) {
        val playlistData = SongWindowData(
            songs = uiState.songs.collectAsLazyPagingItems(),
            title = uiState.playlist?.name ?: "Unknown",
            body = uiState.playlist?.description ?: "Unknown",
            image = ImageRequest.Builder(LocalContext.current)
                .data(imageData)
                .crossfade(true)
                .build(),
            backgroundImage = ImageRequest.Builder(LocalContext.current)
                .data(imageData)
                .crossfade(true)
                .build(),
            count = uiState.songsCount,
            type = "musics",
            headingTitle = "Songs",
            search = uiState.searchQuery,

        )

        Log.d("Playlist Songs", "Playlist Songs: $playlistData")

        SongGroupWindow(
            modifier = Modifier,
            data = playlistData,
            onPlayClick = {
                if (uiState.playlist == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Playlist(uiState.playlist!!.id)),
                    false,
                    0,
                )
            },
            onCardClick = {
                if (uiState.playlist == null) return@SongGroupWindow
                onSongClick(
                    it,
                    SongFilter(category = SongsCategory.Playlist(uiState.playlist!!.id)),
                    false,
                    0,
                )
            },
            onShuffleClick = {
                if (uiState.playlist == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Playlist(uiState.playlist!!.id)),
                    true,
                    0,
                )
            },
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onSearchChange = viewModel::setSearchQuery
        ){
            SortFilterSheet(
                availableSortBy = viewModel.availableSortBy,
                onSortChange = viewModel::changeSortBy,
                onSortOrderChange = viewModel::changeSortOrder,
                currentSortBy = uiState.sortBy,
                currentSortOrder = uiState.sortOrder
            )
        }

    }
}