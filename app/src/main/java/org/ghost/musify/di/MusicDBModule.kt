package org.ghost.musify.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ghost.musify.database.MusifyDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MusicDBModule {

    @Singleton
    @Provides
    fun provideMusicDatabase(@ApplicationContext context: Context): MusifyDatabase {
        val db = MusifyDatabase.Companion.getInstance(context)
//        val artistItem = MockArtistImageData.generate(200)
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                db.artistImageDao().insertAll(artistItem)
//            }
//        }
//        val histroyItems = MockHistoryData.generate(1000)
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                db.historyAndStatsDao().insertAll(histroyItems)
//            }
//        }
        return db

    }

    @Singleton
    @Provides
    fun provideSongDao(database: MusifyDatabase) = database.songDao()

    @Singleton
    @Provides
    fun providePlaylistDao(database: MusifyDatabase) = database.playlistDao()

    @Singleton
    @Provides
    fun provideFavoriteDao(database: MusifyDatabase) = database.favoriteDao()

    @Singleton
    @Provides
    fun provideHistoryAndStatsDao(database: MusifyDatabase) = database.historyAndStatsDao()

    @Singleton
    @Provides
    fun provideArtistImageDao(database: MusifyDatabase) = database.artistImageDao()
}