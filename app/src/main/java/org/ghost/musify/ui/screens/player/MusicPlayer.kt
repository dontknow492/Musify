package org.ghost.musify.ui.screens.player

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.components.SwipeableSongItem
import org.ghost.musify.ui.components.common.CircularProgressBar
import org.ghost.musify.ui.components.common.PlayPauseButton
import org.ghost.musify.ui.components.common.VolumeControlButton
import org.ghost.musify.ui.dialog.MusicInfoDialog
import org.ghost.musify.ui.dialog.menu.SongQueueMenu
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.utils.cacheEmbeddedArts
import org.ghost.musify.utils.getSongUri
import org.ghost.musify.utils.toFormattedDuration
import org.ghost.musify.viewModels.PlayerStatus
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.QueueViewModel


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayerWindow(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddToPlaylistClick: (Long) -> Unit,
) {

    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var isSongMenuVisible by remember { mutableStateOf(false) }
    var songMenuId by remember { mutableLongStateOf(-1L) }
    val onQueueListClick: () -> Unit = {
        isBottomSheetVisible = !isBottomSheetVisible
    }

    val uiState by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current




    val songId = uiState.currentSong?.songDetail?.song?.id

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(songId ?: Unit) {
        songId?.let {
            imageUri = cacheEmbeddedArts(context, getSongUri(songId))
        }
        Log.d("Player Window", "songId: $songId")
    }


    DynamicThemeFromImage(
        imageUrl = imageUri ?: Any()
    ) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    )
                    .padding(innerPadding)
            ) {
                PlayerScreenTopBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    onBackClick = onBackClick,
                    onQueueListClick = onQueueListClick
                )
                PlayerScreenItem(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    imageUri = imageUri,
                )

            }
            //
        }
        if (isBottomSheetVisible) {
            Log.d("PlayerWindows", "PlayerWindow: BottomSheet is visible")
            PlayerBottomSheet(
                modifier = Modifier,
                viewModel = hiltViewModel(),
                onDismiss = { isBottomSheetVisible = false },
                onSongRemove = { songId ->
                    viewModel.removeSongFromQueue(songId = songId)
                },
                onSongClick = { songId ->
                    viewModel.seekToSong(songId)
                },
                onSongMenuClick = { songMenuID ->
                    songMenuId = songMenuID
                    isSongMenuVisible = true
                }
            )
        }
        if (isSongMenuVisible && songMenuId != -1L) {
            SongQueueMenu(
                songId = songMenuId,
                onDismissRequest = { isSongMenuVisible = false },
                onAddToPlaylist = onAddToPlaylistClick,
                modifier = Modifier
            )
        }

    }


}






@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerScreenItem(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    onAddToPlaylistClick: (Long) -> Unit,
    imageUri: Uri?,
) {

    BoxWithConstraints(
        modifier = modifier
    ) {
        val width = maxWidth
        val height = maxHeight
        if (width - height < 200.dp) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .error(R.drawable.music_album_cover)
                        .build(),
                    contentDescription = "title",
                    modifier = Modifier
                        .weight(1.75f)
                        .padding(20.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Fit,
                )
                PlayerBar(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                )
            }

        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .error(R.drawable.artist_placeholder)
                        .build(),
                    contentDescription = "title",
                    modifier = Modifier
                        .size(350.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.FillWidth,
                )
                PlayerBar(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight(0.8f),
                    viewModel = viewModel,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                )
            }
        }


    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    onAddToPlaylistClick: (Long) -> Unit,
) {
    var isInfoDialogVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite = false
    val songDetailsWithLikeStatus: SongDetailsWithLikeStatus? = uiState.currentSong
    val songTitle = songDetailsWithLikeStatus?.songDetail?.song?.title ?: "Unknown"
    val songArtist = songDetailsWithLikeStatus?.songDetail?.artist?.name ?: "Unknown"
    val songAlbum = songDetailsWithLikeStatus?.songDetail?.album?.title ?: "Unknown"
    val iconSizeLarge = 60.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,

        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = songTitle,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = songArtist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = songAlbum,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row {
            IconButton(
                onClick = { false }
            ) {
                when (isFavorite) {
                    true -> Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = "favorite"
                    )

                    false -> Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "favorite"
                    )
                }
            }

            IconButton(
                onClick = { isInfoDialogVisible = true }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "info"
                )
            }
            IconButton(
                onClick = { onAddToPlaylistClick(uiState.currentSong?.songDetail?.song?.id ?: 0L) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "add to playlist"
                )
            }
            SpeedButton(
                speeds = viewModel.playbackSpeeds,
                onClick = viewModel::setPlaybackSpeed,
                currentSpeed = uiState.playbackSpeed
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { viewModel.toggleRepeatMode() }
            ) {
                when (uiState.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Icon(
                        painter = painterResource(R.drawable.rounded_repeat_24),
                        contentDescription = "repeat mode off"
                    )

                    Player.REPEAT_MODE_ONE -> Icon(
                        painter = painterResource(R.drawable.rounded_repeat_one_24),
                        contentDescription = "repeat mode one"
                    )

                    Player.REPEAT_MODE_ALL -> Icon(
                        painter = painterResource(R.drawable.rounded_repeat_on_24),
                        contentDescription = "repeat mode all"
                    )
                }
            }

            IconButton(
                onClick = { viewModel.toggleShuffleMode() }
            ) {
                when (uiState.isShuffleEnabled) {
                    true -> Icon(
                        painter = painterResource(R.drawable.rounded_shuffle_on_24),
                        contentDescription = "shuffle"
                    )

                    false -> Icon(
                        painter = painterResource(R.drawable.rounded_shuffle_24),
                        contentDescription = "shuffle"
                    )
                }
            }

        }


        MusicProgressBar(
            modifier = Modifier.padding(horizontal = 12.dp),
            totalDurationMs = uiState.currentSong?.songDetail?.song?.duration?.toLong() ?: 0L,
            currentDurationMs = uiState.currentPosition
        ) {
            viewModel.onSeekTo(it.toLong())
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VolumeControlButton(
                uiState.volume,
                onVolumeChange = { viewModel.setVolume(it) }
            )
            IconButton(
                onClick = { viewModel.onPreviousClicked() },
                enabled = uiState.hasPrevious
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_skip_previous_24),
                    contentDescription = "previous",
                    modifier = Modifier.size(iconSizeLarge)
                )
            }

            PlayPauseButton(
                modifier = Modifier,
                onClick = { viewModel.onPlayPauseClicked() },
                isPlaying = uiState.isPlaying,
                enable = uiState.status != PlayerStatus.BUFFERING,
                iconSize = iconSizeLarge
            )

            IconButton(
                onClick = { viewModel.onNextClicked() },
                enabled = uiState.hasNext
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_skip_next_24),
                    contentDescription = "next",
                    modifier = Modifier.size(iconSizeLarge)
                )
            }
            IconButton(
                onClick = { }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_bar_chart_24),
                    contentDescription = "modifier"
                )
            }
        }
//        Spacer(modifier = Modifier.height(16.dp))
    }

    AnimatedVisibility(isInfoDialogVisible && uiState.currentSong != null) {
        MusicInfoDialog(
            songWithAlbumAndArtist = uiState.currentSong!!.songDetail
        ) {
            isInfoDialogVisible = false
        }
    }
}

@Composable
fun SpeedButton(
    modifier: Modifier = Modifier,
    currentSpeed: Float,
    speeds: List<Float>,
    onClick: (Float) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    TextButton(
        onClick = { isExpanded = !isExpanded }
    ) {
        Text(
            text = "$currentSpeed X",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        if (isExpanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { isExpanded = false }
            ) {
                speeds.forEach { speed ->
                    DropdownMenuItem(
                        text = { Text(text = speed.toString()) },
                        onClick = {
                            onClick(speed)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MusicProgressBar(
    modifier: Modifier = Modifier,
    currentDurationMs: Long = 0L,
    totalDurationMs: Long = 0L,
    onValueChange: (Float) -> Unit = {}
) {
    Log.d("MusicProgressBar", "MusicProgressBar: $currentDurationMs")
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = currentDurationMs.toFormattedDuration(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // REMOVED .weight(1f) to let the text take its natural width
            textAlign = TextAlign.End
        )
        CircularProgressBar(
            modifier = Modifier
                .height(20.dp)
                // CHANGED to .weight(1f) to fill ALL remaining space
                .weight(1f),
            currentProgress = currentDurationMs.toFloat(),
            range = 0f..totalDurationMs.toFloat(),
            onValueChange = onValueChange
        )
        Text(
            text = totalDurationMs.toFormattedDuration(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // REMOVED .weight(1f) here as well
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun PlayerScreenTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onQueueListClick: () -> Unit,
) {
    val iconSize = 60.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "back",
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(
            onClick = onQueueListClick
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_queue_music_24),
                contentDescription = "menu",
                modifier = Modifier.size(iconSize)
            )

        }
    }
}
