package org.ghost.musify.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage

@Composable
fun CoverChangeableItem(
    modifier: Modifier = Modifier,
    coverImage: Any?,
    title: String,
) {
    Card(
        modifier = modifier
    ) {
        ConstraintLayout {
            // Create references for the image and text
            val (imageRef, nameRef) = createRefs()
            AsyncImage(
                model = coverImage,
                contentDescription = title,
                modifier = Modifier
                    .constrainAs(imageRef) {
                        // Pin the image to the top of the layout
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .background(Color.LightGray)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // Note: TextAlign.Justify might not look good with a single line.
                // Consider TextAlign.Center or TextAlign.Start.
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(nameRef) {
                    // 1. Position the text below the image
                    top.linkTo(imageRef.bottom, margin = 8.dp) // Add some margin

                    // 2. Align the text's start/end with the image's start/end
                    start.linkTo(imageRef.start)
                    end.linkTo(imageRef.end)

                    // 3. This is the key part: tell the text to fill the defined width
                    width = Dimension.fillToConstraints
                }
            )
        }
    }


}