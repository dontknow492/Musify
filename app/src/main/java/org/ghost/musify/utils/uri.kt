package org.ghost.musify.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.File

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