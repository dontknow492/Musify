package org.ghost.musify.ui.screens.common

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.screens.items.SongItem
import org.ghost.musify.utils.getSongUri

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    songs: LazyPagingItems<SongWithAlbumAndArtist>,
    item: @Composable () -> Unit = {},
    onSongClick: (Long) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        item {
            item()
        }
        items(songs.itemCount) { index ->
            val song = songs[index]
            song?.let { song ->
                SongItem(
                    songWithAlbumAndArtist = song,
                    coverArtUri = getSongUri(song.song.id),
                    onCardClick = {
                        onSongClick(it)
                        Log.d("HomeScreen", "onCardClick: $it")
                    },
                    onMenuCLick = {
                        Log.d("HomeScreen", "onMenuCLick: $it")
                    }
                )
            }

        }
    }
}


