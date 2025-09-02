package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE // If a song is deleted, its history is also cleared
        )
    ],
    indices = [Index("song_id"), Index("played_at"), Index("duration_played")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "song_id")
    val songId: Long,

    @ColumnInfo(name = "played_at")
    val playedAt: Long = System.currentTimeMillis(), // Timestamp when playback started

    @ColumnInfo(name = "duration_played")
    val durationPlayed: Long, // How many milliseconds of the song were actually played

    @ColumnInfo(name = "was_favorite_at_time_of_play")
    val wasFavorite: Boolean // Captures if the song was a favorite when played
)


