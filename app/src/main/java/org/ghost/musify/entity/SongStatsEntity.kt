package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "song_stats",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,    // Links to the main SongEntity
            parentColumns = ["id"],        // The primary key of the SongEntity
            childColumns = ["song_id"],    // The foreign key in this table
            onDelete = ForeignKey.CASCADE  // If a song is deleted, its stats are also deleted
        )
    ],
    indices = [Index("play_count")]
)
data class SongStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long, // This is both the Primary Key and a Foreign Key to SongEntity.id

    @ColumnInfo(name = "play_count")
    var playCount: Int = 0, // The total number of times the song has been played

    @ColumnInfo(name = "last_played_timestamp")
    var lastPlayedTimestamp: Long = 0 // The Unix timestamp of when the song was last played
)
