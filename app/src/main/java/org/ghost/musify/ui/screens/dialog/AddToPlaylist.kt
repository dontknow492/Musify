package org.ghost.musify.ui.screens.dialog

import android.widget.CheckBox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import org.ghost.musify.viewModels.home.PlaylistViewModel

@Composable
fun AddToPlaylistDialog(
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel,
    onDismissRequest: () -> Unit,
    title: String?,
) {
    val playlists = viewModel.playlists.collectAsLazyPagingItems()
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            title?.let{
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HorizontalDivider()
            LazyColumn {
                items(
                    playlists.itemCount,
                    key = { index -> playlists[index]?.id ?: index }) { index ->
                    playlists.get(index)?.let { playlistEntity ->
                        PlaylistDialogItem(
                            name = playlistEntity.name,
                            isSelected = false,
                            onSelectionChange = {}
                        )
                    }
                }
            }
            HorizontalDivider()
            OutlinedButton(
                onClick = {}
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
                onAccept = onDismissRequest,
                onDismiss = onDismissRequest
            )

        }
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
        modifier = modifier
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
                color = MaterialTheme.colorScheme.surfaceVariant
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