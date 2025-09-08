package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.entity.ArtistEntity
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.entity.relation.SongSyncInfo
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

/**
 * Data Access Object for the SongEntity. This is the primary DAO for managing
 * the music library itself.
 */
@Dao
interface SongDao {
    // --- Insert & Update ---
    


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artist: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Transaction
    suspend fun insertSongWithAlbumAndArtist(
        song: SongEntity,
        album: AlbumEntity,
        artist: ArtistEntity
    ) {
        insertArtist(artist)
        insertAlbum(album)
        insertSong(song)
    }

    @Transaction
    suspend fun insertSongsWithAlbumAndArtist(
        songs: List<SongEntity>,
        albums: List<AlbumEntity>,
        artists: List<ArtistEntity>
    ) {
        insertArtists(artists)
        insertAlbums(albums)
        insertSongs(songs)
    }


    @Update
    suspend fun updateSong(song: SongEntity)

    // --- Read (with Paging Support) ---



    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track_number ASC")
    fun getSongsByAlbumId(albumId: Long): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE artist_id = :artistId ORDER BY title ASC")
    fun getSongsByArtist(artistId: Long): PagingSource<Int, SongEntity>

    // --- Delete ---
    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun deleteSongsByIds(songIds: List<Long>)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs(): Int

    @Query(
        """
        SELECT * FROM albums
        WHERE title LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN :sortOrder = 'ASCENDING' THEN title END ASC,
            CASE WHEN :sortOrder = 'DESCENDING' THEN title END DESC
    """
    )
    fun getAllAlbums(
        query: String,
        sortOrder: SortOrder
    ): PagingSource<Int, AlbumEntity>


    @Query(
        """
        SELECT * FROM albums
        WHERE title LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN :sortOrder = 'ASCENDING' THEN title END ASC,
            CASE WHEN :sortOrder = 'DESCENDING' THEN title END DESC
    """
    )
    suspend fun getAllAlbumsAsList(
        query: String,
        sortOrder: SortOrder
    ): List<AlbumEntity>

    @Query(
        """
        SELECT * FROM artists
        WHERE name LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortOrder = 'ASCENDING' THEN name END ASC,
            CASE WHEN :sortOrder = 'DESCENDING' THEN name END DESC
    """
    )
    fun getAllArtists(
        query: String,
        sortOrder: SortOrder // Assuming this is an enum with ASCENDING, DESCENDING
    ): PagingSource<Int, ArtistEntity>


    /**
     * Efficiently fetches only the ID and modification timestamp for all songs
     * from the local database.
     */
    @Query("SELECT id, date_modified FROM songs")
    suspend fun getSongSyncInfo(): List<SongSyncInfo>


    @Query("SELECT * FROM albums WHERE id = :albumId limit 1")
    fun getAlbumById(albumId: Long): Flow<AlbumEntity?>

    @Query("SELECT COUNT(*) FROM songs WHERE album_id = :albumId")
    fun getAlbumSongsCount(albumId: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        WHERE artists.name like '%' || :artistName || '%'
    """
    )
    fun getArtistSongsCount(artistName: String): Flow<Int>





    /**
     * Fetches all songs from the database, joining them with their "favorite" status.
     *
     * @Transaction is required because the result includes a @Relation from
     * the nested SongWithAlbumAndArtist class.
     *
     * The LEFT JOIN ensures that all songs are returned, even if they are not
     * in the favorite_songs table. If no match is found, the fields for
     * FavoriteSongEntity will be null, and Room will correctly construct the
     * SongWithLikeStatus object with a null 'liked' property.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM songs 
        LEFT JOIN favorite_songs ON songs.id = favorite_songs.song_id
    """
    )
    fun getSongsWithLikeStatus(): PagingSource<Int, SongDetailsWithLikeStatus>

    /**
     * Fetches a single song by its ID, along with its "liked" status.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM songs 
        LEFT JOIN favorite_songs ON songs.id = favorite_songs.song_id
        WHERE songs.id = :songId
    """
    )
    fun getSongWithLikeStatusById(songId: Long): Flow<SongDetailsWithLikeStatus?>

    /**
     * Fetches songs with their related album and artist details for a given list of IDs,
     * preserving the original order of the ID list.
     *
     * @param songIds The ordered list of song IDs to fetch.
     * @return A list of SongWithAlbumAndArtist objects in the same order as the input IDs.
     */
    @Transaction
    @Query("""
        SELECT * FROM songs
        WHERE id IN (:songIds)
    """)
    fun getSongsDetailsWithLikeStatusByIds(songIds: List<Long>): Flow<List<SongDetailsWithLikeStatus>>


    

    // The single, unified query that handles all sorting.
    // It's better to make this private or protected.
    @Transaction
    @Query(
        """
        SELECT songs.id FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        INNER JOIN albums ON songs.album_id = albums.id
        LEFT JOIN favorite_songs f ON songs.id = f.song_id
        WHERE
            songs.title LIKE '%' || :query || '%'
            AND artists.name LIKE '%' || :artist || '%'
            AND albums.title LIKE '%' || :album || '%'
            AND (:artistId IS NULL OR songs.artist_id = :artistId)
            AND (:albumId IS NULL OR songs.album_id = :albumId)
            -- The subquery needs the table name specified as well
            AND (:playlistId IS NULL OR songs.id IN (
                SELECT song_id FROM playlist_song_join WHERE playlist_id = :playlistId
            ))
            AND (:fetchFavoritesOnly = 0 OR f.song_id IS NOT NULL)
            
        ORDER BY
            -- This complex CASE statement handles both column and direction dynamically
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASCENDING' THEN songs.title END ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESCENDING' THEN songs.title END DESC,
            
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'ASCENDING' THEN songs.duration END ASC,
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'DESCENDING' THEN songs.duration END DESC,
            
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'ASCENDING' THEN songs.year END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'DESCENDING' THEN songs.year END DESC,
            
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'ASCENDING' THEN songs.date_added END ASC,
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'DESCENDING' THEN songs.date_added END DESC,

            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'ASCENDING' THEN songs.date_modified END ASC,
            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'DESCENDING' THEN songs.date_modified END DESC
        """
    )
    suspend fun getAllSongIds(
        fetchFavoritesOnly: Boolean = false,
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        playlistId: Long? = null,
        sortBy: String,
        sortOrder: String // Pass the order as a string: "ASCENDING" or "DESCENDING"
    ): List<Long>


    @Transaction
    @Query(
        """
        SELECT * FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        INNER JOIN albums ON songs.album_id = albums.id
        LEFT JOIN favorite_songs f ON songs.id = f.song_id
        WHERE
            songs.title LIKE '%' || :query || '%'
            AND artists.name LIKE '%' || :artist || '%'
            AND albums.title LIKE '%' || :album || '%'
            AND (:artistId IS NULL OR songs.artist_id = :artistId)
            AND (:albumId IS NULL OR songs.album_id = :albumId)
            -- The subquery needs the table name specified as well
            AND (:playlistId IS NULL OR songs.id IN (
                SELECT song_id FROM playlist_song_join WHERE playlist_id = :playlistId
            ))
            AND (:fetchFavoritesOnly = 0 OR f.song_id IS NOT NULL)
        ORDER BY
            -- This complex CASE statement handles both column and direction dynamically
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASCENDING' THEN songs.title END ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESCENDING' THEN songs.title END DESC,
            
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'ASCENDING' THEN songs.duration END ASC,
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'DESCENDING' THEN songs.duration END DESC,
            
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'ASCENDING' THEN songs.year END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'DESCENDING' THEN songs.year END DESC,
            
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'ASCENDING' THEN songs.date_added END ASC,
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'DESCENDING' THEN songs.date_added END DESC,

            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'ASCENDING' THEN songs.date_modified END ASC,
            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'DESCENDING' THEN songs.date_modified END DESC
    """)
    suspend fun getAllSongsList(
        fetchFavoritesOnly: Boolean = false,
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        playlistId: Long? = null,
        sortBy: String,
        sortOrder: String // Pass the order as a string: "ASCENDING" or "DESCENDING"
    ): List<SongWithAlbumAndArtist>


    @Transaction
    @Query(
        """
        SELECT * FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        INNER JOIN albums ON songs.album_id = albums.id
        LEFT JOIN favorite_songs f ON songs.id = f.song_id
        WHERE
            songs.title LIKE '%' || :query || '%'
            AND artists.name LIKE '%' || :artist || '%'
            AND albums.title LIKE '%' || :album || '%'
            AND (:artistId IS NULL OR songs.artist_id = :artistId)
            AND (:albumId IS NULL OR songs.album_id = :albumId)
            -- The subquery needs the table name specified as well
            AND (:playlistId IS NULL OR songs.id IN (
                SELECT song_id FROM playlist_song_join WHERE playlist_id = :playlistId
            ))
            AND (:fetchFavoritesOnly = 0 OR f.song_id IS NOT NULL)
        ORDER BY
            -- This complex CASE statement handles both column and direction dynamically
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASCENDING' THEN songs.title END ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESCENDING' THEN songs.title END DESC,
            
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'ASCENDING' THEN songs.duration END ASC,
            CASE WHEN :sortBy = 'duration' AND :sortOrder = 'DESCENDING' THEN songs.duration END DESC,
            
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'ASCENDING' THEN songs.year END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'DESCENDING' THEN songs.year END DESC,
            
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'ASCENDING' THEN songs.date_added END ASC,
            CASE WHEN :sortBy = 'date_added' AND :sortOrder = 'DESCENDING' THEN songs.date_added END DESC,

            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'ASCENDING' THEN songs.date_modified END ASC,
            CASE WHEN :sortBy = 'date_modified' AND :sortOrder = 'DESCENDING' THEN songs.date_modified END DESC
    """
    )
    fun getAllSongs(
        fetchFavoritesOnly: Boolean = false,
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        playlistId: Long? = null,
        sortBy: String,
        sortOrder: String // Pass the order as a string: "ASCENDING" or "DESCENDING"
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    fun getSongDetailsWithLikeStatusById(songId: Long): Flow<SongDetailsWithLikeStatus?>
}

