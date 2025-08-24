package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_songs",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.Companion.CASCADE // If a song is deleted, remove it from favorites
        )
    ],
    indices = [Index("song_id"), Index("added_at")] // Index for quick look-up by song ID and added timestamp
)
data class FavoriteSongEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long, // The ID of the favorited song

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis() // Timestamp when it was favorited
)



