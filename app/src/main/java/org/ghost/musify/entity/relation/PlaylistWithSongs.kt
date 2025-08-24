package org.ghost.musify.entity.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.PlaylistSongCrossRef
import org.ghost.musify.entity.SongEntity

/**
 * Data class to hold the result of the getPlaylistWithSongs query.
 */
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlist_id",
            entityColumn = "song_id"
        )
    )
    val songs: List<SongEntity>
)