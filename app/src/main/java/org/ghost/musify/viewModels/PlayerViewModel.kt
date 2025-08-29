package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

// The single, immutable state object for your player UI
data class PlayerUiState(
    // Song details
    val currentSong: SongWithAlbumAndArtist? = null,
    val currentFilter: SongFilter? = null,

    // Player status
    val isPlaying: Boolean = false,
    val status: PlayerStatus = PlayerStatus.IDLE,

    // Playback progress
    val currentPosition: Long = 0L, // in milliseconds
    val totalDuration: Long = 0L,   // in milliseconds

    // Controls state (optional but useful)
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF // REPEAT_MODE_OFF, REPEAT_MODE_ONE, or REPEAT_MODE_ALL
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    // Hilt finds the provider in your MediaModule and injects the connected MediaController.
//    private val controllerFuture: ListenableFuture<MediaController>,
    private val repository: MediaControllerRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {
    private var mediaController: MediaController? = null

    init {
        viewModelScope.launch {
            // Get the controller from the repository's flow
            mediaController = repository.mediaController.first()
            mediaController?.addListener(playerListener)
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<SongWithAlbumAndArtist?>(null)
    val currentSong = _currentSong.asStateFlow()

    var playerListener: Player.Listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlayingValue: Boolean) {
//            _isPlaying.value = isPlayingValue
            Log.d("MusicViewModel", "Player isPlaying changed to: $isPlayingValue")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem == null) return
            val songId = mediaItem.mediaId.toLongOrNull()
            Log.d("MusicViewModel", "MediaItem transitioned to song ID: $songId")
            _currentSong.update {
                if (songId != null) {
                    getPlaylistSong(songId)
                } else {
                    null
                }
            }
            Log.d("MusicViewModel", "Current song updated: ${_currentSong.value?.song?.title}")
        }

        // --- ADD THESE NEW OVERRIDES FOR DEBUGGING ---

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN"
            }
            Log.d("MusicViewModel", "Playback state changed to: $stateString")
        }

        override fun onPlayerError(error: PlaybackException) {
            // This is the most important one!
            Log.e("MusicViewModel", "Player Error: ${error.message}", error)
        }
    }

    private val _uiState = MutableStateFlow<PlayerUiState>(
        PlayerUiState(null, null)
    )

    private val _playlist = MutableStateFlow<List<SongWithAlbumAndArtist>>(emptyList())


    //
    fun onPlayPauseClicked() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    //
//    // Best practice: release the controller when the ViewModel is no longer needed.
    override fun onCleared() {
        // Clean up the listener, but DO NOT release the singleton controller
        mediaController?.removeListener(playerListener)
        super.onCleared()
    }

    fun onSongSelected(songId: Long, filter: SongFilter) {
        Log.d("PlayerViewModel", "onSongSelected: $songId, filters: $filter")
        mediaController?.clearMediaItems()
        viewModelScope.launch {
            val songs = musicRepository.getAllSongsList(filter)
            _playlist.update {
                songs
            }
            Log.d("PlayerViewModel", "onSongSelected: Songs: ${songs.size}")
            val mediaItems = mapSongsToMediaItems(songs)
            Log.d("PlayerViewModel", "onSongSelected: MediaItems: ${mediaItems.size}")

            mediaController?.setMediaItems(mediaItems, 2, 0L)
            mediaController?.shuffleModeEnabled = true
            mediaController?.repeatMode = Player.REPEAT_MODE_ALL
            mediaController?.currentTimeline
//            mediaController?.mediaItem
            mediaController?.prepare()
            mediaController?.play()

        }
    }

    private fun getPlaylistSong(songId: Long): SongWithAlbumAndArtist? {
        _playlist.value.forEach { songWithAlbumAndArtist ->
            if (songWithAlbumAndArtist.song.id == songId) {
                return songWithAlbumAndArtist
            }
        }
        return null
    }


}