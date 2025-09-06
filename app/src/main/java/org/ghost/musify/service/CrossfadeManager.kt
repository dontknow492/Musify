package org.ghost.musify.service

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrossfadeManager(
    private val player: ExoPlayer,
    private val scope: CoroutineScope
) : Player.Listener {

    private var crossfadeEnabled = false
    private var crossfadeDurationSeconds = 0
    private var fadeOutWatcherJob: Job? = null

    /**
     * Updates the manager with the latest settings from the user.
     */
    fun updateSettings(enabled: Boolean, durationSeconds: Int) {
        crossfadeEnabled = enabled
        crossfadeDurationSeconds = durationSeconds
        Log.d("CrossfadeManager", "Settings updated: enabled=$enabled, duration=${durationSeconds}s")

        // If crossfading was just disabled, ensure volume is reset and watchers are stopped.
        if (!enabled) {
            fadeOutWatcherJob?.cancel()
            scope.launch {
                setPlayerVolume(1.0f)
            }
        } else {
            // If it was just enabled, start a watcher for the current track immediately.
            startFadeOutWatcher()
        }
    }

    /**
     * Starts listening to player events.
     */
    fun start() {
        player.addListener(this)
    }

    /**
     * Stops listening to player events and cleans up.
     */
    fun stop() {
        player.removeListener(this)
        fadeOutWatcherJob?.cancel()
    }

    /**
     * Called when the player moves to a new track. This is our trigger for the FADE IN.
     */
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (crossfadeEnabled && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            Log.d("CrossfadeManager", "Track transition: Starting FADE IN.")
            // Start the fade-in for the new track
            scope.launch {
                rampVolume(0.0f, 1.0f, crossfadeDurationSeconds * 1000L)
            }
        }
        // Always start the watcher for the new track regardless of transition reason.
        startFadeOutWatcher()
    }

    /**
     * Called when playback state changes. We use this to know when a song has started playing.
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            startFadeOutWatcher()
        } else {
            fadeOutWatcherJob?.cancel()
        }
    }

    /**
     * This is the core of the FADE OUT logic. It starts a job that continuously
     * checks the playback position to see if it's time to start fading out.
     */
    private fun startFadeOutWatcher() {
        fadeOutWatcherJob?.cancel() // Cancel any previous watcher
        if (!crossfadeEnabled || player.currentMediaItem == null || !player.isPlaying) {
            return
        }

        fadeOutWatcherJob = scope.launch {
            val fadeOutThresholdMs = crossfadeDurationSeconds * 1000L

            // Loop until it's time to fade out
            while (true) {
                val duration = player.duration
                val position = player.currentPosition
                val remainingTime = duration - position

                // Check if it's time to start the fade and that we're not at the end of the song
                if (duration > 0 && remainingTime <= fadeOutThresholdMs) {
                    Log.d("CrossfadeManager", "Fade threshold reached: Starting FADE OUT.")
                    // Start the fade-out and then stop this watcher job
                    rampVolume(1.0f, 0.0f, remainingTime)
                    break // Exit the while loop
                }
                delay(250) // Check position every 250ms
            }
        }
    }

    /**
     * A coroutine that smoothly changes the player volume over a given duration.
     */
    private suspend fun rampVolume(from: Float, to: Float, durationMs: Long) {
        if (durationMs <= 0) {
            setPlayerVolume(to)
            return
        }
        val steps = 50 // More steps = smoother fade
        val delayPerStep = durationMs / steps
        val volumeStep = (to - from) / steps

        var currentVolume = from
        for (i in 1..steps) {
            currentVolume += volumeStep
            setPlayerVolume(currentVolume.coerceIn(0.0f, 1.0f))
            delay(delayPerStep)
        }
        setPlayerVolume(to) // Ensure final volume is set
    }

    private suspend fun setPlayerVolume(volume: Float) {
        player.volume = volume
    }
}