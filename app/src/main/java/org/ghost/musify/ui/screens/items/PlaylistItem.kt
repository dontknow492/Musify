package org.ghost.musify.ui.screens.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.musify.entity.PlaylistEntity


@Preview
@Composable
fun PlaylistItemPreview() {
    val playlist = PlaylistEntity(
        name = "Temp this is big one",
        description = "A collection of my favorite tunes."
    )
    PlaylistItem(
        playlist = playlist
    )
}