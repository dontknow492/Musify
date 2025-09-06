package org.ghost.musify.data

import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction

data class PlayerSettings(
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 5,
    val useGaplessPlayback: Boolean = true,
    val audioFocusSetting: AudioFocus = AudioFocus.PAUSE_ON_INTERRUPTION,
    val headsetPlugAction: HeadsetPlugAction = HeadsetPlugAction.RESUME_PLAYBACK,
    val bluetoothAutoplay: Boolean = true
)
