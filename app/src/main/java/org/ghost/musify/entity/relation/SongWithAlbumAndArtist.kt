package org.ghost.musify.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistEntity
import org.ghost.musify.entity.SongEntity

data class SongWithAlbumAndArtist(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "album_id",
        entityColumn = "id",
    )
    val album: AlbumEntity,
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: ArtistEntity
)
