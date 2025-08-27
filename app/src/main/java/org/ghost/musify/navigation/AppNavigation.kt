package org.ghost.musify.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.ghost.musify.ui.screens.HomeScreen
import org.ghost.musify.ui.screens.songs.AlbumSongs
import org.ghost.musify.ui.screens.songs.ArtistSongs
import org.ghost.musify.ui.screens.songs.PlaylistSongs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: NavScreen = NavScreen.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<NavScreen.Home> {
            HomeScreen(
                viewModel = hiltViewModel(),
                onAlbumClick = { albumId ->
                    navController.navigate(NavScreen.AlbumSongs(albumId))
                },
                onArtistClick = { artistName ->
                    navController.navigate(NavScreen.ArtistSongs(artistName))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(NavScreen.PlaylistSongs(playlistId))
                }
            )
        }
        composable<NavScreen.Search> {}
        composable<NavScreen.History> {}
        composable<NavScreen.Setting> {}
        composable<NavScreen.AlbumSongs> {
            AlbumSongs()
        }
        composable<NavScreen.ArtistSongs> {
            ArtistSongs()
        }
        composable<NavScreen.PlaylistSongs> {
            PlaylistSongs()
        }

    }
}