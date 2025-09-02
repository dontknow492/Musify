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
import org.ghost.musify.R
import org.ghost.musify.entity.AlbumEntity
import org.ghost.musify.repository.MusicRepository
import org.ghost.musify.utils.getAlbumArtUri
import javax.inject.Inject

data class AlbumWithCover(
    val album: AlbumEntity,
    val cover: ImageRequest
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: MusicRepository,
    @param: ApplicationContext private val context: Context
) : ViewModel() {
    val albums = repository
        .getAllAlbums()
        .map { pagingData ->
            pagingData.map { album ->
                AlbumWithCover(
                    album = album,
                    cover = ImageRequest.Builder(context)
                        .data(
                            album.albumImageUriId ?: album.albumImageUrl ?: getAlbumArtUri(album.id)
                        )
                        .crossfade(true)
                        .placeholder(R.drawable.music_album_icon_2)
                        .error(R.drawable.music_album_icon_2)
                        .build()
                )
            }
        }
        .cachedIn(viewModelScope)
}