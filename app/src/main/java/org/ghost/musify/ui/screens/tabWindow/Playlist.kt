package org.ghost.musify.ui.screens.tabWindow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.rememberAsyncImagePainter
import org.ghost.musify.ui.screens.components.PlaylistItem
import org.ghost.musify.viewModels.home.PlaylistViewModel

@Composable
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel,
    onPlaylistClick: (Long) -> Unit = {}
) {
    var isCreatingPlaylist by remember { mutableStateOf(false) }

    val playlists = viewModel.playlists.collectAsLazyPagingItems()
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                playlists.itemCount,
                key = { index -> playlists[index]?.id ?: index }
            ) { index ->
                val playlist = playlists[index]
                if (playlist != null) {
                    PlaylistItem(
                        playlist = playlist,
                        onPlaylistClick = onPlaylistClick,
                        coverImage = playlist.playlistImageUriId ?: playlist.playlistImageUrl
                    )
                }

            }

        }

        FloatingActionButton(
            onClick = {
                isCreatingPlaylist = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }
    AnimatedVisibility(isCreatingPlaylist) {
        AddPlaylistDialog(
            onDismissRequest = { isCreatingPlaylist = false }
        ) { title, description, imageUriId, imageUrl ->
            viewModel.createPlaylist(title, description, imageUriId, imageUrl)
        }
    }
//    AddPlaylistDialog()
}

@Composable
fun AddPlaylistDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onAddClick: (String, String, Long?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val imageUriId by remember { mutableLongStateOf(-1) }



    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            StandardAsyncImage(
                model = imageUrl,
                contentDescription = null,
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )



            Text(
                text = "Create Playlist",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = imageUrl,
                onValueChange = {
                    imageUrl = it
                },
                label = { Text("Image URL") },
                trailingIcon = {
                    if (imageUrl.isNotBlank()) {
                        IconButton(
                            onClick = {
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onAddClick(
                            title,
                            description,
                            if (imageUriId == -1L) null else imageUriId,
                            imageUrl.ifBlank { null }
                        )
                        onDismissRequest()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }
    }
}

/**
 * A reusable Composable that loads an image from a URL or other data source,
 * showing a loading indicator and handling error states.
 *
 * @param model The data to load the image from (e.g., URL, URI, File).
 * @param contentDescription The accessibility description for the image.
 * @param modifier The modifier to be applied to the layout.
 * @param contentScale The scaling algorithm to apply to the image.
 */
@Composable
fun StandardAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    // This painter will internally manage the state of the image loading.
    val painter = rememberAsyncImagePainter(model = model)


    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // The main Image composable
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )

        // Handle the different loading states
        when (painter.state.collectAsState().value) {
            is coil3.compose.AsyncImagePainter.State.Loading -> {
                // Show a loading indicator while the image is being fetched.
                CircularProgressIndicator()
            }

            is coil3.compose.AsyncImagePainter.State.Error -> {
                // Show an error message if the image fails to load.
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            // The Success and Empty states are implicitly handled by the Image composable itself.
            // When successful, the image is shown. When empty, nothing is shown.
            else -> {}
        }
    }
}