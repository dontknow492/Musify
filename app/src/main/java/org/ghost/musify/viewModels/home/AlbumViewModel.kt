package org.ghost.musify.viewModels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {
    val albums = repository.getAllAlbums().cachedIn(viewModelScope)
}