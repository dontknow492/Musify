package org.ghost.musify

import android.net.Uri

data class Song(
    val id: Long,          // A unique ID for the song
    val title: String,
    val artist: String,
    val duration: Int,     // Duration in milliseconds
    val contentUri: Uri,   // The URI to play the file
    val albumArtUri: Uri   // The URI for the album cover
)
