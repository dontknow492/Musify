package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ghost.musify.data.OnboardingManager
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject


sealed class MainUiState {
    data object Loading : MainUiState()
    data object NavigateToOnboarding : MainUiState()
    data object NavigateToMainApp : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager,
    private val musicRepository: MusicRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // This block runs when the ViewModel is created
        viewModelScope.launch {
            // 1. Run multiple tasks in parallel for efficiency
            val tasks = listOf(
                async { musicRepository.syncMediaStore() },
                // Add more async tasks here
            )
            tasks.awaitAll() // Wait for all parallel tasks to complete

            // 2. Check the onboarding status (sequential task)
            val isOnboardingCompleted = onboardingManager.isOnboardingCompleted.first()

            // 3. Update the state to trigger navigation
            if (isOnboardingCompleted) {
                _uiState.value = MainUiState.NavigateToMainApp
            } else {
                _uiState.value = MainUiState.NavigateToOnboarding
            }
        }
    }
}