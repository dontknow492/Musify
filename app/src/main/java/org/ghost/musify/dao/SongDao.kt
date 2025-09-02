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
    fun getAllSongs(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): PagingSource<Int, SongWithAlbumAndArtist> {
        return if (sortOrder == SortOrder.ASCENDING) {
            getSongsAsc(query, artist, album, artistId, albumId, sortBy.value)
        } else {
            getSongsDesc(query, artist, album, artistId, albumId, sortBy.value)
        }
    }

    @Transaction
    @Query(
        """
        SELECT songs.* FROM songs
            INNER JOIN artists ON songs.artist_id = artists.id
            INNER JOIN albums ON songs.album_id = albums.id
            WHERE
                songs.title LIKE '%' || :query || '%'
                AND artists.name LIKE '%' || :artist || '%'
                AND albums.title LIKE '%' || :album || '%'
                AND (:artistId IS NULL OR songs.artist_id = :artistId)
                AND (:albumId IS NULL OR songs.album_id = :albumId)
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN songs.title END ASC,
            CASE WHEN :sortBy = 'duration' THEN songs.duration END ASC,
            CASE WHEN :sortBy = 'year' THEN songs.year END ASC,
            CASE WHEN :sortBy = 'date_added' THEN songs.date_added END ASC,
            CASE WHEN :sortBy = 'date_modified' THEN songs.date_modified END ASC
    """
    )
    fun getSongsAsc(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Transaction
    @Query(
        """
            SELECT songs.* FROM songs
            INNER JOIN artists ON songs.artist_id = artists.id
            INNER JOIN albums ON songs.album_id = albums.id
            WHERE
                songs.title LIKE '%' || :query || '%'
                AND artists.name LIKE '%' || :artist || '%'
                AND albums.title LIKE '%' || :album || '%'
                AND (:artistId IS NULL OR songs.artist_id = :artistId)
                AND (:albumId IS NULL OR songs.album_id = :albumId)
            ORDER BY
                CASE WHEN :sortBy = 'title' THEN songs.title END DESC,
                CASE WHEN :sortBy = 'duration' THEN songs.duration END DESC,
                CASE WHEN :sortBy = 'year' THEN songs.year END DESC,
                CASE WHEN :sortBy = 'date_added' THEN songs.date_added END DESC,
                CASE WHEN :sortBy = 'date_modified' THEN songs.date_modified END DESC
    """
    )
    fun getSongsDesc(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

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


    // one time fetch
    suspend fun getAllSongsList(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): List<SongWithAlbumAndArtist> {
        return if (sortOrder == SortOrder.ASCENDING) {
            getSongsAscList(query, artist, album, artistId, albumId, sortBy.value)
        } else {
            getSongsDescList(query, artist, album, artistId, albumId, sortBy.value)
        }
    }

    // The new suspend function for ASCENDING order
    @Transaction
    @Query(
        """
    SELECT songs.* FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        INNER JOIN albums ON songs.album_id = albums.id
        WHERE
            songs.title LIKE '%' || :query || '%'
            AND artists.name LIKE '%' || :artist || '%'
            AND albums.title LIKE '%' || :album || '%'
            AND (:artistId IS NULL OR songs.artist_id = :artistId)
            AND (:albumId IS NULL OR songs.album_id = :albumId)
    ORDER BY
        CASE WHEN :sortBy = 'title' THEN songs.title END ASC,
        CASE WHEN :sortBy = 'duration' THEN songs.duration END ASC,
        CASE WHEN :sortBy = 'year' THEN songs.year END ASC,
        CASE WHEN :sortBy = 'date_added' THEN songs.date_added END ASC,
        CASE WHEN :sortBy = 'date_modified' THEN songs.date_modified END ASC
"""
    )
    suspend fun getSongsAscList(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: String
    ): List<SongWithAlbumAndArtist>

    // The new suspend function for DESCENDING order
    @Transaction
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN artists ON songs.artist_id = artists.id
        INNER JOIN albums ON songs.album_id = albums.id
        WHERE
            songs.title LIKE '%' || :query || '%'
            AND artists.name LIKE '%' || :artist || '%'
            AND albums.title LIKE '%' || :album || '%'
            AND (:artistId IS NULL OR songs.artist_id = :artistId)
            AND (:albumId IS NULL OR songs.album_id = :albumId)
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN songs.title END DESC,
            CASE WHEN :sortBy = 'duration' THEN songs.duration END DESC,
            CASE WHEN :sortBy = 'year' THEN songs.year END DESC,
            CASE WHEN :sortBy = 'date_added' THEN songs.date_added END DESC,
            CASE WHEN :sortBy = 'date_modified' THEN songs.date_modified END DESC
"""
    )
    suspend fun getSongsDescList(
        query: String,
        artist: String,
        album: String,
        artistId: Long?,
        albumId: Long?,
        sortBy: String
    ): List<SongWithAlbumAndArtist>


//    @Query("SELECT * FROM songs WHERE id = :songId")
//    suspend fun getSongWithAlbumAndArtistById(songId: Long): SongWithAlbumAndArtist?


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
}

