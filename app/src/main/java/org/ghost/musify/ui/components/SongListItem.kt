package org.ghost.musify.ui.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.utils.getSongUri

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SongsLazyColumn(
    modifier: Modifier = Modifier,
    songs: LazyPagingItems<SongWithAlbumAndArtist>,
    item: @Composable () -> Unit = {},
    onSongClick: (Long) -> Unit = {},
    onMenuClick: (Long) -> Unit = {},
//    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if(songs.itemCount == 0 && songs.loadState.refresh !is LoadState.Loading){
            item{
                EmptySongWindow(
                    title = "No Songs Found",
                    subtitle = "It looks like you haven't added any songs yet. Start by adding some music to your library.",
                    actionText = "Add Songs",
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.rounded_music_off_24),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

            }
        }
        else{
            item {
                item()
            }
            items(songs.itemCount) { index ->
                val song = songs[index]
                song?.let { song ->
                    SongItem(
                        modifier = Modifier,
                        songWithAlbumAndArtist = song,
                        coverArtUri = getSongUri(song.song.id),
                        onCardClick = {
                            onSongClick(it)
                            Log.d("SongsLazyColumn", "onCardClick: $it")
                        },
                        onMenuCLick = {
                            onMenuClick(it)
                            Log.d("SongsLazyColumn", "onMenuCLick: $it")
                        }
                    )
                }
            }
        }
    }

}

@Preview()
@Composable
fun EmptySongWindowPreview() {
    EmptySongWindow(
        title = "No Songs Found",
        subtitle = "It looks like you haven't added any songs yet. Start by adding some music to your library.",
        actionText = "Add Songs"
    ) {
        // Handle action click
    }
}


/**
 * A visually appealing, reusable composable for displaying an empty state.
 * Perfect for screens like playlists, song lists, search results, etc.,
 * when there is no content to show.
 *
 * @param modifier The modifier to be applied to the container.
 * @param icon The icon to be displayed at the center. Defaults to a 'Music Off' icon.
 * @param title The main headline text for the empty state.
 * @param subtitle A more descriptive subtext displayed below the title.
 * @param actionText Optional text for a call-to-action button. If null, the button is not shown.
 * @param onActionClick The lambda to be executed when the action button is clicked.
 */
@Composable
fun EmptySongWindow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
    icon: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding to the outer box
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp) // Inner padding for text content
        ) {
            // 1. Icon
            icon()

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // A slightly muted color
                textAlign = TextAlign.Center
            )

            // 4. Optional Action Button
            actionText?.let {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onActionClick) {
                    Text(text = it)
                }
            }
        }
    }
}


