package org.ghost.musify.ui.screens.player

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.ui.components.SwipeableSongItem
import org.ghost.musify.utils.toFormattedDuration
import org.ghost.musify.viewModels.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayerBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel,
    onDismiss: () -> Unit,
    onSongRemove: (Long) -> Unit = {},
    onSongClick: (Long) -> Unit = {},
    onSongMenuClick: (Long) -> Unit = {},
) {
    val queueState by viewModel.queueState.collectAsStateWithLifecycle()
    val currentSongId = queueState.currentSong?.songDetail?.song?.id
    val songs = queueState.queue

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        // 1. Add state to hold and remember the search query
        var searchQuery by rememberSaveable { mutableStateOf("") }

        var songIndex by remember { mutableIntStateOf(0) }
        var totalDuration by remember { mutableLongStateOf(0L) }

        LaunchedEffect(songs) {
            totalDuration = songs.sumOf { it.songDetail.song.duration }.toLong()
        }

        // 2. Filter the list based on the search query.
        // This remembers the result unless the query or the original songs list changes.
        val filteredSongs = remember(searchQuery, songs) {
            if (searchQuery.isBlank()) {
                songs
            } else {
                songs.filter { songDetailsWithLikeStatus ->
                    // You can search by title, artist, or album
                    songDetailsWithLikeStatus.songDetail.song.title.contains(searchQuery, ignoreCase = true) ||
                            songDetailsWithLikeStatus.songDetail.artist.name.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                }
            }
        }

        Column(modifier = modifier) {
            // 3. Add the TextField for the search bar UI

            PlayerBottomSheetAction(
                modifier = Modifier,
                size = songs.size,
                current = songIndex + 1,
                duration = totalDuration
            )

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
                itemsIndexed(filteredSongs, key = { _, item -> item.songDetail.song.id }) { index, songDetailWithLikeStatus ->
                    val isPlaying = if(songDetailWithLikeStatus.songDetail.song.id == currentSongId){
                        songIndex = index
                        true
                    }
                    else
                        false
                    QueueItem(
                        modifier = Modifier.animateItem(),
                        song = songDetailWithLikeStatus,
                        isPlaying = isPlaying,
                        onSongRemove = onSongRemove,
                        onSongClick = onSongClick,
                        onSongMenuClick = onSongMenuClick,
                    )
                }
            }
        }
    }
}


@Composable
private fun PlayerBottomSheetAction(
    modifier: Modifier = Modifier,
    size: Int,
    current: Int,
    duration: Long,
){
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "lock"
            )
        }
        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_play_arrow_24),
                contentDescription = "play"
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text("$current/$size", style = MaterialTheme.typography.bodySmall)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(R.drawable.rounded_timer_24),
                    contentDescription = "time",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(duration.toFormattedDuration(), style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_save_24),
                contentDescription = "Save"
            )
        }
        IconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More"
            )
        }
    }
}


@Composable
private fun QueueItem(
    modifier: Modifier = Modifier,
    song: SongDetailsWithLikeStatus,
    isPlaying: Boolean,
    onSongRemove: (Long) -> Unit = {},
    onSongClick: (Long) -> Unit = {},
    onSongMenuClick: (Long) -> Unit
) {
    Log.d("QueueItem", "isPlaying: $isPlaying")
    val state = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSongRemove(song.songDetail.song.id)
                    true
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onSongRemove(song.songDetail.song.id)
                    true
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    val modifier = if (isPlaying){
//        songIndex = index
        modifier
            .border(
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            )
            .padding(2.dp)
    }
    else
        modifier

    val cardElevation = if (isPlaying)
        CardDefaults.cardElevation(defaultElevation = 12.dp)
    else
        CardDefaults.cardElevation()


    SwipeableSongItem(
        dismissState = state,
        modifier = modifier,
        songWithAlbumAndArtist = song.songDetail,
        coverArtUri = null,
        isDraggable = false,
        onCardClick = onSongClick,
        onMenuCLick = onSongMenuClick,
        leftComposable = {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterStart),
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "more"
                )
            }
        },
        rightComposable = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    contentDescription = "more"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        elevation = cardElevation
    )

}