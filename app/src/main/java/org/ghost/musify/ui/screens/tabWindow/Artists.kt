package org.ghost.musify.ui.screens.tabWindow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.ghost.musify.ui.screens.components.ArtistItem
import org.ghost.musify.viewModels.home.ArtistViewModel

@Composable
fun ArtistScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtistViewModel,
    onArtistClick: (String) -> Unit = {}
) {
    val artists = viewModel.artists.collectAsLazyPagingItems()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            artists.itemCount,
            key = { index -> artists[index]?.name ?: index }
        ) { index ->
            val artist = artists[index]
            if (artist != null) {
                ArtistItem(artist = artist) { item ->
                    onArtistClick(item.name)
                }
                viewModel.updateArtistsImage(
                    name = artist.name,
                    imageUrl = null,
                    imageUriId = artist.imageUriId
                )
            }

        }

    }

}

