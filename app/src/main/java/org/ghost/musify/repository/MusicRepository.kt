package org.ghost.musify.repository

import android.content.Context
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.ghost.musify.dao.FavoriteDao
import org.ghost.musify.dao.HistoryAndStatsDao
import org.ghost.musify.dao.PlaylistDao
import org.ghost.musify.dao.SongDao
import org.ghost.musify.entity.FavoriteSongEntity
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.PlaylistSongCrossRef
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import javax.inject.Inject

class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyAndStatsDao: HistoryAndStatsDao,
    private val favoriteDao: FavoriteDao,
) {
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false
    )

    // --- Data Synchronization ---

    /**
     * Scans the MediaStore and synchronizes the local database with the findings.
     * This is the core logic for keeping the app's library up-to-date.
     */
    suspend fun syncMediaStore() = withContext(Dispatchers.IO) {
        // Step 1: Fetch lightweight sync data (ID and timestamp) from both sources.
        val mediaStoreSongsMap = scanMediaStoreForSyncInfo()
        val databaseSongsMap = songDao.getSongSyncInfo().associateBy({ it.id }, { it.dateModified })

        // Step 2: Identify songs to be deleted from the local database.
        // These are songs that exist in our DB but not in the MediaStore anymore.
        val deletedSongIds = databaseSongsMap.keys - mediaStoreSongsMap.keys
        if (deletedSongIds.isNotEmpty()) {
            songDao.deleteSongsByIds(deletedSongIds.toList())
        }

        // Step 3: Identify new or updated songs.
        // A song is new if its ID is not in our DB.
        // A song is updated if its modification timestamp in MediaStore is newer.
        val idsToUpdateOrInsert = mediaStoreSongsMap.filter { (id, timestamp) ->
            id !in databaseSongsMap || timestamp > databaseSongsMap[id]!!
        }.keys

        // Step 4: Fetch full data for only the new/updated songs and save them.
        if (idsToUpdateOrInsert.isNotEmpty()) {
            val songsToInsert = fetchFullSongDetails(idsToUpdateOrInsert)
            songDao.insertSongs(songsToInsert) // Uses OnConflictStrategy.REPLACE
        }
    }

    /**
     * Scans the MediaStore for only the ID and DATE_MODIFIED columns for efficiency.
     * @return A Map of song ID to its modification timestamp.
     */
    private fun scanMediaStoreForSyncInfo(): Map<Long, Long> {
        val songsMap = mutableMapOf<Long, Long>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                songsMap[id] = dateModified
            }
        }
        return songsMap
    }

    /**
     * Fetches the full SongEntity details from MediaStore for a specific set of song IDs.
     * This is called only for songs that are new or have been updated.
     */
    private fun fetchFullSongDetails(songIds: Set<Long>): List<SongEntity> {
        // This function will be very similar to your original `scanMediaStoreForSongs`,
        // but with an added WHERE clause to select by ID.
        if (songIds.isEmpty()) return emptyList()

        val songList = mutableListOf<SongEntity>()
        val selection = "${MediaStore.Audio.Media._ID} IN (${songIds.joinToString(",")})"

        // The projection here should include ALL columns needed for your SongEntity
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_PODCAST,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION
        )

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val song = SongEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                    artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                    albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    trackNumber = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)),
                    dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)),
                    albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)),
                    composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)),
                    bitrate = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)),
                    isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) == 1,
                    isPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) == 1,
                    isRingtone = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) == 1,
                    isAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) == 1,
                    isNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) == 1
                )
                songList.add(song)
            }
        }
        return songList
    }

    /**
     * Queries the Android MediaStore for all audio files and maps them to SongEntity objects.
     */
    private fun scanMediaStoreForSongs(): List<SongEntity> {
        val songList = mutableListOf<SongEntity>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_PODCAST,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val song = SongEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                    artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                    albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    trackNumber = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)),
                    dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)),
                    albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)),
                    composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)),
                    bitrate = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)),
                    isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) == 1,
                    isPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) == 1,
                    isRingtone = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) == 1,
                    isAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) == 1,
                    isNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) == 1
                )
                songList.add(song)
            }
        }
        return songList
    }

    // --- Song Access ---

    fun getAllSongs(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<SongEntity>> {
        return Pager(config = pagingConfig) {

            songDao.getAllSongs(
                query = query,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
        }.flow
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    // --- Playlist Access & Management ---

    fun getAllPlaylists(): Flow<PagingData<PlaylistEntity>> {
        return Pager(config = pagingConfig) {
            playlistDao.getAllPlaylists()
        }.flow
    }

    suspend fun createPlaylist(name: String) {
        playlistDao.createPlaylist(PlaylistEntity(name = name))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    // --- Favorite Access & Management ---

    fun getFavoriteSongs(
        query: String = "",
        sortBy: SortBy = SortBy.ADDED_AT,
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<SongEntity>> {
        return Pager(config = pagingConfig) {
            favoriteDao.getFavoriteSongs(
                query = query,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
        }.flow
    }

    suspend fun addToFavorites(songId: Long) {
        favoriteDao.addToFavorites(FavoriteSongEntity(songId = songId))
    }

    suspend fun removeFromFavorites(songId: Long) {
        favoriteDao.removeFromFavorites(songId)
    }

    suspend fun isFavorite(songId: Long): Boolean {
        return favoriteDao.isFavorite(songId) != null
    }

    // --- History & Stats Access ---

    fun getRecentlyPlayed(
        query: String = "",
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<SongEntity>> {
        return Pager(config = pagingConfig) {
            historyAndStatsDao.getRecentlyPlayed(
                query = query,
                sortOrder = sortOrder
            )
        }.flow
    }

    fun getTopPlayedSongs(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<SongEntity>> {
        return Pager(config = pagingConfig) {
            historyAndStatsDao.getTopPlayedSongs(
                query = query,
                sortOrder = sortOrder
            )
        }.flow
    }

    suspend fun addToHistory(history: HistoryEntity) {
        historyAndStatsDao.addToHistory(history)
    }
}