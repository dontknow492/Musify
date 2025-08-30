package org.ghost.musify.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import org.ghost.musify.ui.screens.PlayerWindow
import org.ghost.musify.ui.screens.components.AppNavigationBar
import org.ghost.musify.ui.screens.models.SongFilter
import org.ghost.musify.ui.screens.models.SongsCategory
import org.ghost.musify.ui.screens.songs.AlbumSongs
import org.ghost.musify.ui.screens.songs.ArtistSongs
import org.ghost.musify.ui.screens.songs.PlaylistSongs
import org.ghost.musify.viewModels.PlayerViewModel

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
            bottomBar = {
                val show = when (currentScreen) {
                    is NavScreen.Home -> true
                    is NavScreen.Search -> true
                    is NavScreen.History -> true
                    is NavScreen.Setting -> true
                    is NavScreen.AlbumSongs -> false
                    is NavScreen.ArtistSongs -> false
                    is NavScreen.PlaylistSongs -> false
                    is NavScreen.PlayerScreen -> false
                    else -> true
                }
                AnimatedVisibility(true) {
                    val bottomModifier = if (show) Modifier else Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                    Column(
                        modifier = bottomModifier
//                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        AnimatedVisibility(false) {
                            BottomPlayer(
                                modifier = modifier.clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp
                                    )
                                )
                            )
                        }
                        if (show) {
                            HorizontalDivider()
                        }
                        AnimatedVisibility(show) {

                            AppNavigationBar(
                                currentRoute = NavScreen.Home,
                                onClick = { navController.navigate(it) }
                            )
                        }


                    }
                }

            }
        ) { innerPadding ->
            Box {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(
                        bottom = innerPadding.calculateBottomPadding() - WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
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
                    composable<NavScreen.PlayerScreen> {
                        LaunchedEffect(Unit) {
                            currentScreen = NavScreen.PlayerScreen()
                        }
                        val playerViewModel: PlayerViewModel = hiltViewModel()
                        playerViewModel.playSongFromFilter(
                            0, SongFilter(category = SongsCategory.AllSongs)
                        )
//    )
                        PlayerWindow(viewModel = playerViewModel)
//                        PlayerScreen()
                    }
                }
            }

        }
    }
}