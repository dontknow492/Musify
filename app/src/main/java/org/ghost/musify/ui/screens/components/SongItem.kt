package org.ghost.musify.ui.screens.components

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistEntity
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.utils.cacheEmbeddedArt
import org.ghost.musify.utils.formatDuration
import org.ghost.musify.utils.getAlbumArtUri
import org.ghost.musify.utils.getSongUri


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeableSongItem(
    modifier: Modifier = Modifier,
    dismissState: SwipeToDismissBoxState,
    songWithAlbumAndArtist: SongWithAlbumAndArtist,
    isVisible: Boolean = true,
    coverArtUri: Uri?,
    onCardClick: (Long) -> Unit,
    onMenuCLick: (Long) -> Unit,
    isDraggable: Boolean = false,
    leftComposable: @Composable () -> Unit,
    rightComposable: @Composable () -> Unit,
) {

    AnimatedVisibility(
        isVisible,
        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut(),
        modifier = modifier
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> rightComposable()
                    SwipeToDismissBoxValue.StartToEnd -> leftComposable()
                    SwipeToDismissBoxValue.Settled -> {}
                }
            }
        ) {
            SongItem(
                songWithAlbumAndArtist = songWithAlbumAndArtist,
                coverArtUri = coverArtUri,
                isDraggable = isDraggable,
                onCardClick = onCardClick,
                onMenuCLick = onMenuCLick,
            )
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    songWithAlbumAndArtist: SongWithAlbumAndArtist,
    coverArtUri: Uri? = null,
    isDraggable: Boolean = false,
    onCardClick: (Long) -> Unit,
    onMenuCLick: (Long) -> Unit,
) {
    val startPadding = if (isDraggable) 4.dp else 12.dp
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
        modifier = Modifier
            .padding(
                start = startPadding,
                end = 4.dp,
                top = 12.dp,
                bottom = 12.dp
            )
            .clickable(onClick = { onCardClick(song.id) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDraggable) {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_drag_indicator_24),
                    contentDescription = "drag",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

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
            IconButton(
                onClick = {
                    onMenuCLick(song.id)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "More"
                )
            }
            Text(
                text = formatDuration(song.duration.toLong()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
private fun SongItemPreview() {

    val song = SongEntity(
        id = 36L,
        title = "Tu Hi Meri Shab Hai (From \"Gangster\")",
        artistId = 522289580882137544L,
        albumId = 522289580882137544L,
        duration = 180000,
        trackNumber = 1,
        year = 2023,
        filePath = "/path/to/sample/song.mp3",
        dateAdded = System.currentTimeMillis() / 1000,
        dateModified = System.currentTimeMillis() / 1000,
        size = 5000000L,
        mimeType = "audio/mpeg",
        bitrate = 128000,
        composer = "Sample Composer",
        isMusic = true,
        isPodcast = false,
        isRingtone = false,
        isAlarm = false,
        isNotification = false
    )

    val coverUri = getAlbumArtUri(song.albumId)
    var isVisible by remember { mutableStateOf(true) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> true
                SwipeToDismissBoxValue.EndToStart -> true
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )
    val songWithAlbumAndArtist = SongWithAlbumAndArtist(
        song = song,
        album = AlbumEntity(101L, "Album Name", "Arijit Singh"),
        artist = ArtistEntity(10L, "Arijit Singh")
    )
//    val leftComposable = Box(modifier = Modifier.size(100.dp).background(Color.Red))
//    val rightComposable = Box(modifier = Modifier.size(100.dp).background(Color.Green))
    SwipeableSongItem(
        dismissState = state,
        songWithAlbumAndArtist = songWithAlbumAndArtist,
        isVisible = isVisible,
        coverArtUri = coverUri,
        onCardClick = {},
        onMenuCLick = {},
        leftComposable = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
            )
        },
        rightComposable = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Green)
            )
        }
    )
}