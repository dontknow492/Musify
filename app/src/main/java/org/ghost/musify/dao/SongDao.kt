package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.entity.relation.SongSyncInfo
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

    @Update
    suspend fun updateSong(song: SongEntity)

    // --- Read (with Paging Support) ---
    fun getAllSongs(
        query: String,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): PagingSource<Int, SongEntity> {
        return if (sortOrder == SortOrder.ASCENDING) {
            getSongsAsc(query, sortBy.value)
        } else {
            getSongsDesc(query, sortBy.value)
        }
    }

    @Query(
        """
        SELECT * FROM songs WHERE title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN title END ASC,
            CASE WHEN :sortBy = 'duration' THEN duration END ASC,
            CASE WHEN :sortBy = 'year' THEN year END ASC,
            CASE WHEN :sortBy = 'date_added' THEN date_added END ASC,
            CASE WHEN :sortBy = 'date_modified' THEN date_modified END ASC
    """
    )
    fun getSongsAsc(query: String, sortBy: String): PagingSource<Int, SongEntity>

    @Query(
        """
        SELECT * FROM songs WHERE title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN title END DESC,
            CASE WHEN :sortBy = 'duration' THEN duration END DESC,
            CASE WHEN :sortBy = 'year' THEN year END DESC,
            CASE WHEN :sortBy = 'date_added' THEN date_added END DESC,
            CASE WHEN :sortBy = 'date_modified' THEN date_modified END DESC
    """
    )
    fun getSongsDesc(query: String, sortBy: String): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track_number ASC")
    fun getSongsByAlbumId(albumId: Long): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE artist = :artistName ORDER BY title ASC")
    fun getSongsByArtist(artistName: String): PagingSource<Int, SongEntity>

    // --- Delete ---
    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun deleteSongsByIds(songIds: List<Long>)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs(): Int

    /**
     * Efficiently fetches only the ID and modification timestamp for all songs
     * from the local database.
     */
    @Query("SELECT id, date_modified FROM songs")
    suspend fun getSongSyncInfo(): List<SongSyncInfo>

}

