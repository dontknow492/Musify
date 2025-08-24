package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artist_stats", indices = [Index("artist_name")])
data class ArtistStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "artist_name")
    val artistName: String, // The name of the artist

    @ColumnInfo(name = "total_play_count")
    var totalPlayCount: Int = 0,

    @ColumnInfo(name = "total_listen_duration_ms")
    var totalListenDurationMs: Long = 0, // Total time user has spent listening to this artist

    @ColumnInfo(name = "last_played_timestamp")
    var lastPlayedTimestamp: Long = 0
)


