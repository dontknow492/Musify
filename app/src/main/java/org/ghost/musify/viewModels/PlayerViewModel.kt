package org.ghost.musify.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import coil3.request.ImageRequest
import coil3.request.error
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.ghost.musify.R
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MediaControllerRepository
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.utils.mapSongToMediaItem
import org.ghost.musify.utils.mapSongsToMediaItems
//import org.ghost.musify.viewModels.QueueViewModel.Companion.TAG
import javax.inject.Inject

enum class PlayerStatus {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

enum class PlayerRepeatMode(private val value: Int) {
    OFF(Player.REPEAT_MODE_OFF),
    ONE(Player.REPEAT_MODE_ONE),
    ALL(Player.REPEAT_MODE_ALL);

    fun getValue(): Int {
        return value
    }
}

// The single, immutable state object for your player UI
data class PlayerUiState(
    val currentSong: SongDetailsWithLikeStatus? = null,
    val isPlaying: Boolean = false,
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val volume: Float = 1f,
    val playbackSpeed: Float = 1f
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MediaControllerRepository,
    private val musicRepository: MusicRepository,
    @param: ApplicationContext private val context: Context
) : ViewModel() {
    // region State Properties

    companion object{
        
    }

    var playbackSpeeds = listOf(0.25f, 0.5f, 1f, 1.5f, 2f)
    private var mediaController: MediaController? = null
    private var progressUpdateJob: Job? = null
    private val isControllerReady = MutableStateFlow(false) // State to signal readiness

    private var currentMediaIdForHistory: Long? = null
    private var playbackStartTimeMs: Long = 0L
    private var playedSongTimeMs: Long = 0L

    // A threshold to avoid adding songs to history if they were skipped immediately
    private val MIN_PLAYBACK_DURATION_FOR_HISTORY_MS = 10000 // 10 seconds

    // A map for O(1) song lookups, more efficient than iterating the list.

    private val timelineSongIds = MutableStateFlow<List<Long>>(emptyList())


    // The single source of truth for the Player UI state.
    // The other parts of your UI state
    private val _playerState = MutableStateFlow(PlayerUiState()) // Assume PlayerState holds isPlaying, duration, etc.

    private val currentSongId: MutableStateFlow<Long?> = MutableStateFlow(null)

    val currentSong: StateFlow<SongDetailsWithLikeStatus?> = currentSongId
        .onEach { id -> Log.d("FlowDebug", "1. currentSongId emitted: $id") }
        .flatMapLatest { songId ->
            Log.d("PlayerViewModelCurrent", "currentSongId: $songId")
            if (songId == null) {
                flowOf(null) // Emit null if there's no song ID
            } else {
                musicRepository.getSongDetailsWithLikeStatusById(songId) // This returns a Flow
            }
        }.stateIn( // 3. Convert the resulting Flow into a StateFlow for the UI
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after UI stops listening
            initialValue = null // Initial value before the first ID is emitted
        )

    val uiState = combine(
        _playerState,
        currentSong
    ){
        playerState, currentSong ->
        playerState.copy(
            currentSong = currentSong,
            isPlaying = playerState.isPlaying,
            status = playerState.status,
            totalDuration = playerState.totalDuration,
            currentPosition = playerState.currentPosition,
            hasNext = playerState.hasNext,
            hasPrevious = playerState.hasPrevious,
            isShuffleEnabled = playerState.isShuffleEnabled,
            repeatMode = playerState.repeatMode,
            volume = playerState.volume,
            playbackSpeed = playerState.playbackSpeed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState() // Initial empty state
    )




    // endregion

    var playerListener: Player.Listener = object : Player.Listener {



        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            logCurrentSongToHistory()
            mediaItem?.mediaId?.toLongOrNull()?.let { newMediaIdAsLong ->
                currentMediaIdForHistory = newMediaIdAsLong
                playbackStartTimeMs = System.currentTimeMillis()
            }

            Log.d(
                "PlayerViewModelCurrent",
                "Media item transition: ${mediaItem?.mediaId}, reason: $reason, currentSong: ${currentSongId.value}"
            )


            updateStateFromController()

        }


        override fun onRepeatModeChanged(repeatMode: Int) {
            // The repeat mode icon needs to be updated.
            updateStateFromController()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // The shuffle mode icon needs to be updated.
            updateStateFromController()
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // The play/pause button needs to be updated.
            updateStateFromController()
            // Specifically handle the progress updater based on play state.
            if (isPlaying) startProgressUpdater() else stopProgressUpdater()
        }

        override fun onVolumeChanged(volume: Float) {
            updateStateFromController()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateStateFromController()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            updateStateFromController()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            // Check if the reason for the jump was a user seek
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                Log.d("MusicService", "✅ Seek finished! New position is ${newPosition.positionMs} ms")
                // You can update your UI state from here
                updateStateFromController()
                // For example, hide a loading spinner
            }
        }



        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlayerViewModel", "Player Error: ${error.message}", error)
        }


    }

    init {
        viewModelScope.launch {
            // Get the controller from the repository's flow
            mediaController = repository.mediaController.first()
            mediaController?.addListener(playerListener)
            isControllerReady.value = true

            mediaController?.let { controller ->
                currentSongId.value = controller.currentMediaItem?.mediaId?.toLongOrNull()
                updateStateFromController()
                if(controller.isPlaying) startProgressUpdater()
            }
        }
    }





    private fun updateStateFromController() {
        val controller = mediaController ?: return
        val songId = controller.currentMediaItem?.mediaId?.toLongOrNull()

        currentSongId.update {
            songId
        }

        _playerState.update { currentState ->
            currentState.copy(
                isPlaying = controller.isPlaying,
                status = mapPlaybackStateToStatus(controller.playbackState),
                isShuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = controller.repeatMode,
                totalDuration = controller.duration.coerceAtLeast(0L),
                hasNext = controller.hasNextMediaItem(),
                hasPrevious = controller.hasPreviousMediaItem(),
                volume = controller.volume,
                playbackSpeed = controller.playbackParameters.speed,
                currentPosition = controller.currentPosition,
            )
        }
        Log.d("PlayerViewModel", "State updated: ${_playerState.value}")
    }

    private fun startProgressUpdater() {
        stopProgressUpdater() // Ensure only one updater is running.
        Log.d("PlayerViewModel", "Starting progress updater")
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                playedSongTimeMs += 1000L
                _playerState.update { it.copy(currentPosition = currentPosition) }
                delay(1000L)
                Log.d("PlayerViewModel", "Progress updated: $currentPosition")
            }
        }
    }

    private fun stopProgressUpdater() {
        Log.d("PlayerViewModel", "Stopping progress updater")
        progressUpdateJob?.cancel()
    }


    fun playSongFromFilter(
        songId: Long,
        filter: SongFilter,
        isShuffled: Boolean = false,
        repeatMode: Int = Player.REPEAT_MODE_OFF
    ) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Play command failed: Controller not available.")
                return@launch
            }

            val songs = musicRepository.getAllSongsList(filter)
            if (songs.isEmpty()) {
                Log.w("PlayerViewModel", "No songs found for filter: $filter")
                return@launch
            }
            // Update the source-of-truth song map for efficient lookups.
            setSongs(controller, songId, songs, isShuffled, repeatMode)
        }
    }

    fun playSongFromList(
        songId: Long,
        songs: List<SongWithAlbumAndArtist>,
        isShuffled: Boolean = false,
        repeatMode: Int = Player.REPEAT_MODE_OFF
    ) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Play command failed: Controller not available.")
                return@launch
            }

            if (songs.isEmpty()) {
                Log.w("PlayerViewModel", "No songs found for playing")
                return@launch
            }
            // Update the source-of-truth song map for efficient lookups.
            setSongs(controller, songId, songs, isShuffled, repeatMode)

        }
    }

    private fun setSongs(
        controller: MediaController,
        songId: Long,
        songs: List<SongWithAlbumAndArtist>,
        isShuffled: Boolean = false,
        repeatMode: Int = Player.REPEAT_MODE_OFF
    ) {

        val startIndex = songs.indexOfFirst { it.song.id == songId }.takeIf { it != -1 } ?: 0
        val mediaItems = mapSongsToMediaItems(songs)

        controller.shuffleModeEnabled = isShuffled
        controller.repeatMode = repeatMode
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
    }

    fun appendSongToQueue(
        song: SongWithAlbumAndArtist,
        index: Int = -1
    ) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Append command failed: Controller not available.")
                return@launch
            }

            val mediaItem = mapSongToMediaItem(song)

            if (index == -1 || index >= controller.mediaItemCount) {
                // Add to the end of the queue
                controller.addMediaItem(mediaItem)
                Log.d("PlayerViewModel", "Appended song '${song.song.title}' to queue.")
            } else {
                // Add at a specific position
                controller.addMediaItem(index, mediaItem)
                Log.d("PlayerViewModel", "Inserted song '${song.song.title}' at index $index.")
            }
        }
    }

    fun appendSongsToQueue(
        songs: List<SongWithAlbumAndArtist>,
        index: Int = -1
    ) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Append command failed: Controller not available.")
                return@launch
            }

            val mediaItems = mapSongsToMediaItems(songs)

            if (index == -1 || index >= controller.mediaItemCount) {
                // Add to the end of the queue
                controller.addMediaItems(mediaItems)
                Log.d("PlayerViewModel", "Appended ${songs.size} songs to queue.")
            } else {
                // Add at a specific position
                controller.addMediaItems(index, mediaItems)
                Log.d("PlayerViewModel", "Inserted ${songs.size} songs at index $index.")
            }
        }
    }


    fun removeSongFromQueue(songId: Long) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Remove command failed: Controller not available.")
                return@launch
            }

            var itemIndexToRemove = -1
            // Loop through the queue to find the index of the matching media item.
            for (i in 0 until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                if (mediaItem.mediaId == songId.toString()) {
                    itemIndexToRemove = i
                    break
                }
            }

            // If we found the item, remove it by its index.
            if (itemIndexToRemove != -1) {
                // Remove the item from the player's queue.
                controller.removeMediaItem(itemIndexToRemove)
                Log.d(
                    "PlayerViewModel",
                    "Removed song with ID $songId at index $itemIndexToRemove from queue."
                )
            } else {
                Log.w(
                    "PlayerViewModel",
                    "Could not remove song: ID $songId not found in the queue."
                )
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            mediaController?.let{controller ->
                controller.clearMediaItems()
                Log.d("PlayerViewModel", "Queue cleared.")
            }
        }
    }


    fun seekToSong(songId: Long) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Seek command failed: Controller not available.")
                return@launch
            }
            var itemIndexToPlay = -1
            // Loop through the queue to find the index of the matching media item.
            for (i in 0 until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                if (mediaItem.mediaId == songId.toString()) {
                    itemIndexToPlay = i
                    break
                }
            }

            // If we found the item, remove it by its index.
            if (itemIndexToPlay != -1) {
                // Remove the item from the player's queue.
                controller.seekTo(itemIndexToPlay, 0L)

                Log.d(
                    "PlayerViewModel",
                    "Playing song with ID $songId at index $itemIndexToPlay from queue."
                )
            } else {
                Log.w(
                    "PlayerViewModel",
                    "Could not play song: ID $songId not found in the queue."
                )
            }

        }
    }


    fun onPlayPauseClicked() {
        mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun onNextClicked() = mediaController?.seekToNextMediaItem()

    fun onPreviousClicked() = mediaController?.seekToPreviousMediaItem()

    fun onSeekTo(positionMs: Long) = mediaController?.seekTo(positionMs.coerceAtLeast(0L))

    fun toggleShuffleMode() {

        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    /**
     * Cycles through the available repeat modes: OFF -> ONE -> ALL -> OFF.
     */
    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        // Cycle to the next repeat mode
        val nextRepeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF // Fallback
        }
        controller.repeatMode = nextRepeatMode
    }


    /**
     * Sets the player's volume.
     * Note: This is typically used for remote players (like Cast) or in-app volume controls,
     * as most music apps rely on the device's physical volume buttons.
     *
     * @param volume A float between 0.0 (silent) and 1.0 (full volume).
     */
    fun setVolume(volume: Float) {
        // Coerce the value to ensure it's within the valid range [0.0, 1.0].
        val validVolume = volume.coerceIn(0f, 1f)
        mediaController?.volume = validVolume

    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
    }




    private suspend fun waitForController(): MediaController? {
        if (mediaController != null) return mediaController
        return try {
            withTimeout(2000L) { isControllerReady.first { it }; mediaController }
        } catch (e: TimeoutCancellationException) {
            null
        }
    }


    private fun mapPlaybackStateToStatus(state: Int): PlayerStatus = when (state) {
        Player.STATE_IDLE -> PlayerStatus.IDLE
        Player.STATE_BUFFERING -> PlayerStatus.BUFFERING
        Player.STATE_READY -> PlayerStatus.READY
        Player.STATE_ENDED -> PlayerStatus.ENDED
        else -> PlayerStatus.IDLE
    }


    private fun logCurrentSongToHistory() {
        // Check if we were tracking a song
        currentMediaIdForHistory?.let { mediaId ->
            // Only add to history if it was played for a meaningful duration
            if (playedSongTimeMs > MIN_PLAYBACK_DURATION_FOR_HISTORY_MS) {
                viewModelScope.launch {
                    val historyEntity = HistoryEntity(
                        songId = mediaId,
                        playedAt = playbackStartTimeMs,
                        durationPlayed = playedSongTimeMs,
                        wasFavorite = musicRepository.isSongFavorite(mediaId).first()
                        )
                    musicRepository.addToHistory(historyEntity)
                    Log.d(
                        "PlayerViewModel",
                        "History: Added '$mediaId' to history. Played for ${playedSongTimeMs / 1000} seconds."
                    )
                }
            }
            resetPlayedSongTimeMs()
        }

        // Reset the tracker
        currentMediaIdForHistory = null
    }

    private fun resetPlayedSongTimeMs(){
        playedSongTimeMs = 0L
    }

    override fun onCleared() {
        stopProgressUpdater()
        mediaController?.removeListener(playerListener)
        super.onCleared()
    }





}