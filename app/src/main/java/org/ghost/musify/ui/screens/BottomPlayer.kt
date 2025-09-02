package org.ghost.musify.ui.screens

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import org.ghost.musify.R
import org.ghost.musify.utils.cacheEmbeddedArts
import org.ghost.musify.utils.getSongUri
import org.ghost.musify.viewModels.PlayerUiState

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BottomPlayer(
    modifier: Modifier = Modifier,
    playerUiState: PlayerUiState,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onCloseCLick: () -> Unit,
) {
    val songId = playerUiState.currentSong?.song?.id ?: 0L
    val title = playerUiState.currentSong?.song?.title ?: ""
    val artist = playerUiState.currentSong?.artist?.name ?: ""
    val album = playerUiState.currentSong?.album?.title ?: ""
    val currentPosition = playerUiState.currentPosition
    val totalDuration = playerUiState.totalDuration

    val context = LocalContext.current

    // 1. Create a state variable to hold the result of your suspend function.
    //    It starts as null.
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 2. Use LaunchedEffect to run your suspend function safely.
    //    'key1 = song.uri' ensures this effect re-runs only if the song changes.
    LaunchedEffect(key1 = getSongUri(songId)) {
        imageUri = cacheEmbeddedArts(context, getSongUri(songId))
    }

    Box(
        modifier = modifier
            .background(containerColor)
            .clickable(
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .error(R.drawable.music_album_cover)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$artist - $album",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPlayPauseClick
            ) {
                when (playerUiState.isPlaying) {
                    true -> {
                        Icon(
                            painter = painterResource(R.drawable.rounded_pause_24),
                            contentDescription = "pause",
                            modifier = Modifier.size(30.dp)
                        )

                    }

                    false -> {
                        Icon(
                            painter = painterResource(R.drawable.baseline_play_arrow_24),
                            contentDescription = "play",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                }
            }
            IconButton(
                onClick = onCloseCLick
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "close",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        ProgressBar(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            currentPosition = currentPosition,
            totalDuration = totalDuration.toLong(),
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
        )
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    totalDuration: Long,
    brush: Brush,
) {
//    LinearProgressIndicator(
//        modifier = modifier,
//        progress = { currentPosition.toFloat() / totalDuration },
//    )
    Canvas(
        modifier = modifier.width(40.dp)
    ) {
        drawRect(
            brush,
            size = size.copy(width = size.width * currentPosition / totalDuration),
        )

    }
}