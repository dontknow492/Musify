package org.ghost.musify.ui.screens.songs

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.ui.components.PlayerBottomAppBar
import org.ghost.musify.ui.components.SongGroupWindow
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.data.SongWindowData
import org.ghost.musify.ui.models.SongsCategory
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.songs.AlbumSongsViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AlbumSongs(
    modifier: Modifier = Modifier,
    viewModel: AlbumSongsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onSongClick: (Long, SongFilter, Boolean, Int) -> Unit,
    onMenuClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    DynamicThemeFromImage(
        uiState.album?.albumImageUriId ?: uiState.album?.albumImageUrl
        ?: uiState.albumImageUri?.let { it as Any } ?: Any(),
    ) {
        val albumData = SongWindowData(
            songs = uiState.songs.collectAsLazyPagingItems(),
            title = uiState.album?.title ?: "Unknown",
            body = uiState.album?.artist ?: "Unknown",
            image = ImageRequest.Builder(LocalContext.current)
                .data(uiState.albumImageUri)
                .crossfade(true)
                .build(),
            backgroundImage = ImageRequest.Builder(LocalContext.current)
                .data(uiState.albumImageUri)
                .crossfade(true)
                .build(),
            count = uiState.songsCount,
            type = "musics",
            headingTitle = "Songs",
            search = uiState.searchQuery,

            )

        Log.d("AlbumSongs", "AlbumSongs: $albumData")


        SongGroupWindow(
            modifier = modifier,
            data = albumData,
            bottomAppBar = {
                PlayerBottomAppBar(
                    playerViewModel = playerViewModel,
                    onClick = {}
                )
            },
            onPlayClick = {
                if (uiState.album == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Album(uiState.album!!.id)),
                    false,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onCardClick = {
                if (uiState.album == null) return@SongGroupWindow
                onSongClick(
                    it,
                    SongFilter(category = SongsCategory.Album(uiState.album!!.id)),
                    false,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onShuffleClick = {
                if (uiState.album == null) return@SongGroupWindow
                onSongClick(
                    -1L,
                    SongFilter(category = SongsCategory.Album(uiState.album!!.id)),
                    true,
                    Player.REPEAT_MODE_OFF,
                )
            },
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onSearchChange = viewModel::setSearchQuery,
        ) {
            SortFilterSheet(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                availableSortBy = viewModel.availableSortBy,
                onSortChange = viewModel::changeSortBy,
                onSortOrderChange = viewModel::changeSortOrder,
                currentSortBy = uiState.sortBy,
                currentSortOrder = uiState.sortOrder
            )
        }


    }

}

@Composable
fun SortFilterSheet(
    modifier: Modifier = Modifier,
    availableSortBy: List<SortBy>,
    onSortChange: (SortBy) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    currentSortBy: SortBy,
    currentSortOrder: SortOrder = SortOrder.ASCENDING,
) {
    val sortOrders = listOf(SortOrder.ASCENDING, SortOrder.DESCENDING)
    Column(
        modifier = modifier
    ) {
        Text(
            "Sort order",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        availableSortBy.forEach {
            CheckButton(
                label = it.name,
                checked = it == currentSortBy,
                onCheckedChange = { checked ->
                    if (checked) onSortChange(it)
                }
            )
        }
        Text(
            "Sort order",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        sortOrders.forEach {
            CheckButton(
                label = it.name,
                checked = it == currentSortOrder,
                onCheckedChange = { checked ->
                    if (checked) onSortOrderChange(it)
                }
            )
        }

    }
}


@Composable
fun CheckButton(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.clickable(
            onClick = { onCheckedChange(!checked) }
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}