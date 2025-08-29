package org.ghost.musify.viewModels.home

//import dagger.hilt.android.internal.Contexts.getApplication
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.paging.cachedIn
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.service.MusicService
import org.ghost.musify.utils.getSongUri
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var mediaController: MediaController? = null
    val music = repository.getAllSongs().cachedIn(viewModelScope)
    val favoriteSongs = repository.getFavoriteSongs().cachedIn(viewModelScope)

    // --- State for the UI ---
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong

    init {
        viewModelScope.launch {
            repository.syncMediaStore()
        }
        connectToService()

//        onSongSelected()

    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(
                getApplication(context),
                MusicService::class.java
            )
        )
        val controllerFuture =
            MediaController.Builder(getApplication(context), sessionToken).buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlayingValue: Boolean) {
            _isPlaying.value = isPlayingValue
            Log.d("MusicViewModel", "Player isPlaying changed to: $isPlayingValue")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem == null) return
            val songId = mediaItem.mediaId.toLongOrNull()
            Log.d("MusicViewModel", "MediaItem transitioned to song ID: $songId")
            // Your logic to update _currentSong
            // viewModelScope.launch {
            //     _currentSong.value = repository.getSongById(songId)
            // }
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

    fun onSongSelected() {
        Log.d("MusicViewModel", "onSongSelected: ${getSongUri(36)}")
        val mediaItem = MediaItem.Builder()
            .setUri(getSongUri(36)) // Assumes you have a contentUri on your entity
            .setMediaId("36")
            .build()
        val mediaItem2 = MediaItem.Builder()
            .setUri(getSongUri(37)) // Assumes you have a contentUri on your entity
            .setMediaId("37")
            .build()
        mediaController?.addMediaItems((listOf(mediaItem, mediaItem2)))
//        mediaController?.has
        mediaController?.prepare()
        mediaController?.play()
//        mediaController.
//        mediaController?.duration
    }

}