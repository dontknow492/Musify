package org.ghost.musify.ui.dialog.menu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import org.ghost.musify.R
import org.ghost.musify.ui.dialog.MusicInfoDialog
import org.ghost.musify.viewModels.SongUiState
import org.ghost.musify.viewModels.SongViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SongQueueMenu(
    modifier: Modifier = Modifier,
    songId: Long,
    viewModel: SongViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onAddToPlaylist: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDetailScreenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.loadSongDetail(songId)
    }

    when (val state = uiState) {
        is SongUiState.Error -> {}
        SongUiState.Loading -> {
            CircularProgressIndicator()
        }

        is SongUiState.Success -> {
            val songDetails = state.details
            val isLiked = state.details.liked != null
            Dialog(
                onDismissRequest = onDismissRequest
            ) {
                Card(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = songDetails.songDetail.song.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        IconButton(
                            onClick = viewModel::toggleFavorite
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isLiked) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                    Column(
                        modifier = modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Song info"
                                )
                            },
                            title = "Song info",
                            onClick = { isDetailScreenVisible = true }
                        )
                        HorizontalDivider()
                        MenuButton(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.rounded_queue_music_24),
                                    contentDescription = "Remove from queue"
                                )
                            },
                            title = "Remove from queue",
                            onClick = {}
                        )
                        MenuButton(
                            icon = {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_media_ff),
                                    contentDescription = "Play next"
                                )
                            },
                            title = "Play next",
                            onClick = {}
                        )
                        MenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send to top"
                                )
                            },
                            title = "Send to top",
                            onClick = {
                                onAddToPlaylist(songDetails.songDetail.song.id)
                            }
                        )
                        MenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Add to playlist"
                                )
                            },
                            title = "Add to playlist",
                            onClick = {
                                onAddToPlaylist(songDetails.songDetail.song.id)
                            }
                        )
                        HorizontalDivider()
                        MenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "share"
                                )
                            },
                            title = "Share",
                            onClick = {}
                        )
                        MenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            },
                            title = "Delete",
                            onClick = {}
                        )
                    }
                }
            }
            if (isDetailScreenVisible) {
                MusicInfoDialog(
                    songWithAlbumAndArtist = songDetails.songDetail,
                    onDismissRequest = {
                        isDetailScreenVisible = false
                        onDismissRequest()
                    }
                )
            }
        }
    }


}