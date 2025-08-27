package org.ghost.musify.utils

//import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration


@RequiresApi(Build.VERSION_CODES.O)
fun formatDuration(millis: Long): String {

// 1. Convert milliseconds to whole seconds
    val duration = Duration.ofMillis(millis)

// Extract the parts you need
    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    val seconds = duration.toSeconds() % 60 // Get the remaining seconds


// 2. Format using DateUtils
    if (hours > 0) {
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds)
    }
    return String.format("%02d:%02d", minutes, seconds)
}