package org.ghost.musify.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a complete record of a song's metadata, mirroring
 * the columns available in the Android MediaStore.
 */
@Entity(
    tableName = "songs",
    indices = [Index("album_id"), Index("artist"), Index("title")]
)
data class SongEntity(
    // --- Essential Metadata ---
    @PrimaryKey
    val id: Long, // From MediaStore.Audio.Media._ID

    @ColumnInfo(name = "title")
    val title: String, // From MediaStore.Audio.Media.TITLE

    @ColumnInfo(name = "artist")
    val artist: String, // From MediaStore.Audio.Media.ARTIST

    @ColumnInfo(name = "album")
    val album: String, // From MediaStore.Audio.Media.ALBUM

    @ColumnInfo(name = "album_artist")
    val albumArtist: String?, // From MediaStore.Audio.Media.ALBUM_ARTIST

    @ColumnInfo(name = "album_id")
    val albumId: Long, // From MediaStore.Audio.Media.ALBUM_ID

    @ColumnInfo(name = "duration")
    val duration: Int, // From MediaStore.Audio.Media.DURATION (in milliseconds)

    @ColumnInfo(name = "track_number")
    val trackNumber: Int, // From MediaStore.Audio.Media.TRACK

    @ColumnInfo(name = "year")
    val year: Int, // From MediaStore.Audio.Media.YEAR

    // --- Technical File Details ---
    @ColumnInfo(name = "file_path")
    val filePath: String, // From MediaStore.Audio.Media.DATA (deprecated but useful)

    @ColumnInfo(name = "date_added")
    val dateAdded: Long, // From MediaStore.Audio.Media.DATE_ADDED (Unix timestamp)

    @ColumnInfo(name = "date_modified")
    val dateModified: Long, // From MediaStore.Audio.Media.DATE_MODIFIED (Unix timestamp)

    @ColumnInfo(name = "size")
    val size: Long, // From MediaStore.Audio.Media.SIZE (in bytes)

    @ColumnInfo(name = "mime_type")
    val mimeType: String?, // From MediaStore.Audio.Media.MIME_TYPE

    @ColumnInfo(name = "bitrate")
    val bitrate: Int, // From MediaStore.Audio.Media.BITRATE (in bits per second)

    // --- Optional & Categorical Metadata ---
    @ColumnInfo(name = "composer")
    val composer: String?, // From MediaStore.Audio.Media.COMPOSER

    @ColumnInfo(name = "is_music")
    val isMusic: Boolean, // From MediaStore.Audio.Media.IS_MUSIC (to filter out ringtones etc.)

    @ColumnInfo(name = "is_podcast")
    val isPodcast: Boolean, // From MediaStore.Audio.Media.IS_PODCAST

    @ColumnInfo(name = "is_ringtone")
    val isRingtone: Boolean, // From MediaStore.Audio.Media.IS_RINGTONE

    @ColumnInfo(name = "is_alarm")
    val isAlarm: Boolean, // From MediaStore.Audio.Media.IS_ALARM

    @ColumnInfo(name = "is_notification")
    val isNotification: Boolean // From MediaStore.Audio.Media.IS_NOTIFICATION
)



