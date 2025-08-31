package org.ghost.musify.ui.screens

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R
import org.ghost.musify.entity.relation.HistoryWithSongDetails
import org.ghost.musify.ui.screens.components.SongItem
import org.ghost.musify.utils.cacheEmbeddedArt
import org.ghost.musify.utils.getSongUri
import org.ghost.musify.utils.toFormattedDuration
import org.ghost.musify.viewModels.HistoryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
    onSongClick: (Long) -> Unit
) {
    val historyItems = viewModel.playbackHistory.collectAsLazyPagingItems()

    Box(modifier = modifier.fillMaxSize()) {
        if (historyItems.loadState.refresh is LoadState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (historyItems.itemCount == 0) {
            Text(
                text = "Your listening history is empty.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Loop through all the loaded items
                for (index in 0 until historyItems.itemCount) {
                    // Get the current and previous items to compare their dates
                    val currentItem = historyItems.peek(index) ?: continue
                    val previousItem = if (index > 0) historyItems.peek(index - 1) else null

                    val currentDate = currentItem.history.playedAt.toLocalDate()
                    val previousDate = previousItem?.history?.playedAt?.toLocalDate()

                    // Add a sticky header if it's the first item or the date has changed
                    if (index == 0 || currentDate != previousDate) {
                        stickyHeader {
                            DateHeader(date = currentDate)
                        }
                    }

                    // The actual history item
                    item(key = currentItem.history.id) {
                        HistoryItem(
                            data = currentItem,
                            onCardClick = { onSongClick(currentItem.songWithAlbumAndArtist.song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(data: HistoryWithSongDetails, onCardClick: () -> Unit) {
    TODO("Not yet implemented")
}

/**
 * A helper composable to format and display the date header.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val headerText = when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

    Text(
        text = headerText,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * An extension function to easily convert the Long timestamp to a LocalDate.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

@Composable
private fun HistroyItem(
    modifier: Modifier = Modifier,
    data: HistoryWithSongDetails,
    coverArtUri: Uri? = null,
    onCardClick: (Long) -> Unit,
) {
    val songWithAlbumAndArtist = data.songWithAlbumAndArtist
    val song = songWithAlbumAndArtist.song
    val artist = songWithAlbumAndArtist.artist
    val album = songWithAlbumAndArtist.album


    val artistName = if (artist.name.isNotEmpty() && artist.name != "<unknown>") {
        artist.name
    } else {
        "Unknown artist"
    }

    val albumTitle = if (album.title.isNotEmpty() && album.title != "<unknown>") {
        album.title
    } else {
        "Unknown album"
    }



    Row(
        modifier = modifier
            .clickable(onClick = { onCardClick(song.id) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    cacheEmbeddedArt(LocalContext.current, getSongUri(song.id))
                )
                .crossfade(true)
                .placeholder(R.drawable.empty_album)
                .error(R.drawable.ic_music_black)
                .build(),
            contentDescription = song.title,
            modifier = Modifier
                .size(74.dp)
                .clip(
                    shape = MaterialTheme.shapes.medium
                )
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,

            )
        Spacer(
            modifier = Modifier.width(12.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = artistName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = albumTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )


        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.history.durationPlayed.toFormattedDuration(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}