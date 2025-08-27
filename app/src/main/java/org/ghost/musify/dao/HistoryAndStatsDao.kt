package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.ghost.musify.entity.ArtistStatsEntity
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.SongStatsEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.enums.SortBy
import org.ghost.musify.enums.SortOrder

/**
 * DAO for managing playback history and statistics.
 */
@Dao
interface HistoryAndStatsDao {
    // --- History ---
    @Insert
    suspend fun addToHistory(history: HistoryEntity)


    // --- Song Statistics ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSongStats(stats: SongStatsEntity)

    @Query("SELECT * FROM song_stats WHERE song_id = :songId")
    suspend fun getSongStats(songId: Long): SongStatsEntity?

    fun getRecentlyPlayed(
        query: String,
        sortOrder: SortOrder
    ): PagingSource<Int, SongWithAlbumAndArtist> {
        val sortBy = SortBy.PLAYED_AT
        return if (sortOrder == SortOrder.ASCENDING) {
            getHistoryPlayedAsc(query, sortBy.value)
        } else {
            getHistoryPlayedDesc(query, sortBy.value)
        }
    }

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN play_history h ON s.id = h.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'played_at' THEN h.played_at END ASC,
            CASE WHEN :sortBy = 'title' THEN s.title END ASC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END ASC
    """
    )
    fun getHistoryPlayedAsc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN play_history h ON s.id = h.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'played_at' THEN h.played_at END DESC,
            CASE WHEN :sortBy = 'title' THEN s.title END DESC,
            CASE WHEN :sortBy = 'duration_played' THEN s.duration END DESC
    """
    )
    fun getHistoryPlayedDesc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>


    fun getTopPlayedSongs(
        query: String,
        sortOrder: SortOrder
    ): PagingSource<Int, SongWithAlbumAndArtist> {
        val sortBy = SortBy.PLAY_COUNT
        return if (sortOrder == SortOrder.ASCENDING) {
            getStatsPlayedSongsAsc(query, sortBy.value)
        } else {
            getStatsPlayedSongsDesc(query, sortBy.value)
        }
    }

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'play_count' THEN st.play_count END ASC,
            CASE WHEN :sortBy = 'title' THEN s.title END ASC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END ASC
    """
    )
    fun getStatsPlayedSongsAsc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>

    @Transaction
    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.song_id
        WHERE s.title LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortBy = 'play_count' THEN st.play_count END DESC,
            CASE WHEN :sortBy = 'title' THEN s.title END DESC,
            CASE WHEN :sortBy = 'duration' THEN s.duration END DESC
    """
    )
    fun getStatsPlayedSongsDesc(
        query: String,
        sortBy: String
    ): PagingSource<Int, SongWithAlbumAndArtist>


    // --- Artist Statistics ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateArtistStats(stats: ArtistStatsEntity)

    @Query("SELECT * FROM artist_stats WHERE artist_name = :artistName")
    suspend fun getArtistStats(artistName: String): ArtistStatsEntity?

    @Query("SELECT * FROM artist_stats ORDER BY total_play_count DESC")
    fun getTopArtists(): PagingSource<Int, ArtistStatsEntity>
}