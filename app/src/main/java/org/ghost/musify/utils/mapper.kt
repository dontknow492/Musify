package org.ghost.musify.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist

/**
 * Converts a list of your database entities into a list of MediaItems.
 *
 * @param songs The list of SongWithAlbumAndArtist objects from your repository.
 * @return A mutable list of MediaItems ready for ExoPlayer.
 */
fun mapSongsToMediaItems(songs: List<SongWithAlbumAndArtist>): MutableList<MediaItem> {
    return songs.map { songData ->
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(songData.song.title)
            .setArtist(songData.artist.name)
            .setAlbumTitle(songData.album.title)
            .build()

        MediaItem.Builder()
            .setMediaId(songData.song.id.toString())
            .setUri(getSongUri(songData.song.id))
            .setMediaMetadata(mediaMetadata)
            .build()

    }.toMutableList()
}