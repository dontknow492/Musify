package org.ghost.musify.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.screens.components.SongItem
import org.ghost.musify.ui.screens.dialog.MusicInfoDialog
import org.ghost.musify.utils.DynamicThemeFromImage
import org.ghost.musify.utils.toFormattedDuration
import org.ghost.musify.viewModels.PlayerStatus
import org.ghost.musify.viewModels.PlayerViewModel


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayerWindow(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddToPlaylistClick: (Long) -> Unit,
) {
    val playbackQueue by viewModel.playbackQueue.collectAsState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val onQueueListClick: () -> Unit = {
        isBottomSheetVisible = true
    }

    val uiState by viewModel.uiState.collectAsState()



    Log.d("PlayerWindow", "PlayerWindow: ${playbackQueue.size}")

    DynamicThemeFromImage(
        imageUrl = uiState.coverImage
    ) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
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
                )

            }
            //
        }
    }

    if (isBottomSheetVisible) {
        Log.d("PlayerWindow", "PlayerWindow: BottomSheet is visible")
        PlayerBottomSheet(modifier = modifier, songs = playbackQueue) {
            isBottomSheetVisible = false
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayerBottomSheet(
    modifier: Modifier = Modifier,
    songs: List<SongWithAlbumAndArtist>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        // 1. Add state to hold and remember the search query
        var searchQuery by rememberSaveable { mutableStateOf("") }

        // 2. Filter the list based on the search query.
        // This remembers the result unless the query or the original songs list changes.
        val filteredSongs = remember(searchQuery, songs) {
            if (searchQuery.isBlank()) {
                songs
            } else {
                songs.filter { songWithAlbumAndArtist ->
                    // You can search by title, artist, or album
                    songWithAlbumAndArtist.song.title.contains(searchQuery, ignoreCase = true) ||
                            songWithAlbumAndArtist.artist.name.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                }
            }
        }

        Column(modifier = modifier) {
            // 3. Add the TextField for the search bar UI
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search in queue...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Icon")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp), // Pill shape
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, // No underline
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            // 4. Use the filtered list in the LazyColumn
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(filteredSongs, key = { item -> item.song.id }) { song ->
                    SongItem(
                        songWithAlbumAndArtist = song,
                        coverArtUri = null,
                        isDraggable = false,
                        onCardClick = {},
                        onMenuCLick = {}
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerScreenItem(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    onAddToPlaylistClick: (Long) -> Unit,
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
                        .data(null)
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
                        .data(null)
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
    val songWithAlbumAndArtist = uiState.currentSong
    val songTitle = songWithAlbumAndArtist?.song?.title ?: "Unknown"
    val songArtist = songWithAlbumAndArtist?.artist?.name ?: "Unknown"
    val songAlbum = songWithAlbumAndArtist?.album?.title ?: "Unknown"
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
                onClick = { viewModel.toggleFavorite() }
            ) {
                when (uiState.isFavorite) {
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
                onClick = { onAddToPlaylistClick(uiState.currentSong?.song?.id ?: 0L) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "add to playlist"
                )
            }

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
            totalDurationMs = uiState.currentSong?.song?.duration?.toLong() ?: 0L,
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

            Box {
                if (uiState.status == PlayerStatus.BUFFERING) {
                    CircularProgressIndicator()
                } else {
                    IconButton(
                        onClick = { viewModel.onPlayPauseClicked() },
                    ) {
                        Icon(
                            painter = if (uiState.isPlaying)
                                painterResource(R.drawable.baseline_pause_24)
                            else
                                painterResource(R.drawable.baseline_play_arrow_24),
                            contentDescription = "play/pause",
                            modifier = Modifier.size(iconSizeLarge)
                        )
                    }
                }

            }
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
            songWithAlbumAndArtist = uiState.currentSong!!
        ) {
            isInfoDialogVisible = false
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
fun CircularProgressBar(
    modifier: Modifier,
    currentProgress: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = LocalContentColor.current,


    ) {
    val currentProgress = if (!range.contains(currentProgress)) {
        range.start
    } else {
        currentProgress
    }

    Canvas(
        modifier = modifier
            .pointerInput(range) { // The key for handling user input
                detectTapGestures { offset ->
                    // Logic to handle taps
                    val barWidth = size.width - size.height // The tappable width of the bar
                    val radius = size.height / 2f

                    // Calculate the progress based on the tap position
                    val position = (offset.x - radius).coerceIn(0f, barWidth.toFloat())
                    val progress =
                        (position / barWidth) * (range.endInclusive - range.start) + range.start
                    onValueChange(progress)
                }
            }
            .pointerInput(range) {
                detectDragGestures { change, _ ->
                    // Logic to handle drags
                    val barWidth = size.width - size.height // The draggable width of the bar
                    val radius = size.height / 2f

                    // Calculate the progress based on the drag position
                    val position = (change.position.x - radius).coerceIn(0f, barWidth.toFloat())
                    val progress =
                        (position / barWidth) * (range.endInclusive - range.start) + range.start
                    onValueChange(progress)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barHeight = height.div(3)
        val radius = height.div(2)

        val fillSize = (currentProgress / range.endInclusive) * width.minus(radius * 2)



        drawRoundRect(
            color = color,
            topLeft = Offset(radius, height.minus(barHeight).div(2)),
            size = Size(width - radius * 2, barHeight),
            cornerRadius = CornerRadius(height, height)
        )


        drawRoundRect(
            color = indicatorColor,
            topLeft = Offset(radius, barHeight),
            size = Size(fillSize, barHeight),
            cornerRadius = CornerRadius(height, height)
        )


        drawArc(
            color = indicatorColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(fillSize, 0f),
            size = Size(radius * 2, radius * 2)
        )
        drawArc(
            color = contentColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                fillSize + radius.div(2),
                radius.div(2)
            ),
            size = Size(radius, radius)
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


@Composable
fun VolumeControlButton(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {}
) {
    // 1. State to control the visibility of the popup.
    var showPopup by remember { mutableStateOf(false) }

    // State for the slider's value.

    // A Box is used to anchor the Popup to the Button.
    Box {
        // The button that triggers the popup.
        IconButton(onClick = { showPopup = true }) {
            when (volumeLevel) {
                0f -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_off_24),
                        contentDescription = "volume off"
                    )
                }

                in 0f..0.5f -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_down_24),
                        contentDescription = "volume medium"
                    )
                }

                else -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_up_24),
                        contentDescription = "Set Volume"
                    )
                }
            }
        }

        // The Popup is displayed conditionally based on the state.
        if (showPopup) {
            // 2. The Popup composable.
            Popup(
                // Position the popup right above the button.
                alignment = Alignment.TopCenter,
                // The lambda to execute when the user clicks outside the popup.
                onDismissRequest = { showPopup = false },
                // Optional: properties to control focus, etc.
                properties = PopupProperties(focusable = true)
            ) {
                // 3. The content of the popup.
                Card(
                    modifier = Modifier.padding(bottom = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Master Volume", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = volumeLevel,
                            onValueChange = onVolumeChange
                        )
                        Text(text = "${(volumeLevel * 100).toInt()}%")
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDialog() {

}
