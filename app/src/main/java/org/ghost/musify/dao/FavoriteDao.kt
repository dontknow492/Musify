package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.ghost.musify.entity.FavoriteSongEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

/**
 * DAO for managing the user's favorite songs.
 */
@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE song_id = :songId")
    suspend fun removeFromFavorites(songId: Long): Int

    @Query("SELECT * FROM favorite_songs WHERE song_id = :songId")
    suspend fun isFavorite(songId: Long): FavoriteSongEntity?

    fun getFavoriteSongs(
        query: String,
        sortBy: SortBy,
        sortOrder: SortOrder
    ): PagingSource<Int, SongWithAlbumAndArtist> {
        return if (sortOrder == SortOrder.ASCENDING) {
            getFavoriteSongsAsc(query, sortBy.value)
        } else {
            getFavoriteSongsDesc(query, sortBy.value)
        }
    }

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN favorite_songs f ON s.id = f.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'added_at' THEN f.added_at END ASC,
            CASE WHEN :sortBy = 'title' THEN s.title END ASC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END ASC
    """
    )
    fun getFavoriteSongsAsc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN favorite_songs f ON s.id = f.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'added_at' THEN f.added_at END DESC,
            CASE WHEN :sortBy = 'title' THEN s.title END DESC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END DESC
    """
    )
    fun getFavoriteSongsDesc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>
}