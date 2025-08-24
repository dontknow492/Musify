package org.ghost.musify.enums

enum class SortBy(val value: String) {
    TITLE("title"),
    DURATION("duration"),
    DURATION_PLAYED("duration_played"), // For stats, the total duration the song has been played
    YEAR("year"),
    DATE_ADDED("date_added"),
    DATE_MODIFIED("date_modified"),
    ADDED_AT("added_at"), // For favorites, the timestamp when the song was marked as favorite
    PLAYED_AT("played_at"), // For history, the timestamp when the song was last played
    PLAY_COUNT("play_count"); // For stats, the total number of times the song has been played

    companion object {
        fun fromValue(value: String): SortBy {
            return entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}