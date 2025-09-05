package org.ghost.musify.ui.screens.songs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R
import org.ghost.musify.ui.components.PlayerBottomAppBar
import org.ghost.musify.ui.components.SongGroupWindow
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.models.SongWindowData
import org.ghost.musify.ui.models.SongsCategory
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.songs.ArtistSongsViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ArtistSongs(
    modifier: Modifier = Modifier,
    viewModel: ArtistSongsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onSongClick: (Long, SongFilter, Boolean, Int) -> Unit,
    onMenuClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val imageData = uiState.artist?.imageUriId ?: uiState.artist?.imageUrl ?: Any()
    DynamicThemeFromImage(
        imageData
    ) {
        val artistData = SongWindowData(
            songs = viewModel.artistSongsPagingFlow.collectAsLazyPagingItems(),
            title = uiState.artist?.name ?: "Unknown",
            body = "Unknown",
            image = ImageRequest.Builder(LocalContext.current)
                .data(imageData)
                .crossfade(true)
                .placeholder(R.drawable.artist_placeholder)
                .error(R.drawable.artist_placeholder)
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
        SongGroupWindow(
            modifier = Modifier,
            data = artistData,
            bottomAppBar = {
                PlayerBottomAppBar(
                    playerViewModel = playerViewModel,
                    onClick = {}
                )
            },
            onPlayClick = {
                if (uiState.artist == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Artist(uiState.artist!!.name)),
                    false,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onCardClick = {
                if (uiState.artist == null) return@SongGroupWindow
                onSongClick(
                    it,
                    SongFilter(category = SongsCategory.Artist(uiState.artist!!.name)),
                    false,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onShuffleClick = {
                if (uiState.artist == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Artist(uiState.artist!!.name)),
                    true,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onSearchChange = viewModel::setSearchQuery
        ) {
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


