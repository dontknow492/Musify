package org.ghost.musify.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {
    val artists = repository.getAllAritistImage().cachedIn(viewModelScope)


    fun updateArtistsImage(name: String, imageUrl: String?, imageUriId: Long? = null) {
        viewModelScope.launch {
            repository.updateArtistImage(
                ArtistImageEntity(
                    name = name,
                    imageUrl = imageUrl,
                    imageUriId = imageUriId
                )
            )
        }
    }
}