package org.ghost.musify.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import org.ghost.musify.R

@Composable
fun CoverChangeableItem(
    modifier: Modifier = Modifier,
    coverImage: Any?,
    title: String,
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = coverImage,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
//                    .aspectRatio(1f) // Makes it a square
                    .weight(1f) // Takes up the majority of the space
                    .background(Color.LightGray)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }


}

@Preview(widthDp = 100, heightDp = 100)
@Composable
private fun CoverChangeableItemPreview() {
    val image = ImageRequest.Builder(LocalContext.current)
        .data(null)
        .crossfade(true)
        .placeholder(R.drawable.artist_placeholder)
        .error(R.drawable.artist_placeholder)
        .build()
    CoverChangeableItem(
        coverImage = image,
        title = "Sample Title"
    )
}
