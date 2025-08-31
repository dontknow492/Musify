package org.ghost.musify.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.SongEntity

/**
 * This is the final, top-level data class for a history entry.
 * It contains the specific playback event and the fully-detailed song object.
 */
data class HistoryWithSongDetails(
    @Embedded
    val history: HistoryEntity,

    @Relation(
        // The relation must target the base entity of the nested object (SongEntity).
        entity = SongEntity::class,
        // The parent column is in the @Embedded HistoryEntity.
        parentColumn = "song_id",
        // The entity column is in the target SongEntity.
        entityColumn = "id"
    )
    val songWithAlbumAndArtist: SongWithAlbumAndArtist
)
