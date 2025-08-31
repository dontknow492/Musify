package org.ghost.musify.ui.screens.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import org.ghost.musify.R
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.utils.formatFileSize
import org.ghost.musify.utils.toFormattedDate
import org.ghost.musify.utils.toFormattedDuration
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MusicInfoDialog(
    modifier: Modifier = Modifier,
    songWithAlbumAndArtist: SongWithAlbumAndArtist,
    onDismissRequest: () -> Unit,
) {
    // State to hold the fetched song details. Initially null.
    val song = songWithAlbumAndArtist.song
    val artist = songWithAlbumAndArtist.artist
    val album = songWithAlbumAndArtist.album

    // A LaunchedEffect will run the query in the background when the songId changes.
    // This avoids blocking the UI thread.


    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {

            // Display the song information
            Box {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Album Art
                    AsyncImage(
                        model = null,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        // Fallback placeholder if art is not found
                        error = painterResource(id = R.drawable.musify)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Artist
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )



                    Spacer(modifier = Modifier.height(20.dp))


                    // Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val locationPath = File(song.filePath).parent?.let { path ->
                            // 2. Replace the storage prefix and format the path as a breadcrumb.
                            path.replace("/storage/emulated/0", "Internal storage")
                                .removePrefix("/") // Removes a leading slash for cleaner output
                                .replace("/", " > ")
                        } ?: "N/A" // This will now work correctly if .parent is null

                        val fileName = File(song.filePath).name



                        InfoColumn(
                            label = "Location",
                            value = locationPath,
                            isPath = true
                        )

                        HorizontalDivider()
                        InfoColumn(label = "File name", value = fileName, isPath = true)
                        InfoColumn(label = "Artist", value = artist.name)
                        InfoColumn(label = "Album", value = album.title)
                        InfoColumn(label = "Album-Artist", value = album.artist)
                        InfoColumn(label = "Composer", value = song.composer ?: "Unknown")
                        InfoColumn(label = "File Size", value = formatFileSize(song.size))
                        InfoColumn(label = "Format", value = song.mimeType.toString())
                        HorizontalDivider()
                        InfoColumn(label = "Duration", value = song.duration.toFormattedDuration())
                        InfoColumn(label = "Bitrate", value = song.bitrate.toString())
                        InfoColumn(label = "Year", value = song.year.toString())
                        InfoColumn(label = "Track number", value = song.trackNumber.toString())
                        HorizontalDivider()
                        InfoColumn(
                            label = "Date Added",
                            value = song.dateAdded.times(1000).toFormattedDate()
                        )
                        InfoColumn(
                            label = "Date Modified",
                            value = song.dateModified.times(1000).toFormattedDate()
                        )
                    }

                    //                    Spacer(modifier = Modifier.height(24.dp))


                    // Close Button
                }
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)

                ) {
                    Text("CLOSE")
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "close"
                    )
                }
            }
        }
    }
}

// A helper composable to display a row of information
@Composable
private fun InfoColumn(label: String, value: String, isPath: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
//        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )

        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (isPath) 10 else 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}