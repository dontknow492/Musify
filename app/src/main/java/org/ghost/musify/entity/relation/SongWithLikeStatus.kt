package org.ghost.musify.entity.relation

import androidx.room.Embedded
import org.ghost.musify.entity.FavoriteSongEntity

data class SongDetailsWithLikeStatus(
    @Embedded
    val songDetail: SongWithAlbumAndArtist,

    @Embedded
    val liked: FavoriteSongEntity?
)
