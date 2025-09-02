package org.ghost.musify.ui.screens.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import org.ghost.musify.viewModels.AddToPlaylistViewModel

@Composable
fun AddToPlaylistDialog(
    modifier: Modifier = Modifier,
    viewModel: AddToPlaylistViewModel,
    onDismissRequest: () -> Unit,
) {
    val title = "Add to playlist"
    val playlists = viewModel.playlistsPagingFlow.collectAsLazyPagingItems()
    var isCreatingNewPlaylist by remember { mutableStateOf(false) }

    var isSongInPlaylistMap = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(playlists.itemCount) {
        // We only want to populate the map once when the list first appears
        if (playlists.itemCount > 0 && isSongInPlaylistMap.isEmpty()) {

            // CORRECTED: Use itemSnapshotList for modern Paging Compose versions
            val initialItems = playlists.itemSnapshotList.items

            // Create a map from the initial, non-null items
            val initialMap = initialItems
                .filterNotNull() // Safely handle any null placeholders
                .associate { playlistForDialog ->
                    playlistForDialog.id to playlistForDialog.isSongInPlaylist
                }

            isSongInPlaylistMap.putAll(initialMap)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }
            HorizontalDivider()
            LazyColumn {
                items(
                    playlists.itemCount,
                    key = { index -> playlists[index]?.id ?: index }
                ) { index ->
                    playlists[index]?.let { playlistEntity ->
                        // The problematic line is now GONE from here!

                        PlaylistDialogItem(
                            name = playlistEntity.name,
                            // Read the current selection state from our local map
                            isSelected = isSongInPlaylistMap[playlistEntity.id] ?: false,
                            // When the user clicks, only update our local map
                            onSelectionChange = { isSelected ->
                                isSongInPlaylistMap[playlistEntity.id] = isSelected
                            }
                        )
                    }
                }
            }
            HorizontalDivider()
            OutlinedButton(
                onClick = { isCreatingNewPlaylist = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new Playlist"
                )
                Text(
                    text = "Create new Playlist",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider()
            DialogButton(
                modifier = Modifier.fillMaxWidth(),
                onAccept = {
                    viewModel.applyPlaylistChanges(isSongInPlaylistMap)
                    onDismissRequest()
                },
                onDismiss = onDismissRequest
            )

        }
    }
    if (isCreatingNewPlaylist) {
        CreatePlaylist(
            onDismissRequest = { isCreatingNewPlaylist = false },
            onAddClick = { title, description, imageUriId, imageUrl ->
//                viewModel.createPlaylist(title, description, imageUriId, imageUrl)

                isCreatingNewPlaylist = false
            }
        )
    }
}

@Composable
private fun PlaylistDialogItem(
    modifier: Modifier = Modifier,
    name: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChange,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun DialogButton(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onDismiss
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        TextButton(
            onClick = onAccept
        ) {
            Text(
                text = "Accept",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}