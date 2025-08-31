package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    val playbackHistory = musicRepository.getFullHistoryPlayback()
}