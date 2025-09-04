package org.ghost.musify.ui.screens.tabWindow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.ghost.musify.ui.components.CoverChangeableItem
import org.ghost.musify.viewModels.home.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumViewModel,
    onAlbumClick: (Long) -> Unit = {},
) {
    val albumsWithCover = viewModel.albums.collectAsLazyPagingItems()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            albumsWithCover.itemCount,
            key = { index -> albumsWithCover[index]?.album?.id ?: index }
        ) { index ->
            val albumWithCover = albumsWithCover[index]
            if (albumWithCover != null) {
                CoverChangeableItem(
                    modifier = modifier
                        .clickable {
                            onAlbumClick(albumWithCover.album.id)
                        }
                        .height(200.dp),
                    coverImage = albumWithCover.cover,
                    title = albumWithCover.album.title,
                )
            }

        }

    }
}

