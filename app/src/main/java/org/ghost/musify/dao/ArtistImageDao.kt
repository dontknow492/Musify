package org.ghost.musify.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.enums.SortOrder

@Dao
interface ArtistImageDao {
    // --- WRITE OPERATIONS ---

    /**
     * Inserts or replaces a single artist image.
     * The REPLACE strategy means if an artist with the same name (primary key)
     * already exists, it will be replaced. This is an "upsert" (update or insert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(artistImage: ArtistImageEntity)

    /**
     * Inserts or replaces a list of artist images in a single transaction.
     * This is much more efficient than calling upsert() in a loop.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(artistImages: List<ArtistImageEntity>)


    // --- READ OPERATIONS ---

    /**
     * Retrieves a single artist's image information by their name.
     * Returns null if the artist is not found in the database.
     */
    @Query("SELECT * FROM artists_image WHERE name = :name LIMIT 1")
    fun getArtistImageByName(name: String): Flow<ArtistImageEntity?>

    /**
     * Retrieves a list of artist images for a given list of names.
     * Perfect for efficiently loading images for a playlist or album view.
     */
    @Query("SELECT * FROM artists_image WHERE name IN (:names)")
    suspend fun getArtistImagesByNames(names: List<String>): List<ArtistImageEntity>


    @Query(
        """
        SELECT * FROM artists_image
        WHERE name LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortOrder = 'ASCENDING' THEN name END ASC,
            CASE WHEN :sortOrder = 'DESCENDING' THEN name END DESC
    """
    )
    fun getAllArtistImages(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): PagingSource<Int, ArtistImageEntity>


    @Query(
        """
        SELECT * FROM artists_image
        WHERE name LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN :sortOrder = 'ASCENDING' THEN name END ASC,
            CASE WHEN :sortOrder = 'DESCENDING' THEN name END DESC
    """
    )
    suspend fun getAllArtistImagesAsList(
        query: String = "",
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<ArtistImageEntity>


    // --- UTILITY & MAINTENANCE OPERATIONS ---

    /**
     * Checks which artist names from the input list already exist in the database.
     * This is highly optimized for the sync workflow, as it only returns the names (String)
     * instead of the full ArtistImage objects.
     */
    @Query("SELECT name FROM artists_image WHERE name IN (:artistNames)")
    suspend fun findExistingArtistNames(artistNames: List<String>): List<String>

    /**
     * Deletes all entries from the artists_image table.
     * Useful for a "clear cache" user-facing feature.
     */
    @Query("DELETE FROM artists_image")
    suspend fun clearAll()

    @Transaction
    suspend fun upsertMerging(artistImage: ArtistImageEntity) {
        val existingArtist = getArtistImageByName(artistImage.name).firstOrNull()
        if (existingArtist == null) {
            upsert(artistImage)
        } else {
            val updatedArtist = existingArtist.copy(
                // Use the new URL if it's not null, otherwise keep the old one
                imageUrl = artistImage.imageUrl ?: existingArtist.imageUrl,

                // Use the new UriId if it's not null, otherwise keep the old one
                imageUriId = artistImage.imageUriId ?: existingArtist.imageUriId
            )

            upsert(updatedArtist)
        }
    }


}