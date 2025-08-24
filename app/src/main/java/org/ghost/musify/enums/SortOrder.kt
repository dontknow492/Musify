package org.ghost.musify.enums

enum class SortOrder(val value: String) {
    ASCENDING("ASC"),
    DESCENDING("DESC");

    companion object {
        fun fromValue(value: String): SortOrder {
            return entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}