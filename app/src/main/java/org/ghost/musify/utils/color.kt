package org.ghost.musify.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun DynamicThemeFromImage(
    imageUrl: Any,
    content: @Composable () -> Unit
) {
    val defaultColorScheme = MaterialTheme.colorScheme
    // 1. State to hold the generated ColorScheme
    var colorScheme by remember { mutableStateOf(defaultColorScheme) }
    val context = LocalContext.current

    // 2. Launch an effect that regenerates the theme when the imageUrl changes
    LaunchedEffect(imageUrl) {
        // Create a new ColorScheme in a background thread
        val newColorScheme = withContext(Dispatchers.IO) {
            generateColorSchemeFromImage(
                context,
                defaultColorScheme,
                imageUrl,
            )
        }
        // Update the state with the new scheme
        colorScheme = newColorScheme
    }

    // 3. Apply the dynamic ColorScheme to the content
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private suspend fun generateColorSchemeFromImage(
    context: android.content.Context,
    defaultColorScheme: ColorScheme,
    imageUrl: Any
): ColorScheme {
    try {
        // 1. Create an ImageRequest with Coil
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(128, 128)) // Scale down the image to improve performance
            .allowHardware(false) // Important for converting to a bitmap
            .build()

        // 2. Get the Drawable result
        val result = context.imageLoader.execute(request).image

        if (result != null) {
            // 3. Convert the Drawable to a Bitmap
            val bitmap = result.toBitmap()

            // 4. Generate the Palette from the bitmap
            val palette = Palette.from(bitmap).generate()

            // 5. Extract colors, providing fallbacks
            val vibrant =
                palette.vibrantSwatch?.rgb?.let { Color(it) } ?: defaultColorScheme.primary
            val vibrantContainer = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
                ?: defaultColorScheme.primaryContainer
            val muted = palette.mutedSwatch?.rgb?.let { Color(it) } ?: defaultColorScheme.secondary
            val mutedContainer = palette.lightMutedSwatch?.rgb?.let { Color(it) }
                ?: defaultColorScheme.secondaryContainer
            val dominant =
                palette.dominantSwatch?.rgb?.let { Color(it) } ?: defaultColorScheme.surface

            // 6. Create and return a new ColorScheme
            return darkColorScheme(
                primary = vibrant,
                primaryContainer = vibrantContainer,
                secondary = muted,
                secondaryContainer = mutedContainer,
                surface = dominant,
                onSurface = palette.dominantSwatch?.titleTextColor?.let { Color(it) }
                    ?: defaultColorScheme.onSurface
                // You can map other colors as well
            )
        }
    } catch (e: Exception) {
        // Handle exceptions (e.g., network error, invalid image)
        e.printStackTrace()
    }

    // Return the default scheme if anything goes wrong
    return defaultColorScheme
}