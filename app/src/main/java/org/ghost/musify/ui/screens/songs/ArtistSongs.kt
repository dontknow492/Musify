package org.ghost.musify.ui.screens.songs

import android.os.Build
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
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R
import org.ghost.musify.ui.screens.common.SongList
import org.ghost.musify.ui.screens.models.SongWindowData
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.viewModels.songs.ArtistSongsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArtistSongs(modifier: Modifier = Modifier, viewModel: ArtistSongsViewModel = hiltViewModel()) {
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
            headingTitle = "Songs"
        )
        SongList(
            modifier = modifier,
            data = artistData,
            onPlayClick = {},
            onCardClick = {},
            onFilterClick = {},
            onShuffleClick = {}
        )
    }
}


