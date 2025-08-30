package org.ghost.musify.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ghost.musify.dao.ArtistImageDao
import org.ghost.musify.dao.FavoriteDao
import org.ghost.musify.dao.HistoryAndStatsDao
import org.ghost.musify.dao.PlaylistDao
import org.ghost.musify.dao.SongDao
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistEntity
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.entity.FavoriteSongEntity
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.PlaylistSongCrossRef
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.ui.screens.models.SongFilter
import org.ghost.musify.ui.screens.models.SongsCategory
import javax.inject.Inject

class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyAndStatsDao: HistoryAndStatsDao,
    private val favoriteDao: FavoriteDao,
    private val artistImageDao: ArtistImageDao,
) {
    private val pagingConfig = PagingConfig(
        pageSize = 120,
        enablePlaceholders = true
    )
    init{
        Log.d("MusicRepository", "MusicRepository initialized")

    }

    // --- Data Synchronization ---

    /**
     * Scans the MediaStore and synchronizes the local database with the findings.
     * This is the core logic for keeping the app's library up-to-date.
     */
    suspend fun syncMediaStore() = withContext(Dispatchers.IO) {
        // Step 1: Fetch lightweight sync data (ID and timestamp) from both sources.
        Log.d("MusicRepository", "syncMediaStore started")
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

            Log.d("MusicRepository", "ArtistImage to insert: ${songsToInsert["artistImages"]}")

            songDao.insertSongsWithAlbumAndArtist(
                songsToInsert["songs"] as List<SongEntity>,
                songsToInsert["albums"] as List<AlbumEntity>,
                songsToInsert["artists"] as List<ArtistEntity>
            ) // Uses OnConflictStrategy.REPLACE

            updateArtistsImage(songsToInsert["artistImages"] as List<ArtistImageEntity>)

        }
        Log.d("MusicRepository", "syncMediaStore completed: ${idsToUpdateOrInsert.size} songs updated or inserted")
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
    private fun fetchFullSongDetails(songIds: Set<Long>): Map<String, List<Any>> {
        // This function will be very similar to your original `scanMediaStoreForSongs`,
        // but with an added WHERE clause to select by ID.
        if (songIds.isEmpty()) return emptyMap()

        val songList = mutableListOf<SongEntity>()
        val artistList = mutableListOf<ArtistEntity>()
        val albumList = mutableListOf<AlbumEntity>()
        val artistImageList = mutableListOf<ArtistImageEntity>()

        val selection = "${MediaStore.Audio.Media._ID} IN (${songIds.joinToString(",")})"

        // The projection here should include ALL columns needed for your SongEntity
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
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
                    artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)),
                    albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    trackNumber = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)),
                    dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)),
                    composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)),
                    bitrate = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)),
                    isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) == 1,
                    isPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) == 1,
                    isRingtone = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) == 1,
                    isAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) == 1,
                    isNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) == 1
                )
                val album = AlbumEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                    artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                )
                val artist = ArtistEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                )

                val nameSeparator = listOf(',', '-', '&')
                val artistNames = artist.name.split(*nameSeparator.toCharArray())

                artistNames.forEach { artistName ->
                    artistImageList.add(ArtistImageEntity(name = artistName))
                }

                songList.add(song)
                if (albumList.none { it.id == album.id }) {
                    albumList.add(album)
                }
                if (artistList.none { it.id == artist.id }) {
                    artistList.add(artist)
                }

            }
        }
        return mapOf<String, List<Any>>(
            "songs" to songList,
            "albums" to albumList,
            "artists" to artistList,
            "artistImages" to artistImageList
        )
    }


    // --- Song Access ---


    fun getAllSongs(
        query: String = "",
        artist: String = "",
        albumId: Long? = null,
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
        return Pager(config = pagingConfig) {

            songDao.getAllSongs(
                query = query,
                sortBy = sortBy,
                albumId = albumId,
                artist = artist,
                artistId = null,
                album = "",
                sortOrder = sortOrder
            )
        }.flow
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    // --- Playlist Access & Management ---

    fun getAllPlaylists(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<PlaylistEntity>> {
        return Pager(config = pagingConfig) {
            playlistDao.getAllPlaylists(
                query = query,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
        }.flow
    }

    fun getAllArtists(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<ArtistEntity>> {
        return Pager(config = pagingConfig) {
            songDao.getAllArtists(
                query = query,
                sortOrder = sortOrder
            )
        }.flow
    }

    fun getAllAlbums(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<AlbumEntity>> {
        return Pager(config = pagingConfig) {
            songDao.getAllAlbums(
                query = query,
                sortOrder = sortOrder
            )
        }.flow
    }

    fun getAllAritistImage(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<ArtistImageEntity>> = Pager(config = pagingConfig) {
        artistImageDao.getAllArtistImages(
            query = query,
            sortOrder = sortOrder
        )
    }.flow

    fun getPlaylistSongs(
        playlistId: Long,
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
        return Pager(config = pagingConfig) {
            playlistDao.getSongsInPlaylist(
                playlistId,
                query,
                sortBy,
                sortOrder
            )
        }.flow
    }

    suspend fun createPlaylist(playlist: PlaylistEntity) {
        playlistDao.createPlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    // --- Favorite Access & Management ---

    fun getFavoriteSongs(
        query: String = "",
        sortBy: SortBy = SortBy.ADDED_AT,
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
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

    suspend fun isFavorite(songId: Long?): Boolean {
        if (songId == null) return false
        return favoriteDao.isFavorite(songId) != null
    }

    // --- History & Stats Access ---

    fun getRecentlyPlayed(
        query: String = "",
        sortOrder: SortOrder = SortOrder.DESCENDING
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
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
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
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

    suspend fun updateArtistsImage(artistsImage: List<ArtistImageEntity>) {
        artistsImage.forEach { artistImage ->
            artistImageDao.upsertMerging(artistImage)
        }
    }

    suspend fun updateArtistImage(artistImage: ArtistImageEntity) {
        // 1. Fetch the current artist data from the database
        artistImageDao.upsertMerging(artistImage)
    }

    fun getAlbum(albumId: Long?): Flow<AlbumEntity?> {
        if (albumId == null) return flowOf(null)
        return songDao.getAlbumById(albumId)
    }

    fun getAlbumSongsCount(albumId: Long?): Flow<Int> {
        if (albumId == null) return flowOf(0)
        return songDao.getAlbumSongsCount(albumId)
    }

    fun getArtistByName(artistName: String): Flow<ArtistImageEntity?> {
        return artistImageDao.getArtistImageByName(artistName)
    }

    fun getArtistSongsCount(artistName: String): Flow<Int> {
        return songDao.getArtistSongsCount(artistName)
    }

    fun getPlaylistById(playlistId: Long?): Flow<PlaylistEntity?> {
        if (playlistId == null) return flowOf(null)
        return playlistDao.getPlaylistById(playlistId)
    }

    fun getPlaylistSongsCount(playlistId: Long?): Flow<Int> {
        if (playlistId == null) return flowOf(0)
        return playlistDao.getPlaylistSongsCount(playlistId)
    }

    suspend fun getAllSongsList(filter: SongFilter): List<SongWithAlbumAndArtist> {
        return when (filter.category) {
            is SongsCategory.Album -> songDao.getAllSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = filter.category.albumId,
                artist = "",
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.AllSongs -> songDao.getAllSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = null,
                artist = "",
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.Artist -> songDao.getAllSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = null,
                artist = filter.category.artistName,
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.LikedSongs -> favoriteDao.getFavoriteSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder
            )

            is SongsCategory.Playlist -> playlistDao.getSongsInPlaylistList(
                playlistId = filter.category.playlistId,
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder
            )
        }
    }

    fun isSongFavorite(songId: Long): Flow<Boolean> {
        // Call the new DAO function that returns a Flow
        return favoriteDao.isFavoriteFlow(songId)
            .map { favoriteSongEntity ->
                // Transform the result: if the entity is not null, it's a favorite.
                favoriteSongEntity != null
            }
    }

}