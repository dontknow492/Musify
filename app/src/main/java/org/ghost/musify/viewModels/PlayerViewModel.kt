package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import dagger.hilt.android.lifecycle.HiltViewModel
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
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MediaControllerRepository
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.ui.screens.models.SongFilter
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
    val isPlaying: Boolean = false,
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isFavorite: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val volume: Float = 1f
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MediaControllerRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {
    // region State Properties
    private var mediaController: MediaController? = null
    private var progressUpdateJob: Job? = null
    private val isControllerReady = MutableStateFlow(false) // State to signal readiness

    // A map for O(1) song lookups, more efficient than iterating the list.
    private var songIdToSongMap: Map<Long, SongWithAlbumAndArtist> = emptyMap()

    // The single source of truth for the Player UI state.
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()


    // The playback queue, exposed separately for UI components that show the list.
    private val _playbackQueue = MutableStateFlow<List<SongWithAlbumAndArtist>>(emptyList())
    val playbackQueue: StateFlow<List<SongWithAlbumAndArtist>> = _playbackQueue.asStateFlow()
    // endregion

    var playerListener: Player.Listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // The song has changed.
//            fetchFavorite()
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
            if(isPlaying) startProgressUpdater() else stopProgressUpdater()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updatePlaybackQueue(timeline)
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            super.onAudioAttributesChanged(audioAttributes)
        }

        override fun onVolumeChanged(volume: Float) {
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
    val isFavorite: StateFlow<Boolean> = currentSong.flatMapLatest { song ->
        // 1. Use flatMapLatest to switch to a new flow when the song changes.

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

        _uiState.update { currentState ->
            currentState.copy(
                isPlaying = controller.isPlaying,
                status = mapPlaybackStateToStatus(controller.playbackState),
                currentSong = songId?.let { songIdToSongMap[it] },
                isShuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = controller.repeatMode,
                totalDuration = controller.duration.coerceAtLeast(0L),
                hasNext = controller.hasNextMediaItem(),
                hasPrevious = controller.hasPreviousMediaItem(),
                volume = controller.volume,
                isFavorite = isFavorite.value
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
    ){
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
            songIdToSongMap = songs.associateBy { it.song.id }

            val startIndex = songs.indexOfFirst { it.song.id == songId }.takeIf { it != -1 } ?: 0
            val mediaItems = mapSongsToMediaItems(songs)

            controller.shuffleModeEnabled = isShuffled
            controller.repeatMode = repeatMode
            controller.setMediaItems(mediaItems, startIndex, 0L)
            controller.prepare()
//            controller.play()
//            startProgressUpdater()
        }
    }

    fun onPlayPauseClicked() {
        mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun onNextClicked() = mediaController?.seekToNextMediaItem()

    fun onPreviousClicked() = mediaController?.seekToPreviousMediaItem()

    fun onSeekTo(positionMs: Long) = mediaController?.seekTo(positionMs.coerceAtLeast(0L))

    fun toggleShuffleMode() {

        mediaController?.let {controller  ->
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

           when(isCurrentlyFavorite){
               true -> musicRepository.removeFromFavorites(song.song.id)
               false -> musicRepository.addToFavorites(song.song.id)
           }
            updateStateFromController()
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


}