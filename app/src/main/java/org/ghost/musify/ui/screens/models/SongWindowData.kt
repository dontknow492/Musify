package org.ghost.musify.ui.screens.models

import androidx.paging.compose.LazyPagingItems
import coil3.request.ImageRequest
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist

data class SongWindowData(
    val songs: LazyPagingItems<SongWithAlbumAndArtist>,
    val title: String,
    val body: String,
    val headingTitle: String,
    val image: ImageRequest,
    val backgroundImage: ImageRequest? = null,
    val search: String = "",
    val count: Int,
    val type: String = "musics"
)