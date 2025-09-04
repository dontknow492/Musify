package org.ghost.musify.ui.components.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp


@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    totalDuration: Long,
    brush: Brush,
) {
//    LinearProgressIndicator(
//        modifier = modifier,
//        progress = { currentPosition.toFloat() / totalDuration },
//    )
    Canvas(
        modifier = modifier.width(40.dp)
    ) {
        drawRect(
            brush,
            size = size.copy(width = size.width * currentPosition / totalDuration),
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
    val currentProgress = if (!range.contains(currentProgress)) {
        range.start
    } else {
        currentProgress
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

        val fillSize = (currentProgress / range.endInclusive) * width.minus(radius * 2)



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
            size = Size(radius * 2, radius * 2)
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