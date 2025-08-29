package org.ghost.musify.utils

//import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.util.concurrent.TimeUnit


fun Long.toFormattedDuration(): String {
    // This function operates on the Long value it's called on (the milliseconds)

    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(this)

    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun Int.toFormattedDuration(): String {
    // This function operates on the Long value it's called on (the milliseconds)

    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(this.times(1000L))

    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}