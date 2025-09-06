package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.repository.QueueRepository
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class QueueViewModel @Inject constructor(
    queueRepository: QueueRepository,
    musicRepository: MusicRepository
) : ViewModel() {

}