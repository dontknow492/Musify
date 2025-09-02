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
        mapSongToMediaItem(songData)
    }.toMutableList()
}


fun mapSongToMediaItem(song: SongWithAlbumAndArtist): MediaItem {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle(song.song.title)
        .setArtist(song.artist.name)
        .setAlbumTitle(song.album.title)
        .build()

    return MediaItem.Builder()
        .setMediaId(song.song.id.toString())
        .setUri(getSongUri(song.song.id))
        .setMediaMetadata(mediaMetadata)
        .build()
}