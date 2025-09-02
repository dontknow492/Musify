package org.ghost.musify.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ghost.musify.entity.relation.HistoryWithSongDetails
import org.ghost.musify.repository.MusicRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HistoryUiState(
    val dateRange: Pair<Long?, Long?> = Pair(null, null),
    val isLoading: Boolean = false, // We can add this if we have other loading tasks
    val error: String? = null,
    val isRefreshing: Boolean = false
)

sealed interface HistoryListItemModel {
    data class DataItem(val details: HistoryWithSongDetails) : HistoryListItemModel
    data class HeaderItem(val date: LocalDate) : HistoryListItemModel
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    // A private MutableStateFlow to hold the full UI state
    private val _uiState = MutableStateFlow(HistoryUiState(isRefreshing = true))

    // A public StateFlow for the UI to observe
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // The playbackHistory flow now reacts to changes in the uiState's dateRange
    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackHistory: Flow<PagingData<HistoryWithSongDetails>> = uiState
        .map { it.dateRange } // We only care about dateRange changes
        .distinctUntilChanged() // Only proceed if the range is actually different
        .flatMapLatest { range ->
            musicRepository.getFullHistoryPlayback(
                minTimestamp = range.first,
                maxTimestamp = range.second
            )
        }.cachedIn(viewModelScope)


    // --- THIS IS THE KEY FUNCTION ---
    /**
     * Called from the UI to update the state based on Paging's LoadState.
     */
    fun onLoadStateUpdate(loadState: CombinedLoadStates) {
        val refreshState = loadState.refresh

        // Check the refresh state to determine loading and error status
        refreshState is LoadState.Loading
        val error = (refreshState as? LoadState.Error)?.error?.localizedMessage

        _uiState.update { currentState ->
            currentState.copy( // isRefreshing is now directly tied to Paging's loading state
                error = error ?: currentState.error // Keep old error if new one is null
            )
        }
    }

    /**
     * Updates the date range in the UI state.
     */
    fun setRange(start: Long?, end: Long?) {
        _uiState.update { currentState ->
            currentState.copy(dateRange = Pair(start, end))
        }
    }

    /**
     * Clears any displayed error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshHistory() {
        setRange(null, null)
    }

    fun removeFromHistory(id: Long) {
        viewModelScope.launch {
            musicRepository.deleteHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            musicRepository.clearHistory()
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}