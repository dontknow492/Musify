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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.ghost.musify.R
import org.ghost.musify.entity.HistoryEntity
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MediaControllerRepository
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.utils.mapSongToMediaItem
import org.ghost.musify.utils.mapSongsToMediaItems
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
    val currentSong: SongWithAlbumAndArtist? = null,
    val coverImage: ImageRequest,
    val isPlaying: Boolean = false,
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isFavorite: Boolean = false,
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
    private var songIdToSongMap: Map<Long, SongWithAlbumAndArtist> = emptyMap()


    // The single source of truth for the Player UI state.
    private val _uiState = MutableStateFlow(
        PlayerUiState(
            coverImage = ImageRequest.Builder(context)
                .data(null)
                .build()
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()


    // The playback queue, exposed separately for UI components that show the list.
    private val _playbackQueue = MutableStateFlow<List<SongWithAlbumAndArtist>>(emptyList())
    val playbackQueue: StateFlow<List<SongWithAlbumAndArtist>> = _playbackQueue.asStateFlow()
    // endregion

    var playerListener: Player.Listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            logCurrentSongToHistory()
            mediaItem?.mediaId?.toLongOrNull()?.let { newMediaIdAsLong ->
                currentMediaIdForHistory = newMediaIdAsLong
                playbackStartTimeMs = System.currentTimeMillis()
            }


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

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updatePlaybackQueue(timeline)
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


        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _status = MutableStateFlow(PlayerStatus.IDLE)

    private val _repeatMode = MutableStateFlow(PlayerRepeatMode.OFF)

    private val _isShuffleEnabled = MutableStateFlow(false)


    private val _currentSong = MutableStateFlow<SongWithAlbumAndArtist?>(null)
    val currentSong = _currentSong.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = _currentSong.flatMapLatest { song ->
        // 1. Use flatMapLatest to switch to a new flow when the song changes.
        Log.d("PlayerViewModel", "isFavorite: ${song?.song?.title}")
        if (song == null) {
            // 2. If there's no song, the favorite status is false.
            flowOf(false)
        } else {
            // 3. If there is a song, get its favorite status flow from the repository.
            musicRepository.isSongFavorite(song.song.id)
        }
    }.stateIn(
        // 4. Convert the resulting Flow into a StateFlow for the UI to collect.
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    override fun onCleared() {
        stopProgressUpdater()
        mediaController?.removeListener(playerListener)
        super.onCleared()
    }


    private fun updateStateFromController() {
        val controller = mediaController ?: return
        val songId = controller.currentMediaItem?.mediaId?.toLongOrNull()

        val currentSong = songId?.let { songIdToSongMap[it] }
        _currentSong.value = currentSong

        _uiState.update { currentState ->
            currentState.copy(
                isPlaying = controller.isPlaying,
                status = mapPlaybackStateToStatus(controller.playbackState),
                currentSong = currentSong,
                isShuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = controller.repeatMode,
                totalDuration = controller.duration.coerceAtLeast(0L),
                hasNext = controller.hasNextMediaItem(),
                hasPrevious = controller.hasPreviousMediaItem(),
                volume = controller.volume,
                isFavorite = isFavorite.value,
                playbackSpeed = controller.playbackParameters.speed,
                coverImage = ImageRequest.Builder(context)
                    .data(
                        null
                    )
                    .error(R.drawable.music_album_cover)
                    .build()
            )
        }
        Log.d("PlayerViewModel", "State updated: ${_uiState.value}")
    }

    private fun startProgressUpdater() {
        stopProgressUpdater() // Ensure only one updater is running.
        Log.d("PlayerViewModel", "Starting progress updater")
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                playedSongTimeMs += 1000L
                _uiState.update { it.copy(currentPosition = currentPosition) }
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
//        if(_playbackQueue.value.isNotEmpty()) return
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
        songIdToSongMap = songs.associateBy { it.song.id }

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

            // Add the new song to our source-of-truth map
            songIdToSongMap += (song.song.id to song)

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

            // Add all new songs to our source-of-truth map
            songIdToSongMap += songs.associateBy { it.song.id }

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

    /**
     * Removes a song from the playback queue at a specific index.
     */
    fun removeSongFromQueue(index: Int) {
        viewModelScope.launch {
            val controller = waitForController() ?: run {
                Log.e("PlayerViewModel", "Remove command failed: Controller not available.")
                return@launch
            }

            if (index < 0 || index >= controller.mediaItemCount) {
                Log.w("PlayerViewModel", "Cannot remove song: Invalid index $index.")
                return@launch
            }

            // Get the song ID from the media item before removing it
            val mediaId = controller.getMediaItemAt(index).mediaId
            val songId = mediaId.toLongOrNull()

            // Remove the item from the player's queue
            controller.removeMediaItem(index)
            Log.d("PlayerViewModel", "Removed song at index $index from queue.")

            // Also remove it from our local map to maintain consistency
            if (songId != null) {
                songIdToSongMap = songIdToSongMap - songId
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

                // Also remove it from our local map to maintain consistency.
                songIdToSongMap = songIdToSongMap - songId

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

    fun toggleFavorite() {
        Log.d("PlayerViewModel", "toggleFavorite: ${_uiState.value.currentSong}")
        viewModelScope.launch {
            val song = _uiState.value.currentSong ?: return@launch
            val isCurrentlyFavorite = isFavorite.value

            when (isCurrentlyFavorite) {
                true -> musicRepository.removeFromFavorites(song.song.id)
                false -> musicRepository.addToFavorites(song.song.id)
            }
            Log.d("PlayerViewModel", "toggleFavorite: ${isFavorite.value}")
//            isFavorite.value = !isCurrentlyFavorite
        }
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


    private fun updatePlaybackQueue(timeline: Timeline) {
        if (timeline.isEmpty || songIdToSongMap.isEmpty()) {
            _playbackQueue.value = emptyList()
            return
        }

        val newQueue = (0 until timeline.windowCount).mapNotNull { index ->
            val window = timeline.getWindow(index, Timeline.Window())
            val mediaId = window.mediaItem.mediaId.toLongOrNull()
            mediaId?.let { songIdToSongMap[it] }
        }
        _playbackQueue.value = newQueue
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
                val historyEntity = HistoryEntity(
                    songId = mediaId,
                    playedAt = playbackStartTimeMs,
                    durationPlayed = playedSongTimeMs,
                    wasFavorite = isFavorite.value

                )
                viewModelScope.launch {
                    musicRepository.addToHistory(historyEntity)
                    Log.d(
                        "PlayerViewModel",
                        "History: Added '$mediaId' to history. Played for ${playedSongTimeMs / 1000} seconds."
                    )
                }


            }
        }

        // Reset the tracker
        currentMediaIdForHistory = null
    }


}