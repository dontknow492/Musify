package org.ghost.musify.data

import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistEntity
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.SongEntity

data class MediaFetchResult(
    val songs: List<SongEntity>,
    val albums: List<AlbumEntity>,
    val artists: List<ArtistEntity>,
    val artistImages: List<ArtistImageEntity>
)