package org.ghost.musify.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import org.ghost.musify.R
import org.ghost.musify.utils.toFormattedDuration
import kotlin.math.absoluteValue


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            ).padding(innerPadding)
        ){
            PlayerScreenTopBar(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 12.dp)
            )
            PlayerScreenItem(
                modifier = Modifier
            )
        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showSystemUi = true)
fun PlayerScreenItem(modifier: Modifier = Modifier) {
    val iconSizeSmall = 20.dp


    var currentProgress by remember { mutableLongStateOf(0) }


    BoxWithConstraints(
        modifier = modifier
    ) {
        val width = maxWidth
        val height = maxHeight
        if(width - height < 200.dp){
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(null)
                        .error(R.drawable.artist_placeholder)
                        .build(),
                    contentDescription = "title",
                    modifier = Modifier
                        .weight(1.75f)
                        .padding(20.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.FillHeight,
                )
                PlayerInfo(
                    modifier = Modifier.weight(1f)
                )
            }

        }
        else{
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(null)
                        .error(R.drawable.artist_placeholder)
                        .build(),
                    contentDescription = "title",
                    modifier = Modifier
                        .size(350.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.FillWidth,
                )
                PlayerInfo(
                    modifier = Modifier.fillMaxHeight(0.8f)
                )
            }
        }



    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerInfo(modifier: Modifier = Modifier) {
    val iconSizeLarge = 60.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,

    ) {
//        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Title of music",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Artist A, Artist B, Artist C",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = "Music Album",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
//        Spacer(modifier = Modifier.height(16.dp))

        Row{

            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "info"
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "add to playlist"
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_queue_music_24),
                    contentDescription = "music queue"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_repeat_24),
                    contentDescription = "repeat mode"
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_shuffle_24),
                    contentDescription = "shuffle"
                )
            }

        }

//        Spacer(modifier = Modifier.height(32.dp))

        MusicProgressBar(
            modifier = Modifier.padding(horizontal = 12.dp)
        )

//        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_volume_up_24),
                    contentDescription = "volume"
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_skip_previous_24),
                    contentDescription = "previous",
                    modifier = Modifier.size(iconSizeLarge)
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_pause_24),
                    contentDescription = "play/pause",
                    modifier = Modifier.size(iconSizeLarge)
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_skip_next_24),
                    contentDescription = "next",
                    modifier = Modifier.size(iconSizeLarge)
                )
            }
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_bar_chart_24),
                    contentDescription = "modifier"
                )
            }
        }
//        Spacer(modifier = Modifier.height(16.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MusicProgressBar(
    modifier: Modifier = Modifier,
    currentDurationMs: Long = 0L,
    totalDurationMs: Long = 0L,
    onValueChange: (Float) -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(
            text = currentDurationMs.toFormattedDuration(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        CircularProgressBar(
            modifier = Modifier
                .height(20.dp)
                .weight(6f),
            currentProgress = currentDurationMs.toFloat(),
            range = 0f..totalDurationMs.toFloat(),
            onValueChange = onValueChange
        )
        Text(
            text = totalDurationMs.toFormattedDuration(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun CircularProgressBar(
    modifier: Modifier,
    currentProgress: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = LocalContentColor.current,


) {
    check(range.contains(currentProgress)){
        "currentProgress value ($currentProgress) is outside of the specified range $range"
    }
    Canvas(
        modifier = modifier
            .pointerInput(range) { // The key for handling user input
                detectTapGestures { offset ->
                    // Logic to handle taps
                    val barWidth = size.width - size.height // The tappable width of the bar
                    val radius = size.height / 2f

                    // Calculate the progress based on the tap position
                    val position = (offset.x - radius).coerceIn(0f, barWidth.toFloat())
                    val progress =
                        (position / barWidth) * (range.endInclusive - range.start) + range.start
                    onValueChange(progress)
                }
            }
            .pointerInput(range) {
                detectDragGestures { change, _ ->
                    // Logic to handle drags
                    val barWidth = size.width - size.height // The draggable width of the bar
                    val radius = size.height / 2f

                    // Calculate the progress based on the drag position
                    val position = (change.position.x - radius).coerceIn(0f, barWidth.toFloat())
                    val progress =
                        (position / barWidth) * (range.endInclusive - range.start) + range.start
                    onValueChange(progress)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barHeight = height.div(3)
        val radius = height.div(2)

        val fillSize = (currentProgress / range.endInclusive) * width.minus(radius*2)



        drawRoundRect(
            color = color,
            topLeft = Offset(radius, height.minus(barHeight).div(2)),
            size = Size(width - radius * 2, barHeight),
            cornerRadius = CornerRadius(height, height)
        )


        drawRoundRect(
            color = indicatorColor,
            topLeft = Offset(radius, barHeight),
            size = Size(fillSize, barHeight),
            cornerRadius = CornerRadius(height, height)
        )


        drawArc(
            color = indicatorColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(fillSize, 0f),
            size = Size(radius*2, radius*2)
        )
        drawArc(
            color = contentColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                fillSize + radius.div(2),
                radius.div(2)
            ),
            size = Size(radius, radius)
        )


    }

}

@Composable
fun PlayerScreenTopBar(modifier: Modifier = Modifier) {
    val iconSize = 60.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        IconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "back",
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(
            onClick = {}
        ){
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "menu",
                modifier = Modifier.size(iconSize)
            )

        }
    }
}
