package org.ghost.musify.viewModels.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ghost.musify.R
import org.ghost.musify.entity.ArtistImageEntity
import org.ghost.musify.repository.MusicRepository
import javax.inject.Inject

data class ArtistWithCover(
    val artist: ArtistImageEntity,
    val cover: ImageRequest
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository,
    @param: ApplicationContext private val context: Context
) : ViewModel() {
    val artists = repository
        .getAllAritistImage()
        .map { pagingData ->
            pagingData.map { artist ->
                ArtistWithCover(
                    artist = artist,
                    cover = ImageRequest.Builder(context)
                        .data(artist.imageUriId ?: artist.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.artist_placeholder)
                        .error(R.drawable.artist_placeholder)
                        .build()
                )
            }
        }
        .cachedIn(viewModelScope)


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