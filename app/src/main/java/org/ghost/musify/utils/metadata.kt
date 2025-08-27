package org.ghost.musify.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import java.io.IOException

/**
 * Gets the embedded thumbnail for a specific song using its ID.
 * This is the modern and recommended method.
 *
 * @param context The application context.
 * @param songId The ID of the song from MediaStore.
 * @param width The desired width of the thumbnail.
 * @param height The desired height of the thumbnail.
 * @return A Bitmap of the thumbnail, or null if not found or an error occurs.
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun getMusicThumbnailBitmap(
    context: Context,
    songId: Long,
    width: Int = 512,
    height: Int = 512
): Bitmap? {
    // 1. First, get the URI of the song itself
    val songUri: Uri = ContentUris.withAppendedId(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        songId
    )

    // 2. Use loadThumbnail to get the embedded image
    return try {
        val size = Size(width, height)
        context.contentResolver.loadThumbnail(songUri, size, null)
    } catch (e: IOException) {
        // Handle exceptions, e.g., the file is no longer available
        e.printStackTrace()
        null
    }
}