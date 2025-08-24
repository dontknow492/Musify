# **Musify \- Local Music Player**

A modern, offline-first local music player for Android built entirely with Jetpack Compose, following modern Android development best practices.

## **Overview**

Musify is a feature-rich music player designed to scan, organize, and play audio files stored locally on an Android device. It provides a fluid user experience with dynamic theming, robust playback controls, and powerful library management features. The app is built with a focus on performance and a clean, scalable architecture, making it a solid foundation for a fully-fledged music application.

## **✨ Features**

* **Local Media Scanning:** Automatically scans the device's MediaStore to find and import all music files.
* **Offline-First Caching:** Uses a Room database as a single source of truth for a fast, responsive, and offline-capable user experience.
* **Full Playback Control:** Standard controls including play, pause, next, previous, and a seek bar.
* **Background Playback:** Leverages a MediaSessionService to ensure music continues playing when the app is in the background or the screen is off.
* **Dynamic UI:**
    * Dynamic Color: UI themes adapt based on the album art of the currently playing song.
    * Pagination: Efficiently handles large music libraries using the Jetpack Paging 3 library.
* **Library Organization:**
    * **Advanced Sorting:** Sort song lists by title, duration, year, date added, and more.
    * **Filtering:** Search within any list (all songs, playlists, favorites) to quickly find tracks.
* **Playlist Management:**
    * Create, rename, and delete custom playlists.
    * Add or remove songs from any playlist.
    * Export playlists to M3U format (planned).
* **User Statistics & History:**
    * **Favorites:** Mark songs as favorites for quick access.
    * **History:** Keeps a log of all played tracks.
    * **Statistics:** Tracks play counts and listening time for songs and artists to generate "Top Played" lists.
* **Notification & Widget Support:**
    * **Media Notification:** Rich notification with playback controls and a seek bar.
    * **Widget Support:** Home screen widget for quick playback control (planned).
* **Audio Effects:**
    * Support for Equalizer, Bass Boost, and other audio effects using the Android framework (planned).

## **🛠 Tech Stack & Architecture**

* **UI:** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) for a declarative and modern UI.
* **Architecture:** Follows the official "Guide to App Architecture".
    * **UI Layer:** Activities/Composables and ViewModels.
    * **Domain Layer:** (Optional) Use cases for complex business logic.
    * **Data Layer:** Repositories and Data Sources (Room & MediaStore).
* **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html) for managing background tasks and data streams.
* **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for managing dependencies throughout the app.
* **Database:** [Room](https://developer.android.com/training/data-storage/room) for robust, offline caching of all music metadata.
* **Pagination:** [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) for efficiently loading and displaying large lists of songs.
* **Media Playback:** [Jetpack Media3 (ExoPlayer)](https://www.google.com/search?q=https://developer.android.com/jetpack/media3) for powerful and reliable audio playback and background session management.
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for loading and displaying album art.

## **🗂 Database Schema**

The app uses a Room database with several interconnected entities to manage the music library efficiently.

* **SongEntity**: The central table holding all metadata for each song, synced from the MediaStore.
* **PlaylistEntity**: Stores information for each user-created playlist.
* **PlaylistSongCrossRef**: A many-to-many join table linking songs to playlists.
* **FavoriteSongEntity**: A simple table that stores the IDs of songs marked as favorites, along with the date they were added.
* **HistoryEntity**: A log of every song playback event, including duration and timestamp.
* **SongStatsEntity**: An aggregated table for quick access to statistics like playCount for each song.
* **ArtistStatsEntity**: An aggregated table for artist-specific statistics.

All primary and foreign keys are indexed for fast query performance.

## **🚀 Setup and Build**

To build and run the project, follow these steps:

1. **Clone the repository:**  
   git clone https://github.com/dontknow492/Musify.git

2. Open in Android Studio:  
   Open the project in the latest stable version of Android Studio.
3. Sync Gradle:  
   Let Android Studio download all the required dependencies.
4. Build and Run:  
   Connect a device or start an emulator and run the app. The app will request permission to read audio files on the first launch.