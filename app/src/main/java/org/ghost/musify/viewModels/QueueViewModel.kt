package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ghost.musify.repository.MediaControllerRepository
import org.ghost.musify.repository.MusicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import javax.inject.Inject

// This data class is perfect as is.
data class QueueUiState(
    val queue: List<SongDetailsWithLikeStatus> = emptyList(),
    val currentSong: SongDetailsWithLikeStatus? = null,
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val mediaControllerRepository: MediaControllerRepository,
) : ViewModel() {

    private var mediaController: MediaController? = null

    // STEP 1: Create private flows to hold the raw IDs from the controller.
    private val timelineSongIds = MutableStateFlow<List<Long>>(emptyList())
    private val currentSongId = MutableStateFlow<Long?>(null)

    // STEP 2: Use `flatMapLatest` to reactively fetch data from the repository.
    // When a new list of IDs is emitted, the old database query is automatically cancelled
    // and a new one is started.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val queueFlow: Flow<List<SongDetailsWithLikeStatus>> = timelineSongIds.flatMapLatest { ids ->
        if (ids.isEmpty()) {
            flowOf(emptyList()) // Return a flow with an empty list if IDs are empty
        } else {
            // This is now a call to your reactive repository function
            val songsFlow = musicRepository.getSongsDetailsWithLikeStatusByIds(ids)
            // Re-order the results from the DB to match the timeline's order
            songsFlow.map { songs ->
                ids.mapNotNull { id -> songs.find { it.songDetail.song.id == id } }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentSongFlow: Flow<SongDetailsWithLikeStatus?> = currentSongId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            musicRepository.getSongDetailsWithLikeStatusById(id)
        }
    }

    // STEP 3: Combine the final data flows into a single UI state object.
    // This will emit a new QueueUiState whenever the queue OR the current song changes.
    val queueState: StateFlow<QueueUiState> = combine(
        queueFlow,
        currentSongFlow
    ) { queue, currentSong ->
        QueueUiState(queue = queue, currentSong = currentSong)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QueueUiState()
    )

    // The listener's only job now is to update the simple ID flows.
    private val playerListener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            Log.d(TAG, "onTimelineChanged - New timeline received")
            timelineSongIds.value = parseTimelineToSongIds(timeline)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition - New item: ${mediaItem?.mediaMetadata?.title}")
            currentSongId.value = mediaItem?.mediaId?.toLongOrNull()
        }
    }

    init {
        mediaControllerRepository.mediaController
            .onEach { controller ->
                Log.d(TAG, "New MediaController instance received.")
                this.mediaController?.removeListener(playerListener)
                this.mediaController = controller
                controller.addListener(playerListener)

                // On connection, immediately update the ID flows with the current state.
                // The reactive streams will handle fetching and updating the UI.
                timelineSongIds.value = parseTimelineToSongIds(controller.currentTimeline)
                currentSongId.value = controller.currentMediaItem?.mediaId?.toLongOrNull()
            }
            .launchIn(viewModelScope)
    }

    private fun parseTimelineToSongIds(timeline: Timeline): List<Long> {
        return List(timeline.windowCount) { i ->
            val window = timeline.getWindow(i, Timeline.Window())
            window.mediaItem.mediaId.toLongOrNull()
        }.filterNotNull()
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared - Removing player listener.")
        mediaController?.removeListener(playerListener)
        super.onCleared()
    }

    companion object {
        private const val TAG = "QueueViewModel_DEBUG"
    }
}