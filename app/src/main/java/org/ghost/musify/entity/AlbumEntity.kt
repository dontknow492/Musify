package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "title", index = true)
    val title: String,

    // This links the album to its primary artist
    @ColumnInfo(name = "artist", index = true)
    val artist: String,

    @ColumnInfo(name = "album_image_uri_id")
    val albumImageUriId: Long? = null,

    @ColumnInfo(name = "album_image_url")
    val albumImageUrl: String? = null
)
