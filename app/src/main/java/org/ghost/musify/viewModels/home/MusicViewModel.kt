package org.ghost.musify.viewModels.home

//import dagger.hilt.android.internal.Contexts.getApplication
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.paging.cachedIn
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ghost.musify.entity.SongEntity
import org.ghost.musify.enums.SortBy
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.service.MusicService
import org.ghost.musify.utils.getSongUri
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
    val music = repository.getAllSongs().cachedIn(viewModelScope)
    val favoriteSongs = repository.getFavoriteSongs().cachedIn(viewModelScope)

    // --- State for the UI ---


}