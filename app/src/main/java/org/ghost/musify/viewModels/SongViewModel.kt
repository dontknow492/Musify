package org.ghost.musify.viewModels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SongUiState> =
        // Get the songId from navigation arguments
        savedStateHandle.getStateFlow<Long?>("songId", null)
            .transformLatest { songId ->
                if (songId == null) {
                    emit(SongUiState.Error("Song ID not provided."))
                    return@transformLatest
                }

                // 5. Map the repository Flow to the UI State
                repository.getSongDetailWithLikeStatus(songId)
                    .map { songDetails ->
                        if (songDetails != null) {
                            SongUiState.Success(songDetails)
                        } else {
                            SongUiState.Error("Song not found.")
                        }
                    }
                    .catch { throwable ->
                        // 6. Catch any errors from the repository
                        emit(SongUiState.Error("Error: ${throwable.message}"))
                    }
                    .collect { emit(it) } // Emit the mapped state
            }
            .stateIn(
                // 7. Convert the cold Flow to a hot StateFlow
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SongUiState.Loading // 8. Set an initial loading state
            )

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