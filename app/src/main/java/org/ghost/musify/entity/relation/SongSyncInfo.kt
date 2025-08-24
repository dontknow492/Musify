package org.ghost.musify.entity.relation

import androidx.room.ColumnInfo


data class SongSyncInfo(
    val id: Long,
    @ColumnInfo(name = "date_modified")
    val dateModified: Long
)
