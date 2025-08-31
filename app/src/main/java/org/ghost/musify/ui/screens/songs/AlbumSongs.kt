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
import org.ghost.musify.ui.screens.common.SongList
import org.ghost.musify.ui.screens.models.SongFilter
import org.ghost.musify.ui.screens.models.SongWindowData
import org.ghost.musify.ui.screens.models.SongsCategory
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.songs.AlbumSongsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlbumSongs(
    modifier: Modifier = Modifier,
    viewModel: AlbumSongsViewModel = hiltViewModel(),
    onSongClick: (Long, SongFilter) -> Unit,
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
            headingTitle = "Songs"

        )

        Log.d("AlbumSongs", "AlbumSongs: $albumData")


        SongList(
            modifier = modifier,
            data = albumData,
            onPlayClick = {},
            onCardClick = {
                if(uiState.album == null) return@SongList
                onSongClick(it, SongFilter(category = SongsCategory.Album(uiState.album!!.id)))
            },
            onFilterClick = {},
            onShuffleClick = {},
        )


    }


}


