package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists_image", indices = [Index(value = ["name"], unique = true)])
data class ArtistImageEntity(
    @PrimaryKey
    val name: String,

    @ColumnInfo(name = "image_uri_id")
    val imageUriId: Long? = null,

    // Corrected the column name here
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
)
