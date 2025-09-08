package org.ghost.musify.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.dialog.AddToPlaylistDialog
import org.ghost.musify.ui.dialog.menu.SongMenu
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.screens.HistoryScreen
import org.ghost.musify.ui.screens.HomeScreen
import org.ghost.musify.ui.screens.player.PlayerWindow
import org.ghost.musify.ui.screens.SearchScreen
import org.ghost.musify.ui.screens.onboarding.OnboardingScreen
import org.ghost.musify.ui.screens.setting.SettingsScreen
import org.ghost.musify.ui.screens.setting.child.AdvanceSettingScreen
import org.ghost.musify.ui.screens.setting.child.AudioSettingScreen
import org.ghost.musify.ui.screens.setting.child.GeneralSettingScreen
import org.ghost.musify.ui.screens.setting.child.LibrarySettingScreen
import org.ghost.musify.ui.screens.setting.child.NotificationSettingScreen
import org.ghost.musify.ui.screens.songs.AlbumSongs
import org.ghost.musify.ui.screens.songs.ArtistSongs
import org.ghost.musify.ui.screens.songs.PlaylistSongs
import org.ghost.musify.viewModels.HistoryViewModel
import org.ghost.musify.viewModels.MainUiState
import org.ghost.musify.viewModels.MainViewModel
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.SearchViewModel
import org.ghost.musify.viewModels.home.MusicViewModel

const val TAG = "NavigationHandler"

@OptIn(ExperimentalSharedTransitionApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: NavScreen = NavScreen.Main.Home,
    viewModel: MainViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val playerViewModel: PlayerViewModel = hiltViewModel()


    var hasNavigatedFromSplash by rememberSaveable { mutableStateOf(false) }

    // This effect handles the one-time navigation away from the splash screen.
    LaunchedEffect(uiState, hasNavigatedFromSplash) {
        Log.d(TAG, "AppNavigation: Checking navigation state $uiState, $hasNavigatedFromSplash")
        if (!hasNavigatedFromSplash) {
            when (uiState) {
                MainUiState.NavigateToOnboarding -> {
                    navController.navigate(NavScreen.Launch.Onboarding) {
                        popUpTo(NavScreen.Launch.Splash) { inclusive = true }
                    }
                    hasNavigatedFromSplash = true // Mark that navigation has occurred.
                }

                MainUiState.NavigateToMainApp -> {
                    navController.navigate(startDestination) {
                        popUpTo(NavScreen.Launch.Splash) { inclusive = true }
                    }
                    hasNavigatedFromSplash = true // Mark that navigation has occurred.
                }

                MainUiState.Loading -> { /* Do nothing, waiting for view model. */
                }
            }
        }
    }

    var searchViewModel: SearchViewModel = hiltViewModel()
    val musicViewModel: MusicViewModel = hiltViewModel()
    val historyViewModel: HistoryViewModel = hiltViewModel()

    val onNavigate: (NavScreen) -> Unit = { screen ->
        Log.d(TAG, "onNavigate: Navigating to $screen")
        navController.navigate(screen)
    }


    val onAddToPlaylist: (Long) -> Unit = { songId ->
        Log.d(TAG, "onAddToPlaylist: Navigating to add song ID $songId to a playlist")
        navController.navigate(
            NavScreen.Dialogs.AddToPlaylist(songId)
        )
    }


    val onBackClick: () -> Unit = {
        Log.d(
            TAG,
            "onBackClick: Popping back stack"
        )
        navController.popBackStack()
    }

    SharedTransitionLayout(
        modifier = modifier
    ) {

        NavHost(
            navController = navController,
            startDestination = NavScreen.Launch.Splash,
        ) {
            composable<NavScreen.Launch.Splash> {}

            composable<NavScreen.Launch.Onboarding> {
                OnboardingScreen(onOnboardingComplete = {
                    viewModel.syncMedia()
                    navController.navigate(startDestination) {
                        popUpTo(NavScreen.Launch.Onboarding) { inclusive = true }
                    }
                    viewModel.setOnboardingCompleted()
                })
            }

            mainScreenNavigation(
                modifier = modifier,
                onNavigate = onNavigate,
                musicViewModel = musicViewModel,
                searchViewModel = searchViewModel,
                historyViewModel = historyViewModel,
                playerViewModel = playerViewModel,
            )

            songsNavigation(
                onNavigate = onNavigate,
                onBackClick = onBackClick,
                playerViewModel = playerViewModel,
            )

            composable<NavScreen.Player> {
                PlayerWindow(
                    viewModel = playerViewModel,
                    onBackClick = onBackClick,
                    onAddToPlaylistClick = onAddToPlaylist
                )
//                        PlayerScreen()
            }

            dialog<NavScreen.Dialogs.SongMenu> {
                SongMenu(
                    onDismissRequest = {
                        // To "just back", you simply pop the back stack.
                        navController.popBackStack()
                    },
                    onAddToPlaylist = onAddToPlaylist

                )
//                        PlayerScreen()
            }
            dialog<NavScreen.Dialogs.AddToPlaylist> {
                AddToPlaylistDialog(
                    onDismissRequest = {
                        // To "just back", you simply pop the back stack.
                        onBackClick()
                    },
                )
            }

            // setting
            settingNavigation(
                onBackClick = onBackClick
            )


        }

    }

}


@RequiresApi(Build.VERSION_CODES.Q)
private fun NavGraphBuilder.mainScreenNavigation(
    modifier: Modifier = Modifier,
    onNavigate: (NavScreen) -> Unit,
    playerViewModel: PlayerViewModel,
    musicViewModel: MusicViewModel,
    searchViewModel: SearchViewModel,
    historyViewModel: HistoryViewModel,
) {

    composable<NavScreen.Main.Home> {
        HomeScreen(
            viewModel = musicViewModel,
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
        )
    }
    composable<NavScreen.Main.Search> {
        SearchScreen(
            viewModel = searchViewModel,
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
        )
//                    currentScreen = _root_ide_package_.org.ghost.musify.ui.navigation.NavScreen.Main.Search
    }

    composable<NavScreen.Main.History> {
        HistoryScreen(
            viewModel = historyViewModel,
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
            onSongClick = {},
        )
    }
    composable<NavScreen.Settings.Main> {
        SettingsScreen(
            modifier = modifier,
            onNavigate = onNavigate,
            playerViewModel = playerViewModel,
        )
    }

}

//@Composable
@RequiresApi(Build.VERSION_CODES.Q)
private fun NavGraphBuilder.songsNavigation(
//    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onNavigate: (NavScreen) -> Unit,
) {
    composable<NavScreen.Songs.Album> {
        AlbumSongs(
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Songs.Artist> {
        ArtistSongs(
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Songs.Playlist> {
        PlaylistSongs(
            playerViewModel = playerViewModel,
            onNavigate = onNavigate,
            onBackClick = onBackClick,
        )
    }
}

private fun NavGraphBuilder.settingNavigation(
    onBackClick: () -> Unit,
) {
    composable<NavScreen.Settings.General> {
        GeneralSettingScreen(
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Settings.Audio> {
        AudioSettingScreen(
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Settings.Notifications> {
        NotificationSettingScreen(
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Settings.Advanced> {
        AdvanceSettingScreen(
            onBackClick = onBackClick,
        )
    }
    composable<NavScreen.Settings.Library> {
        LibrarySettingScreen(
            onBackClick = onBackClick,
        )
    }
}






