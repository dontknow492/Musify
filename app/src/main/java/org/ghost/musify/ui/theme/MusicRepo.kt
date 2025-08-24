package org.ghost.musify.ui.theme

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import org.ghost.musify.Song

class MusicRepository(private val context: Context) {

    // This is a suspend function, meaning it must be called from a coroutine.
    // We use withContext(Dispatchers.IO) to run this blocking query on a background thread.
    fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()

        // The URI for the audio files table in the MediaStore
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // The specific pieces of information we want to retrieve for each song.
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // We only want files that are marked as music.
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        // Sorting the results by title in ascending order.
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        // Perform the query. The 'use' block ensures the Cursor is automatically closed.
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            // Get the column indices for faster access inside the loop.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            // Loop through each row in the query result.
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getInt(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                // Create a URI for the song file itself.
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Create a URI for the album art.
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumId
                )

                // Add the created Song object to our list.
                songList.add(Song(id, title, artist, duration, contentUri, albumArtUri))
            }
        }

        return songList
    }
}