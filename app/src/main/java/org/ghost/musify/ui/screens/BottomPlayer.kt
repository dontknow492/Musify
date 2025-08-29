package org.ghost.musify.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ghost.musify.R

@Preview(showSystemUi = true)
@Composable
fun BottomPlayer(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {

    Box(
        modifier = modifier.background(containerColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = null,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Song Title",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Artist Name - Album Title",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_pause_24),
                    contentDescription = "pause",
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "next",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        ProgressBar(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            currentPosition = 100,
            totalDuration = 200,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
        )
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    totalDuration: Long,
    brush: Brush,
) {
    Canvas(
        modifier = modifier.width(40.dp)
    ) {
        drawRect(
            brush,
            size = size.copy(width = size.width * currentPosition / totalDuration),
        )

    }
}