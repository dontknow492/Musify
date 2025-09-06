package org.ghost.musify.service

//import android.media.AudioAttributes
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ghost.musify.data.PlayerSettings
import org.ghost.musify.data.QueueState
import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.repository.QueueRepository
import org.ghost.musify.repository.SettingsRepository
import org.ghost.musify.utils.getSongUri
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var queueRepository: QueueRepository

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var crossfadeManager: CrossfadeManager


    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    // Create the player and session when the service starts
    override fun onCreate() {

        Log.d("MusicService", "Service Created")
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, exoPlayer).build()

        // --- Integration Start ---
        // 1. Initialize the manager
        crossfadeManager = CrossfadeManager(player = exoPlayer, scope = serviceScope)
        // 2. Start the manager so it listens to player events
        crossfadeManager.start()
        // --- Integration End ---

        // restore queue
        // Restore the queue as soon as the service starts
        restoreQueue()

        observePlayerSettings()
    }

    // This is what the UI connects to
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MusicService", "Session Requested by ${controllerInfo.packageName}")
        return mediaSession
    }

    // Release resources when the service is destroyed
    override fun onDestroy() {
        Log.d("MusicService", "Service Destroyed")

        saveQueue()

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        // Cancel all coroutines started in this scope
        serviceScope.cancel() // <-- Add this line
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private fun observePlayerSettings() {
        data class PlayerConfig(
            val useGapless: Boolean,
            val audioFocus: AudioFocus,
            val crossfadeEnabled: Boolean,
            val crossfadeDurationSec: Int = 500 // Default value
        )

        serviceScope.launch {
            settingsRepository.appSettingsFlow
                .map { appSettings ->
                    // Map to the new, readable data class
                    PlayerConfig(
                        useGapless = appSettings.useGaplessPlayback,
                        audioFocus = appSettings.audioFocusSetting,
                        crossfadeEnabled = appSettings.crossfadeEnabled,
                        crossfadeDurationSec = appSettings.crossfadeDuration
                    )
                }
                .distinctUntilChanged()
                .collect { config -> // The collected item is now a 'config' object
                    Log.d("PlayerService", "Player settings changed. Applying new configuration.")

                    exoPlayer.skipSilenceEnabled = config.useGapless

                    // --- Audio Focus ---
                    val handleAudioFocus = config.audioFocus == AudioFocus.PAUSE_ON_INTERRUPTION
                    val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build()
                    exoPlayer.setAudioAttributes(audioAttributes, handleAudioFocus)

                    // --- Crossfade ---
                    // Apply crossfade logic based on the 'crossfadeEnabled' boolean
                    // --- Integration: Update the manager with the new settings ---
                    // 4. Pass the latest settings to the manager
                    crossfadeManager.updateSettings(
                        enabled = config.crossfadeEnabled,
                        durationSeconds = config.crossfadeDurationSec // Make sure you add this to PlayerConfig
                    )
                }
        }
    }

    private fun restoreQueue() {
        serviceScope.launch {
            // 1. Get the last saved state from the repository.
            val queueState = queueRepository.queueStateFlow.first()
            if (queueState.songIds.isNotEmpty()) {
                Log.d("MusicService", "Restoring queue with ${queueState.songIds.size} songs.")


                // 3. Convert your SongEntity objects into MediaItems for ExoPlayer.
                //    (You'll need a conversion function for this).
                val mediaItems = queueState.songIds.map { songId ->
                    MediaItem.Builder()
                        .setMediaId(songId.toString())
                        .setUri(getSongUri(songId))
                        .build()

                }

                // 4. Set the restored queue on the player.
                exoPlayer.setMediaItems(mediaItems, queueState.currentTrackIndex, queueState.playbackPosition)
                exoPlayer.prepare()
            }
        }
    }


    private fun saveQueue() {
        // Don't save if there's nothing to save.
        if (exoPlayer.mediaItemCount == 0) return

        // 1. Get the list of song IDs from the current player queue.
        //    (Assumes you've set the song ID in the MediaItem's mediaId field).
        val songIds = (0 until exoPlayer.mediaItemCount).map {
            exoPlayer.getMediaItemAt(it).mediaId.toLong()
        }

        // 2. Create a QueueState object with the current player state.
        val currentState = QueueState(
            songIds = songIds,
            currentTrackIndex = exoPlayer.currentMediaItemIndex,
            playbackPosition = exoPlayer.currentPosition
        )

        // 3. Launch a coroutine to save the state via the repository.
        serviceScope.launch {
            queueRepository.saveQueueState(currentState)
            Log.d("MusicService", "Queue state saved.")
        }
    }

}