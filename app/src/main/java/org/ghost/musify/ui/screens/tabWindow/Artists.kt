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
import org.ghost.musify.viewModels.home.ArtistViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtistViewModel,
    onArtistClick: (String) -> Unit = {},
) {
    val artistsWithCover = viewModel.artists.collectAsLazyPagingItems()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            artistsWithCover.itemCount,
            key = { index -> artistsWithCover[index]?.artist?.name ?: index }
        ) { index ->
            val artistWithCover = artistsWithCover[index]
            if (artistWithCover != null) {
                val artist = artistWithCover.artist
                CoverChangeableItem(
                    modifier = modifier
                        .clickable(
                            onClick = { onArtistClick(artist.name) }
                        )
                        .height(200.dp),
                    coverImage = artistWithCover.cover,
                    title = artist.name,
                )

            }

        }

    }

}

