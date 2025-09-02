package org.ghost.musify.database.mock

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ghost.musify.dao.HistoryAndStatsDao
import org.ghost.musify.entity.HistoryEntity
import java.util.concurrent.TimeUnit

object MockHistoryData {
    // The foreign keys you provided for the songs.
    private val songIds = listOf(36L, 37L, 41L, 42L, 43L, 50L, 51L)

    /**
     * Generates a list of mock HistoryEntity objects for populating the database.
     *
     * @param count The number of history entries to create.
     * @return A list of HistoryEntity objects.
     */
    fun generate(count: Int = 50): List<HistoryEntity> {
        val historyList = mutableListOf<HistoryEntity>()
        var lastPlayedTime = System.currentTimeMillis()

        for (i in 1..count) {
            // Pick a random song ID from the provided list
            val randomSongId = songIds.random()

            // Simulate that this song was played sometime before the last one
            val timeAgo =
                TimeUnit.MINUTES.toMillis((5..240).random().toLong()) // 5 mins to 4 hours ago
            lastPlayedTime -= timeAgo

            // The song was played for a duration between 20 seconds and 5 minutes
            val durationPlayed = TimeUnit.SECONDS.toMillis((20..300).random().toLong())

            // Randomly decide if the song was a favorite at the time of play
            val wasFavorite = Math.random() < 0.3 // 30% chance of being a favorite

            historyList.add(
                HistoryEntity(
                    songId = randomSongId,
                    playedAt = lastPlayedTime,
                    durationPlayed = durationPlayed,
                    wasFavorite = wasFavorite
                )
            )
        }
        return historyList
    }
}

private class PrepopulateDatabaseCallback(
    private val historyDaoProvider: () -> HistoryAndStatsDao
) : RoomDatabase.Callback() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            populateHistory()
        }
    }

    private suspend fun populateHistory() {
        val historyDao = historyDaoProvider()

        // Generate 50 mock history entries
        val mockHistory = MockHistoryData.generate(50)

        // Insert the generated data into the database
        historyDao.insertAll(mockHistory)
    }
}