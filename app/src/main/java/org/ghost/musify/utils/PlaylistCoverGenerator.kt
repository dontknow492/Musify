package org.ghost.musify.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import org.ghost.musify.entity.relation.PlaylistWithSongs
import java.io.File
import java.io.FileOutputStream

object PlaylistCoverGenerator {

    /**
     * Creates a 2x2 collage from a list of bitmaps.
     */
    private fun createCollage(bitmaps: List<Bitmap>, width: Int, height: Int): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val halfW = width / 2
        val halfH = height / 2

        // Draw each bitmap in its quadrant
        if (bitmaps.isNotEmpty()) {
            canvas.drawBitmap(bitmaps[0], null, Rect(0, 0, halfW, halfH), null)
        }
        if (bitmaps.size > 1) {
            canvas.drawBitmap(bitmaps[1], null, Rect(halfW, 0, width, halfH), null)
        }
        if (bitmaps.size > 2) {
            canvas.drawBitmap(bitmaps[2], null, Rect(0, halfH, halfW, height), null)
        }
        if (bitmaps.size > 3) {
            canvas.drawBitmap(bitmaps[3], null, Rect(halfW, halfH, width, height), null)
        }
        return result
    }

    /**
     * Saves a bitmap to the app's internal cache directory and returns its URI.
     */
    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, playlistId: Long): Uri {
        val imageDir = File(context.cacheDir, "playlist_covers")
        if (!imageDir.exists()) {
            imageDir.mkdir()
        }
        val imageFile = File(imageDir, "playlist_${playlistId}_cover.png")

        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        // Use a FileProvider to get a content URI
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Make sure this matches your manifest
            imageFile
        )
    }

    /**
     * Main function to generate, save, and return the URI for a playlist cover.
     */
    suspend fun generate(
        context: Context,
        playlistWithSongs: PlaylistWithSongs
    ): String? {
        val playlist = playlistWithSongs.playlist
        // Only generate if no image is set and there are songs
        if (playlist.playlistImageUrl != null || playlist.playlistImageUriId != null || playlistWithSongs.songs.isEmpty()) {
            return null
        }

        val imageLoader = ImageLoader(context)
        val songCoversToLoad = playlistWithSongs.songs.take(4)

        // Load bitmaps asynchronously using Coil
        val bitmaps = songCoversToLoad.mapNotNull { song ->
            val request = ImageRequest.Builder(context)
                .data(getAlbumArtUri(song.id))
                .allowHardware(false) // Required for drawing to a canvas
                .build()
            imageLoader.execute(request).image?.toBitmap()
        }

        if (bitmaps.isEmpty()) return null

        // Create the collage and save it
        val collage = createCollage(bitmaps, 500, 500) // 500x500 pixels
        val uri = saveBitmapToCache(context, collage, playlist.id)

        return uri.toString()
    }
}

// Helper to convert Drawable to Bitmap
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}