package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import org.ghost.musify.entity.relation.SongDetailsWithLikeStatus
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

sealed interface SongUiState {
    data object Loading : SongUiState
    data class Success(val details: SongDetailsWithLikeStatus) : SongUiState
    data class Error(val message: String) : SongUiState
}

@HiltViewModel
class SongViewModel @Inject constructor(
    private val repository: MusicRepository,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<SongUiState>(SongUiState.Loading)
    val uiState: StateFlow<SongUiState> = _uiState.asStateFlow()

    private var songDetailJob: Job? = null

    init {
        // 2. Check SavedStateHandle when the ViewModel is created.
        //    If a "songId" exists (from navigation args), load it automatically.
        savedStateHandle.get<Long>("songId")?.let { songId ->
            if (songId != 0L) { // Ensure it's a valid ID
                loadSongDetail(songId)
            }
        }
    }

    fun loadSongDetail(songId: Long) {
        songDetailJob?.cancel()
        songDetailJob = viewModelScope.launch {
            repository.getSongDetailWithLikeStatus(songId)
                .map<SongDetailsWithLikeStatus?, SongUiState> { details ->
                    if (details != null) SongUiState.Success(details)
                    else SongUiState.Error("Song not found.")
                }
                .catch { e -> emit(SongUiState.Error(e.message ?: "An error occurred")) }
                .collect { state -> _uiState.value = state }
        }
    }

    fun toggleFavorite() {
        val songId = savedStateHandle.get<Long>("songId")
        if (songId == null) return
        viewModelScope.launch {
            try {
                // First, check the current status from the single source of truth (the repository)
                val isCurrentlyFavorite =
                    repository.isFavorite(songId) // Assuming it returns a Flow

                // CORRECTED LOGIC:
                if (isCurrentlyFavorite) {
                    // If it IS a favorite, REMOVE it.
                    repository.removeFromFavorites(songId)
                } else {
                    // If it IS NOT a favorite, ADD it.
                    // Note: We create the full entity object that the repository likely expects.
                    repository.addToFavorites(songId)
                }
            } catch (e: Exception) {
                // Handle any database errors, e.g., show a snackbar to the user.
                // _errorState.emit("Failed to update favorite status.")
                Log.e("SongViewModel", "Failed to toggle favorite", e)
            }
        }

    }
}