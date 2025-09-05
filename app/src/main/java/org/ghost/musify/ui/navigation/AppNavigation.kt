package org.ghost.musify.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import org.ghost.musify.ui.screens.PlayerWindow
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
    playerViewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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


    val playSong: (Long, SongFilter) -> Unit = { songId, filter ->
        Log.d(TAG, "playSong: Playing song with ID $songId from filter: $filter")
        playerViewModel.playSongFromFilter(songId, filter)
        navController.navigate(NavScreen.Player(songId))
    }
    val playSongExtra: (Long, SongFilter, Boolean, Int) -> Unit =
        { songId, filter, shuffle, repeatMode ->
            Log.d(TAG, "playSong: Playing song with ID $songId from filter: $filter")
            playerViewModel.playSongFromFilter(songId, filter, shuffle, repeatMode)
            navController.navigate(NavScreen.Player(songId))
        }


    val playSongList: (Long, List<SongWithAlbumAndArtist>) -> Unit = { songId, songs ->
        Log.d(TAG, "playSongList: Playing song list with ID $songId from songs: $songs")
        playerViewModel.playSongFromList(songId, songs)
        navController.navigate(NavScreen.Player(songId))
    }

    val onMenuClick: (Long) -> Unit = { songId ->
        Log.d(TAG, "onMenuClick: Opening song menu for song ID $songId")
        navController.navigate(NavScreen.Dialogs.SongMenu(songId))
    }

    val onAddToPlaylist: (Long) -> Unit = { songId ->
        Log.d(TAG, "onAddToPlaylist: Navigating to add song ID $songId to a playlist")
        navController.navigate(
            NavScreen.Dialogs.AddToPlaylist(songId)
        )
    }

    val onAlbumClick: (Long) -> Unit = { albumId ->
        Log.d(TAG, "onAlbumClick: Navigating to album with ID $albumId")
        navController.navigate(NavScreen.Songs.Album(albumId))
    }
    val onArtistClick: (String) -> Unit = { artistName ->
        Log.d(TAG, "onArtistClick: Navigating to artist: $artistName")
        navController.navigate(NavScreen.Songs.Artist(artistName))
    }
    val onPlaylistClick: (Long) -> Unit = { playlistId ->
        Log.d(TAG, "onPlaylistClick: Navigating to playlist with ID $playlistId")
        navController.navigate(NavScreen.Songs.Playlist(playlistId))
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
            startDestination = startDestination,
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
                navController = navController,
                modifier = modifier,
                playerViewModel = playerViewModel,
                musicViewModel = musicViewModel,
                searchViewModel = searchViewModel,
                historyViewModel = historyViewModel,
                playSong = playSong,
                playSongList = playSongList,
                onMenuClick = onMenuClick,
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick,
            )

            songsNavigation(
                playerViewModel = playerViewModel,
                onMenuClick = onMenuClick,
                onBackClick = onBackClick,
                playSongExtra = playSongExtra
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
            settingNavigation()


        }

    }

}


@RequiresApi(Build.VERSION_CODES.Q)
private fun NavGraphBuilder.mainScreenNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    musicViewModel: MusicViewModel,
    searchViewModel: SearchViewModel,
    historyViewModel: HistoryViewModel,
    playSong: (Long, SongFilter) -> Unit = { _, _ -> },
    playSongList: (Long, List<SongWithAlbumAndArtist>) -> Unit = { _, _ -> },
    onMenuClick: (Long) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {},
) {
    val onNavigationItemClick = { screen: NavScreen ->
        navController.navigate(screen)
    }

    val onBottomPlayerClick = {
        navController.navigate(NavScreen.Player())
    }

    composable<NavScreen.Main.Home> {

        HomeScreen(
            viewModel = musicViewModel,
            playerViewModel = playerViewModel,
            onCardClick = playSong,
            onMenuClick = onMenuClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onPlaylistClick = onPlaylistClick,
            onSearchClick = { navController.navigate(NavScreen.Main.Search) },
            onNavigationItemClick = onNavigationItemClick,
            onBottomPlayerClick = onBottomPlayerClick,
        )
    }
    composable<NavScreen.Main.Search> {
        SearchScreen(
            viewModel = searchViewModel,
            playerViewModel = playerViewModel,
            onSongClick = playSongList,
            onMenuClick = onMenuClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onPlaylistClick = onPlaylistClick,
            onNavigationItemClick = onNavigationItemClick,
            onBottomPlayerClick = onBottomPlayerClick,
        )
//                    currentScreen = _root_ide_package_.org.ghost.musify.ui.navigation.NavScreen.Main.Search
    }

    composable<NavScreen.Main.History> {
        HistoryScreen(
            viewModel = historyViewModel,
            playerViewModel = playerViewModel,
            onSongClick = {},
            onNavigationItemClick = onNavigationItemClick,
            onBottomPlayerClick = onBottomPlayerClick,
        )
    }
    composable<NavScreen.Settings.Main> {
        SettingsScreen(
            modifier = modifier,
            playerViewModel = playerViewModel,
            navController = navController,
            onNavigationItemClick = onNavigationItemClick,
            onBottomPlayerClick = onBottomPlayerClick,
        )
    }

}

//@Composable
@RequiresApi(Build.VERSION_CODES.Q)
private fun NavGraphBuilder.songsNavigation(
//    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit = {},
    onMenuClick: (Long) -> Unit = {},
    playSongExtra: (Long, SongFilter, Boolean, Int) -> Unit = { _, _, _, _ -> }
) {
    composable<NavScreen.Songs.Album> {
        AlbumSongs(
            playerViewModel = playerViewModel,
            onSongClick = playSongExtra,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )
    }
    composable<NavScreen.Songs.Artist> {
        ArtistSongs(
            playerViewModel = playerViewModel,
            onSongClick = playSongExtra,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )
    }
    composable<NavScreen.Songs.Playlist> {
        PlaylistSongs(
            playerViewModel = playerViewModel,
            onSongClick = playSongExtra,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )
    }
}

private fun NavGraphBuilder.settingNavigation() {
    composable<NavScreen.Settings.General> {
        GeneralSettingScreen()
    }
    composable<NavScreen.Settings.Audio> {
        AudioSettingScreen()
    }
    composable<NavScreen.Settings.Notifications> {
        NotificationSettingScreen()
    }
    composable<NavScreen.Settings.Advanced> {
        AdvanceSettingScreen()
    }
    composable<NavScreen.Settings.Library> {
        LibrarySettingScreen()
    }
}



