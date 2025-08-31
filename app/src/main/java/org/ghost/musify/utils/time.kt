package org.ghost.musify.utils

//import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
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

@RequiresApi(Build.VERSION_CODES.O)
fun Long.toFormattedDate(): String {
    // 1. Create a formatter with the desired pattern
    val formatter = DateTimeFormatter
        .ofPattern("d MMMM yyyy", Locale.getDefault())

    // 2. Convert the Long (milliseconds) to an Instant
    val instant = Instant.ofEpochMilli(this)

    // 3. Format the Instant using the system's default time zone
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

fun Int.toFormattedDuration(): String {
    // This function operates on the Long value it's called on (the milliseconds)

    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(this.toLong())

    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}