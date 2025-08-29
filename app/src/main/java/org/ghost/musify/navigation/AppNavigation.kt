package org.ghost.musify.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.ghost.musify.ui.screens.BottomPlayer
import org.ghost.musify.ui.screens.HomeScreen
import org.ghost.musify.ui.screens.HomeTopAppBar
import org.ghost.musify.ui.screens.components.AppNavigationBar
import org.ghost.musify.ui.screens.components.SearchableTopAppBar
import org.ghost.musify.ui.screens.songs.AlbumSongs
import org.ghost.musify.ui.screens.songs.ArtistSongs
import org.ghost.musify.ui.screens.songs.PlaylistSongs

@OptIn(ExperimentalSharedTransitionApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: NavScreen = NavScreen.Home
) {
    var currentScreen by remember { mutableStateOf(startDestination) }
    HomeTopAppBar()
    SharedTransitionLayout(
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                when (currentScreen) {
                    is NavScreen.Home -> {
                        HomeTopAppBar()
                    }

                    else -> {
                        SearchableTopAppBar()
                    }
                }
            },
            bottomBar = {
                val show = when (currentScreen) {
                    is NavScreen.Home -> true
                    is NavScreen.Search -> true
                    is NavScreen.History -> true
                    is NavScreen.Setting -> true
                    is NavScreen.AlbumSongs -> false
                    is NavScreen.ArtistSongs -> false
                    is NavScreen.PlaylistSongs -> false
                    else -> true
                }

                Column(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    AnimatedVisibility(true) {
                        BottomPlayer(
                            modifier = modifier.clip(
                                RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 8.dp
                                )
                            )
                        )
                    }

                    HorizontalDivider()
                    AnimatedVisibility(show) {

                        AppNavigationBar(
                            currentRoute = NavScreen.Home,
                            onClick = { navController.navigate(it) }
                        )
                    }


                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            Box {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = modifier
                ) {
                    composable<NavScreen.Home> {
                        LaunchedEffect(Unit) {
                            currentScreen = NavScreen.Home
                        }

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
                    composable<NavScreen.Search> {
                        LaunchedEffect(Unit) {
                            currentScreen = NavScreen.Search
                        }
//                    currentScreen = NavScreen.Search
                    }
                    composable<NavScreen.History> {
                        LaunchedEffect(Unit) {
                            currentScreen = NavScreen.History
                        }
                    }
                    composable<NavScreen.Setting> {
                        LaunchedEffect(Unit) {
                            currentScreen = NavScreen.Setting
                        }
                    }
                    composable<NavScreen.AlbumSongs> {
                        LaunchedEffect(Unit) {
                            currentScreen =
                                NavScreen.AlbumSongs(it.arguments?.getLong("albumId") ?: 0L)
                        }
                        AlbumSongs()
                    }
                    composable<NavScreen.ArtistSongs> {
                        LaunchedEffect(Unit) {
                            currentScreen =
                                NavScreen.ArtistSongs(it.arguments?.getString("artistName") ?: "")
                        }
                        ArtistSongs()
                    }
                    composable<NavScreen.PlaylistSongs> {
                        LaunchedEffect(Unit) {
                            currentScreen =
                                NavScreen.PlaylistSongs(it.arguments?.getLong("playlistId") ?: 0L)
                        }
                        PlaylistSongs()
                    }
                }
            }

        }
    }
}