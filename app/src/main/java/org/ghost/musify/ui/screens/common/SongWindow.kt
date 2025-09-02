package org.ghost.musify.ui.screens.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.ghost.musify.R
import org.ghost.musify.ui.screens.components.SearchableAppBar
import org.ghost.musify.ui.screens.models.SongWindowData


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SongList(
    modifier: Modifier = Modifier,
    data: SongWindowData,
    onPlayClick: () -> Unit,
    onCardClick: (Long) -> Unit,
    onFilterClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val data = data.copy(
        title = if (data.title.isEmpty() || data.title == "<unknown>") "Unknown" else data.title,
        body = if (data.body.isEmpty() || data.body == "<unknown>") "Unknown" else data.body,
    )
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPlayClick,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            SearchableAppBar(
                onBackClick = onBackClick
            )
            SongsScreen(
                modifier = Modifier.padding(12.dp),
                songs = data.songs,
                item = {
                    Heading(
                        modifier = Modifier,
                        title = data.title,
                        body = data.body,
                        image = data.image,
                        count = data.count,
                        type = data.type,
                        onPlayClick = onPlayClick,
                        onShuffleClick = onShuffleClick
                    )
                },
                onSongClick = onCardClick
            )
        }
    }

}


@Composable
fun Heading(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    image: ImageRequest,
    count: Int = 0,
    type: String = "musics",
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
//            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
    ) {
        val width = maxWidth
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (width > 600.dp) {
                HeadingLandscape(
                    title = title,
                    body = body,
                    image = image,
                    count = count,
                    type = type
                )
            } else {
                HeadingPortrait(
                    title = title,
                    body = body,
                    image = image,
                    count = count,
                    type = type
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Play", style = MaterialTheme.typography.bodyLarge)
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                }
                OutlinedButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Shuffle", style = MaterialTheme.typography.bodyLarge)
                    Icon(
                        painter = painterResource(R.drawable.rounded_shuffle_24),
                        contentDescription = null,
                        modifier = Modifier
                    )
                }

            }
        }
    }
}

@Composable
fun HeadingPortrait(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    image: ImageRequest,
    count: Int = 0,
    type: String = "musics",
) {
    Column(
        modifier = modifier
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large),
            contentScale = ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$count $type",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


@Composable
fun HeadingLandscape(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    image: ImageRequest,
    count: Int = 0,
    type: String = "musics",
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(200.dp)
                .clip(MaterialTheme.shapes.large),
            contentScale = ContentScale.FillWidth
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$count $type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}