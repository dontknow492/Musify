package org.ghost.musify.ui.screens.songs

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import org.ghost.musify.ui.screens.models.SongWindowData
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.songs.PlaylistSongsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlaylistSongs(
    modifier: Modifier = Modifier,
    viewModel: PlaylistSongsViewModel = hiltViewModel()
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
            headingTitle = "Songs"

        )

        Log.d("Playlist Songs", "Playlist Songs: $playlistData")

        Scaffold { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            SongList(
                modifier = modifier,
                data = playlistData,
                onPlayClick = {},
                onCardClick = {},
                onFilterClick = {},
                onShuffleClick = {},
            )
        }
    }
}