package org.ghost.musify.ui.screens.tabWindow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.ghost.musify.ui.screens.components.AlbumItem
import org.ghost.musify.viewModels.home.AlbumViewModel

@Composable
fun AlbumScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumViewModel,
    onAlbumClick: (Long) -> Unit = {}
) {
    val albums = viewModel.albums.collectAsLazyPagingItems()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            albums.itemCount,
            key = { index -> albums[index]?.id ?: index }
        ) { index ->
            val album = albums[index]
            if (album != null) {
                AlbumItem(
                    modifier = Modifier.height(200.dp),
                    album = album,
                    onAlbumClick = onAlbumClick
                )
            }

        }

    }

}

