package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.PlaylistSongCrossRef
import org.ghost.musify.entity.relation.PlaylistWithSongs
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

/**
 * DAO for managing playlists and the relationship between playlists and songs.
 */
@Dao
interface PlaylistDao {
    // --- Playlist Management ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query(
        """
        SELECT * FROM playlists
        WHERE playlist_name LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'NAME' AND :sortOrder = 'ASCENDING' THEN playlist_name END ASC,
            CASE WHEN :sortBy = 'NAME' AND :sortOrder = 'DESCENDING' THEN playlist_name END DESC,
            CASE WHEN :sortBy = 'DATE_CREATED' AND :sortOrder = 'ASCENDING' THEN created_at END ASC,
            CASE WHEN :sortBy = 'DATE_CREATED' AND :sortOrder = 'DESCENDING' THEN created_at END DESC
    """
    )
    fun getAllPlaylists(
        query: String,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): PagingSource<Int, PlaylistEntity>

    @Update
    suspend fun renamePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    // --- Song Management in Playlists ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(join: PlaylistSongCrossRef)

    @Delete
    suspend fun removeSongFromPlaylist(join: PlaylistSongCrossRef)

    // --- Complex Read (Get a playlist with all its songs) ---
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs>

    // --- Paging for songs within a specific playlist ---
    fun getSongsInPlaylist(
        playlistId: Long,
        query: String,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): PagingSource<Int, SongWithAlbumAndArtist> {
        return if (sortOrder == SortOrder.ASCENDING) {
            getSongsInPlaylistAsc(playlistId, query, sortBy.value)
        } else {
            getSongsInPlaylistDesc(playlistId, query, sortBy.value)
        }
    }

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_join psj ON s.id = psj.song_id
        WHERE psj.playlist_id = :playlistId AND s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN s.title END ASC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END ASC,
            CASE WHEN :sortBy = 'year' THEN s.year END ASC,
            CASE WHEN :sortBy = 'date_added' THEN s.date_added END ASC
    """
    )
    fun getSongsInPlaylistAsc(
        playlistId: Long,
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>


    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_join psj ON s.id = psj.song_id
        WHERE psj.playlist_id = :playlistId AND s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'title' THEN s.title END DESC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END DESC,
            CASE WHEN :sortBy = 'year' THEN s.year END DESC,
            CASE WHEN :sortBy = 'date_added' THEN s.date_added END DESC
    """
    )
    fun getSongsInPlaylistDesc(
        playlistId: Long,
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Query("SELECT * FROM playlists WHERE id = :playlistId limit 1")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Query("SELECT COUNT(*) FROM playlist_song_join WHERE playlist_id = :playlistId")
    fun getPlaylistSongsCount(playlistId: Long): Flow<Int>


}