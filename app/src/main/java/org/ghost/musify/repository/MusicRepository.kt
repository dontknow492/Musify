package org.ghost.musify.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
import org.ghost.musify.entity.relation.HistoryWithSongDetails
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder
import org.ghost.musify.data.MediaFetchResult
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.models.SongsCategory
import javax.inject.Inject

class MusicRepository @Inject constructor(
    @param: ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyAndStatsDao: HistoryAndStatsDao,
    private val favoriteDao: FavoriteDao,
    private val artistImageDao: ArtistImageDao,
) {
    private val pagingConfig = PagingConfig(
        pageSize = 30,
        prefetchDistance = 10,
        initialLoadSize = 40,
        maxSize = 200,
        enablePlaceholders = false,
    )

    init {
        Log.d("MusicRepository", "MusicRepository initialized")

    }

    // --- Data Synchronization ---

    /**
     * Scans the MediaStore and synchronizes the local database with the findings.
     * This is the core logic for keeping the app's library up-to-date.
     */
    suspend fun syncMediaStore(
        allowedFolders: List<String> = emptyList(),
        minDurationSec: Int? = 30,
        excludedFolders: List<String> = emptyList(),
        separators: Set<Char> = setOf(',', '-', '&'),
    ) = withContext(Dispatchers.IO) {
        // Step 1: Fetch lightweight sync data (ID and timestamp) from both sources.
        Log.d("MusicRepository", "syncMediaStore started")
        val mediaStoreSongsMap = scanMediaStoreForSyncInfo(
            allowedFolders = allowedFolders,
            minDurationSec = minDurationSec,
            excludedFolders = excludedFolders,
        )
        val databaseSongsMap = songDao.getSongSyncInfo().associateBy({ it.id }, { it.dateModified })

        // Step 2: Identify songs to be deleted from the local database.
        // These are songs that exist in our DB but not in the MediaStore anymore.
        val deletedSongIds = databaseSongsMap.keys - mediaStoreSongsMap.keys
        if (deletedSongIds.isNotEmpty()) {
            songDao.deleteSongsByIds(deletedSongIds.toList())
            Log.d("MusicRepository", "Deleted ${deletedSongIds.size} songs.")
        }

        // Step 3: Identify new or updated songs.
        // A song is new if its ID is not in our DB.
        // A song is updated if its modification timestamp in MediaStore is newer.
        // Step 3: Identify new or updated songs.
        val idsToUpdateOrInsert = mediaStoreSongsMap.filter { (id, timestamp) ->
            id !in databaseSongsMap || timestamp > databaseSongsMap[id]!!
        }.keys

        // Step 4: Fetch full data for only the new/updated songs and save them.
        if (idsToUpdateOrInsert.isNotEmpty()) {
            Log.d("MusicRepository", "Fetching details for ${idsToUpdateOrInsert.size} new/updated songs.")
            val songsToInsert = fetchFullSongDetails(
                idsToUpdateOrInsert,
                separators = separators
            )

            // Assuming this runs in a single database transaction
            songDao.insertSongsWithAlbumAndArtist(
                songs = songsToInsert.songs,
                albums = songsToInsert.albums,
                artists = songsToInsert.artists,
            )

            updateArtistsImage(songsToInsert.artistImages)
        }

        Log.d(
            "MusicRepository",
            "syncMediaStore completed: ${idsToUpdateOrInsert.size} songs updated or inserted"
        )
    }

    /**
     * Scans the MediaStore for only the ID and DATE_MODIFIED columns for efficiency.
     * @return A Map of song ID to its modification timestamp.
     */
    private fun scanMediaStoreForSyncInfo(
        allowedFolders: List<String> = emptyList(),
        minDurationSec: Int? = 30,
        excludedFolders: List<String> = emptyList(),
    ): Map<Long, Long> {
        Log.d("MusicRepository", "scanMediaStoreForSyncInfo started")
        val songsMap = mutableMapOf<Long, Long>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        // These lists will hold the parts of our dynamic query
        val selectionParts = mutableListOf<String>("${MediaStore.Audio.Media.IS_MUSIC} != 0")
        val selectionArgs = mutableListOf<String>()

        // 1. Add duration filter if minDurationSec is not null and positive
        minDurationSec?.let { duration ->
            if (duration > 0) {
                selectionParts.add("${MediaStore.Audio.Media.DURATION} >= ?")
                selectionArgs.add((duration * 1000).toString())
            }
        }

        // 2. Add allowed folders filter if the list is not empty
        if (allowedFolders.isNotEmpty()) {
            val placeholders = allowedFolders.joinToString(" OR ") {
                "${MediaStore.Audio.Media.DATA} LIKE ?"
            }
            selectionParts.add("($placeholders)")
            allowedFolders.forEach { folderPath ->
                // Add wildcard to match all files within the folder
                selectionArgs.add("${folderPath.trimEnd('/')}/%")
            }

        }

        // 3. Add excluded folders filter if the list is not empty
        if (excludedFolders.isNotEmpty()) {
            val placeholders = excludedFolders.joinToString(" AND ") {
                "${MediaStore.Audio.Media.DATA} NOT LIKE ?"
            }
            selectionParts.add("($placeholders)")
            excludedFolders.forEach { folderPath ->
                selectionArgs.add("${folderPath.trimEnd('/')}/%")
            }
        }

        // 4. Combine all parts into the final selection string and arguments array
        val selection = if (selectionParts.isEmpty()) null else selectionParts.joinToString(" AND ")
        val finalSelectionArgs = if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray()


        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            finalSelectionArgs,
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
    private fun fetchFullSongDetails(
        songIds: Set<Long>,
        separators: Set<Char>
    ): MediaFetchResult {
        // This function will be very similar to your original `scanMediaStoreForSongs`,
        // but with an added WHERE clause to select by ID.
        if (songIds.isEmpty()) return MediaFetchResult(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList()
        )

        val songList = mutableListOf<SongEntity>()
        val artistList = mutableListOf<ArtistEntity>()
        val albumList = mutableListOf<AlbumEntity>()
        val artistImageList = mutableListOf<ArtistImageEntity>()


        // Use Sets for efficient duplicate checking
        val seenAlbumIds = mutableSetOf<Long>()
        val seenArtistIds = mutableSetOf<Long>()



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

        // Step 2: Batch the queries to avoid the 999 variable limit
        songIds.chunked(900).forEach { batchIds ->
            // Step 3: Use placeholders '?' for security
            val selection = "${MediaStore.Audio.Media._ID} IN (${batchIds.joinToString { "?" }})"
            val selectionArgs = batchIds.map { it.toString() }.toTypedArray()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val composerCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)
                val bitrateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
                val isMusicCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                // --- Added indices for the new columns ---
                val isPodcastCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)
                val isRingtoneCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)
                val isAlarmCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)
                val isNotificationCol =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)


                while (cursor.moveToNext()) {
                    val artistId = cursor.getLong(artistIdCol)
                    val albumId = cursor.getLong(albumIdCol)

                    // --- Updated SongEntity constructor with new fields ---
                    songList.add(
                        SongEntity(
                            id = cursor.getLong(idCol),
                            title = cursor.getString(titleCol),
                            artistId = artistId,
                            albumId = albumId,
                            duration = cursor.getInt(durationCol),
                            trackNumber = cursor.getInt(trackCol),
                            year = cursor.getInt(yearCol),
                            filePath = cursor.getString(dataCol),
                            dateAdded = cursor.getLong(dateAddedCol),
                            dateModified = cursor.getLong(dateModifiedCol),
                            size = cursor.getLong(sizeCol),
                            mimeType = cursor.getString(mimeTypeCol),
                            composer = cursor.getString(composerCol),
                            bitrate = cursor.getInt(bitrateCol),
                            isMusic = cursor.getInt(isMusicCol) == 1,
                            isPodcast = cursor.getInt(isPodcastCol) == 1,
                            isRingtone = cursor.getInt(isRingtoneCol) == 1,
                            isAlarm = cursor.getInt(isAlarmCol) == 1,
                            isNotification = cursor.getInt(isNotificationCol) == 1
                        )
                    )
                    if (seenAlbumIds.add(albumId)) {
                        albumList.add(
                            AlbumEntity(
                                id = albumId,
                                title = cursor.getString(albumCol),
                                artist = cursor.getString(artistCol)
                            )
                        )
                    }
                    if (seenArtistIds.add(artistId)) {
                        val artistName = cursor.getString(artistCol)
                        artistList.add(
                            ArtistEntity(
                                id = artistId,
                                name = artistName
                            )
                        )

                        // Split artist names and trim whitespace
                        val nameSeparators = separators.toCharArray()
                        artistName.split(*nameSeparators)
                            .forEach { namePart ->
                                val trimmedName = namePart.trim()
                                if (trimmedName.isNotEmpty()) {
                                    artistImageList.add(ArtistImageEntity(name = trimmedName))
                                }
                            }
                    }


                }
            }

        }
        return MediaFetchResult(songList, albumList, artistList, artistImageList)
    }


    suspend fun reparseArtists(separators: Set<Char>) = withContext(Dispatchers.IO) {
        Log.d("MusicRepository", "Reparsing artists with new separators: $separators")

        // Step 1: Get all artist names that are ALREADY in our database.
        val existingNames = artistImageDao.getAllNames().toSet()

        // Step 2: Get all artists to generate the FULL list of required names.
        val allArtists = artistImageDao.getAllArtistImagesAsList()
        val requiredNames = mutableSetOf<String>()
        val regex = Regex("[\\s${Regex.escape(separators.joinToString(""))}]+")

        allArtists.forEach { artist ->
            artist.name.split(regex).forEach { namePart ->
                val trimmedName = namePart.trim()
                if (trimmedName.isNotEmpty()) {
                    requiredNames.add(trimmedName)
                }
            }
        }

        // Step 3: Find only the names that are in the required list but not in our database yet.
        val newNamesToInsert = requiredNames - existingNames

        Log.d("MusicRepository", "Found ${newNamesToInsert.size} new artist names to insert.")

        // Step 4: If there are new names, create entities for them and insert.
        // The IGNORE strategy will protect existing entries.
        if (newNamesToInsert.isNotEmpty()) {
            val newImages = newNamesToInsert.map { artistName ->
                ArtistImageEntity(name = artistName) // New entries will have null image fields
            }
            artistImageDao.insertAll(newImages)
        }
    }

    // --- Song Access ---

    fun filterSongs(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING,
        artist: String = "",
        album: String = "",
        artistId: Long? = null,
        albumId: Long? = null,
        playlistId: Long? = null,
        favoritesOnly: Boolean = false,
    ): Flow<PagingData<SongWithAlbumAndArtist>> {
        val sortByString = sortBy.value
        val sortOrderString = sortOrder.value
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                songDao.getAllSongs(
                    query = query,
                    sortBy = sortByString,
                    albumId = albumId,
                    artist = artist,
                    artistId = artistId,
                    album = album,
                    sortOrder = sortOrderString,
                    playlistId = playlistId,
                    fetchFavoritesOnly = favoritesOnly
                )
            }
        ).flow
    }

    suspend fun filterSongsList(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING,
        artist: String = "",
        album: String = "",
        artistId: Long? = null,
        albumId: Long? = null,
        playlistId: Long? = null,
        favoritesOnly: Boolean = false,
    ): List<SongWithAlbumAndArtist> {
        val sortByString = sortBy.value
        val sortOrderString = sortOrder.value
        return songDao.getAllSongsList(
            query = query,
            sortBy = sortByString,
            albumId = albumId,
            artist = artist,
            artistId = artistId,
            album = album,
            sortOrder = sortOrderString,
            playlistId = playlistId,
            fetchFavoritesOnly = favoritesOnly
        )
    }

    suspend fun filterSongsListId(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING,
        artist: String = "",
        album: String = "",
        artistId: Long? = null,
        albumId: Long? = null,
        playlistId: Long? = null,
        favoritesOnly: Boolean = false,
    ): List<Long> {
        val sortByString = sortBy.value
        val sortOrderString = sortOrder.value
        return songDao.getAllSongIds(
            query = query,
            sortBy = sortByString,
            albumId = albumId,
            artist = artist,
            artistId = artistId,
            album = album,
            sortOrder = sortOrderString,
            playlistId = playlistId,
            fetchFavoritesOnly = favoritesOnly
        )
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


    suspend fun createPlaylist(playlist: PlaylistEntity) {
        playlistDao.createPlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    // --- Favorite Access & Management ---

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
            is SongsCategory.Album -> filterSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = filter.category.albumId,
                artist = "",
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.AllSongs -> filterSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = null,
                artist = "",
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.Artist -> filterSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                albumId = null,
                artist = filter.category.artistName,
                artistId = null,
                album = "",
                sortOrder = filter.sortOrder
            )

            is SongsCategory.LikedSongs -> filterSongsList(
                query = filter.searchQuery ?: "",
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder,
                favoritesOnly = true
            )

            is SongsCategory.Playlist -> filterSongsList(
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


    suspend fun getAllArtistImageAsList(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<ArtistImageEntity> = artistImageDao.getAllArtistImagesAsList(
        query = query,
        sortOrder = sortOrder
    )

    suspend fun getAllPlaylistAsList(
        query: String = "",
        sortBy: SortBy = SortBy.TITLE,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<PlaylistEntity> = playlistDao.getAllPlaylistsAsList(
        query = query,
        sortBy = sortBy,
        sortOrder = sortOrder
    )

    suspend fun getAllAlbumsAsList(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<AlbumEntity> = songDao.getAllAlbumsAsList(
        query = query,
        sortOrder = sortOrder
    )

    fun getFullHistoryPlayback(
        minTimestamp: Long? = null,
        maxTimestamp: Long? = null
    ): Flow<PagingData<HistoryWithSongDetails>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            historyAndStatsDao.getFullPlaybackHistory(
                minTimestamp = minTimestamp,
                maxTimestamp = maxTimestamp
            )
        }
    ).flow

    suspend fun deleteHistory(entry: HistoryEntity) {
        historyAndStatsDao.delete(entry)
    }

    suspend fun deleteHistory(id: Long) {
        historyAndStatsDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyAndStatsDao.deleteAll()
    }

    fun getSongDetailWithLikeStatus(songId: Long): Flow<SongDetailsWithLikeStatus?> {
        return songDao.getSongWithLikeStatusById(songId)
    }

    fun getPlaylistIdsForSong(songId: Long): Flow<List<Long>> {
        return playlistDao.getPlaylistIdsForSong(songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId
            )
        )
    }

    fun getSongsDetailsWithLikeStatusByIds(songIds: List<Long>): Flow<List<SongDetailsWithLikeStatus>> {
        return songDao.getSongsDetailsWithLikeStatusByIds(songIds)
    }

    fun getSongDetailsWithLikeStatusById(songId: Long): Flow<SongDetailsWithLikeStatus?> {
        return songDao.getSongDetailsWithLikeStatusById(songId)
    }


}