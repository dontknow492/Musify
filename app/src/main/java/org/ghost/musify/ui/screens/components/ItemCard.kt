package org.ghost.musify.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.utils.getAlbumArtUri

@Composable
fun AlbumItem(
    modifier: Modifier = Modifier,
    album: AlbumEntity,
    coverImage: Any? = null,
    onAlbumClick: (Long) -> Unit = {}
) {
    val image = ImageRequest.Builder(LocalContext.current)
        .data(
            album.albumImageUriId ?: album.albumImageUrl ?: coverImage ?: getAlbumArtUri(album.id)
        )
        .crossfade(true)
        .placeholder(R.drawable.music_album_icon_2)
        .error(R.drawable.music_album_icon_2)
        .build()

    CoverChangeableItem(
        modifier = modifier.clickable(
            onClick = { onAlbumClick(album.id) }
        ),
        coverImage = image,
        title = album.title,
    )
}


@Composable
fun ArtistItem(
    modifier: Modifier = Modifier,
    artist: ArtistImageEntity,
    onClick: (ArtistImageEntity) -> Unit = {}
) {
    val image = ImageRequest.Builder(LocalContext.current)
        .data(artist.imageUriId ?: artist.imageUrl)
        .crossfade(true)
        .placeholder(R.drawable.artist_placeholder)
        .error(R.drawable.artist_placeholder)
        .build()



    CoverChangeableItem(
        modifier = modifier.clickable(
            onClick = { onClick(artist) }
        ),
        coverImage = image,
        title = artist.name,
    )
}

@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    playlist: PlaylistEntity,
    coverImage: Any? = null,
    onPlaylistClick: (Long) -> Unit = {}
) {
    val image = ImageRequest.Builder(LocalContext.current)
        .data(playlist.playlistImageUriId ?: playlist.playlistImageUrl ?: coverImage)
        .crossfade(true)
        .placeholder(R.drawable.playlist_placeholder)
        .error(R.drawable.playlist_placeholder)
        .build()

    Box {
        CoverChangeableItem(
            modifier = modifier.clickable(
                onClick = { onPlaylistClick(playlist.id) }
            ),
            coverImage = image,
            title = playlist.name,
        )
    }
}