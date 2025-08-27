package org.ghost.musify.service

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Create the player and session when the service starts
    override fun onCreate() {
        Log.d("MusicService", "Service Created")
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // This is what the UI connects to
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MusicService", "Session Requested by ${controllerInfo.packageName}")
        return mediaSession
    }

    // Release resources when the service is destroyed
    override fun onDestroy() {
        Log.d("MusicService", "Service Destroyed")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

}