package org.ghost.musify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.ghost.musify.dao.FavoriteDao
import org.ghost.musify.dao.HistoryAndStatsDao
import org.ghost.musify.dao.PlaylistDao
import org.ghost.musify.dao.SongDao
import org.ghost.musify.entity.ArtistStatsEntity
import org.ghost.musify.entity.FavoriteSongEntity
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.PlaylistEntity
import org.ghost.musify.entity.PlaylistSongCrossRef
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.entity.SongStatsEntity

@Database(
    entities = [
        SongEntity::class,
        ArtistStatsEntity::class,
        FavoriteSongEntity::class,
        HistoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SongStatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MusifyDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun historyAndStatsDao(): HistoryAndStatsDao


    companion object {
        // Using @Volatile to ensure that the INSTANCE is always up-to-date
        @Volatile
        private var INSTANCE: MusifyDatabase? = null
        fun getInstance(context: Context): MusifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusifyDatabase::class.java,
                    "mf"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}