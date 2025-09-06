package org.ghost.musify.viewModels.home

//import dagger.hilt.android.internal.Contexts.getApplication
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ghost.musify.enums.SortBy
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    @param: ApplicationContext private val context: Context,
) : ViewModel() {

    val availableSortBy = listOf(
        SortBy.TITLE,
        SortBy.DURATION,
        SortBy.ADDED_AT,
        SortBy.DATE_MODIFIED,
        SortBy.DATE_ADDED,
        SortBy.YEAR
    )

    private var mediaController: MediaController? = null
    val music = repository.filterSongs().cachedIn(viewModelScope)
    val favoriteSongs = repository.filterSongs(favoritesOnly = true).cachedIn(viewModelScope)

    // --- State for the UI ---


}