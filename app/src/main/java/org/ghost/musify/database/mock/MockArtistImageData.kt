package org.ghost.musify.database.mock

import org.ghost.musify.entity.ArtistImageEntity
import java.util.concurrent.atomic.AtomicLong

object MockArtistImageData {
    // A list of sample artist names to pull from.
    private val artistNames = listOf(
        // Original artists
        "The Lumineers", "Of Monsters and Men", "Fleetwood Mac",
        "Queen", "David Bowie", "Led Zeppelin", "Pink Floyd",
        "The Beatles", "Radiohead", "Arcade Fire", "Bon Iver",
        "Florence + The Machine", "Arctic Monkeys", "The Strokes",
        "Vampire Weekend", "Tame Impala", "Gorillaz",

        // Additional Indie and Alternative Rock Bands
        "The National", "Modest Mouse", "Neutral Milk Hotel",
        "Wolf Parade", "The Decemberists", "Beirut", "Grizzly Bear",
        "Fleet Foxes", "Death Cab for Cutie", "Wilco", "My Morning Jacket",
        "The Shins", "Broken Bells", "Band of Horses", "Iron & Wine",
        "Alt-J", "Glass Animals", "MGMT", "Passion Pit", "Foster the People",
        "Phoenix", "Two Door Cinema Club", "The xx", "Vampire Weekend",
        "Lord Huron", "Bon Iver", "Father John Misty", "Kurt Vile",
        "Mac DeMarco", "Cage the Elephant", "The War on Drugs",
        "Real Estate", "Beach House", "Grimes", "St. Vincent",
        "Sufjan Stevens", "Phoebe Bridgers", "Big Thief", "Angel Olsen",
        "Sharon Van Etten", "Mitski", "Japanese Breakfast", "Lucy Dacus",
        "Weyes Blood", "Julia Holter", "King Gizzard & The Lizard Wizard",
        "Ty Segall", "Unknown Mortal Orchestra", "King Krule", "Car Seat Headrest",
        "Whitney", "Men I Trust", "Tennis", "Wild Nothing", "Alvvays",
        "Parcels", "Metronomy", "Cut Copy", "Friendly Fires", "STRFKR",
        "Portugal. The Man", "Young the Giant", "Walk the Moon",
        "Cold War Kids", "The Killers", "Imagine Dragons", "AJR",
        "Twenty One Pilots", "Bastille", "X Ambassadors", "Grouplove",
        "Portugal. The Man", "Manchester Orchestra", "Young the Giant",
        "The Head and the Heart", "Edward Sharpe and the Magnetic Zeros"
    ).distinct()

    // An AtomicLong to simulate auto-incrementing primary keys for URIs.
    private val uriIdCounter = AtomicLong(100L)

    /**
     * Generates a list of mock ArtistImageEntity objects.
     * It ensures no duplicate artist names are used.
     *
     * @param count The number of artist entries to create.
     * @return A list of ArtistImageEntity objects.
     */
    fun generate(count: Int = 15): List<ArtistImageEntity> {
        val artistImageList = mutableListOf<ArtistImageEntity>()

        // Take a shuffled subset of the artist names to avoid duplicates
        val selectedArtists = artistNames.shuffled().take(count)

        for (artistName in selectedArtists) {
            // Generate a plausible placeholder image URL using the artist's name
            val formattedName = artistName.replace(" ", "+")
            val imageUrl = "https://example.com/images/${formattedName}_art.jpg"

            artistImageList.add(
                ArtistImageEntity(
                    name = artistName,
                    // Use a counter for a unique, non-null URI ID
                    imageUriId = uriIdCounter.getAndIncrement(),
                    imageUrl = imageUrl
                )
            )
        }
        return artistImageList
    }
}