package org.ghost.musify.utils

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * The final, robust composable for applying a dynamic theme from an image.
 * It uses the MaterialKolor library for simple and accurate theme generation.
 */
@Composable
fun DynamicThemeFromImage(
    imageUrl: Any,
    content: @Composable () -> Unit
) {

    val isDarkTheme = isSystemInDarkTheme()
    val defaultColorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()

    // 1. State to hold our extracted seed color and loading status.
    var seedColor by remember { mutableStateOf(Color.Transparent) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // 2. A LaunchedEffect to load the image and extract the seed color.
    //    This runs in the background and only re-runs if the imageUrl changes.
    LaunchedEffect(imageUrl) {
        isLoading = true
        withContext(Dispatchers.IO) {
            extractSourceColorFromImage(context, imageUrl)?.let { color ->
                seedColor = Color(color)
            }
        }
        isLoading = false
    }


    // 3. Generate the full ColorScheme using the seed color.
    //    `rememberDynamicColorScheme` is the magic function from the MaterialKolor library.
    val colorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDarkTheme,
    )

    // 4. Apply the theme with a smooth Crossfade transition from loading to content.
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(500),
        label = "ThemeCrossfade"
    ) { loading ->
        MaterialTheme(colorScheme = if (loading) defaultColorScheme else colorScheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

/**
 * A helper function to load an image with Coil and extract the
 * most suitable color using the official Material Color Utilities logic.
 *
 * @return The ARGB integer of the extracted color, or null if it fails.
 */
private suspend fun extractSourceColorFromImage(context: Context, imageUrl: Any): Int? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(128, 128))
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

        val bitmap = context.imageLoader.execute(request).image?.toBitmap() ?: return null
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        Score.score(QuantizerCelebi.quantize(pixels, 128)).firstOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}