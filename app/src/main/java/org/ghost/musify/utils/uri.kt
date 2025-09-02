package org.ghost.musify.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

fun getSongUri(songId: Long): Uri {
    // 1. Get the base URI for all external audio files
    val baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    // 2. Append the specific song's ID to the base URI
    return ContentUris.withAppendedId(baseUri, songId)
}


fun getAlbumArt(context: Context, mediaId: Long): Bitmap? {
    val retriever = MediaMetadataRetriever()
    try {
        val songUri = getSongUri(mediaId)
        retriever.setDataSource(context, songUri)
        val data = retriever.embeddedPicture
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        }
    } catch (e: Exception) {
        // Log the error or handle it as needed
        e.printStackTrace()
    } finally {
        // **CRITICAL:** Always release the retriever in a finally block
        retriever.release()
    }
    // Return null if no art is found or an error occurs
    return null
}

fun getAlbumArtUri(albumId: Long): Uri? {
    // This is the base URI for all external album art.
    val albumArtBaseUri = "content://media/external/audio/albumart".toUri()

    // Append the specific album's ID to the base URI.
    return ContentUris.withAppendedId(albumArtBaseUri, albumId)
}

fun cacheEmbeddedArt(context: Context, uri: Uri): Uri? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, uri)
    val art = retriever.embeddedPicture
    retriever.release()

    return art?.let {
        val file = File(context.cacheDir, "cover_${uri.hashCode()}.jpg")
        if (!file.exists()) {
            file.outputStream().use { os -> os.write(it) }
        }
        file.toUri()
    }
}


/**
 * Extracts and caches embedded album art from a media URI onto a background thread.
 *
 * This version is optimized to:
 * 1. Run all file and metadata operations on a background thread to prevent UI freezing.
 * 2. Check if the art is already cached *before* doing any expensive extraction work.
 * 3. Use try-catch blocks for robust handling of I/O and metadata errors.
 */
@RequiresApi(Build.VERSION_CODES.Q)
suspend fun cacheEmbeddedArts(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
    // 1. Define the target cache file based on a stable name.
    val fileName = "cover_${uri.toString().hashCode()}.jpg"
    val cacheFile = File(context.cacheDir, fileName)

    // 2. OPTIMIZATION: Check the cache first. If the file exists, return its URI immediately.
    if (cacheFile.exists() && cacheFile.length() > 0) {
        return@withContext cacheFile.toUri()
    }

    // 3. If not cached, proceed with the expensive extraction.
    val artBytes = try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture
        }
    } catch (e: Exception) {
        Log.e("ArtCache", "Failed to retrieve metadata for URI: $uri", e)
        null
    }

    // 4. If art was extracted, write it to the file and return the URI.
    artBytes?.let { bytes ->
        try {
            cacheFile.outputStream().use { os ->
                os.write(bytes)
            }
            cacheFile.toUri()
        } catch (e: IOException) {
            Log.e("ArtCache", "Failed to write cache file for URI: $uri", e)
            null // Return null if writing to the cache fails
        }
    }
}