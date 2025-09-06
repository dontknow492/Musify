package org.ghost.musify.data

import kotlinx.serialization.Serializable

@Serializable // Mark as serializable if you're using Kotlinx Serialization with DataStore
data class QueueState(
    val songIds: List<Long> = emptyList(),
    val currentTrackIndex: Int = 0,
    val playbackPosition: Long = 0L
)