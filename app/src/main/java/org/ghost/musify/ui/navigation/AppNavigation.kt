package org.ghost.musify.ui.navigation

import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import org.ghost.musify.entity.relation.SongWithAlbumAndArtist
import org.ghost.musify.ui.screens.BottomPlayer
import org.ghost.musify.ui.screens.HistoryScreen
import org.ghost.musify.ui.screens.HomeScreen
import org.ghost.musify.ui.screens.PlayerWindow
import org.ghost.musify.ui.screens.SearchScreen
import org.ghost.musify.ui.components.AppNavigationBar
import org.ghost.musify.ui.dialog.AddToPlaylistDialog
import org.ghost.musify.ui.dialog.menu.SongMenu
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.screens.setting.SettingsScreen
import org.ghost.musify.ui.screens.setting.child.AdvanceSettingScreen
import org.ghost.musify.ui.screens.setting.child.AudioSettingScreen
import org.ghost.musify.ui.screens.setting.child.GeneralSettingScreen
import org.ghost.musify.ui.screens.setting.child.LibrarySettingScreen
import org.ghost.musify.ui.screens.setting.child.NotificationSettingScreen
import org.ghost.musify.ui.screens.songs.AlbumSongs
import org.ghost.musify.ui.screens.songs.ArtistSongs
import org.ghost.musify.ui.screens.songs.PlaylistSongs
import org.ghost.musify.viewModels.AddToPlaylistViewModel
import org.ghost.musify.viewModels.HistoryViewModel
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.SearchViewModel
import org.ghost.musify.viewModels.SongViewModel
import org.ghost.musify.viewModels.home.MusicViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: NavScreen = NavScreen.Home
) {
    var currentScreen by remember { mutableStateOf(startDestination) }
    var previousScreen by remember { mutableStateOf<NavScreen?>(null) }
    var searchViewModel: SearchViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val musicViewModel: MusicViewModel = hiltViewModel()
    val historyViewModel: HistoryViewModel = hiltViewModel()

    val currentPlay by playerViewModel.uiState.collectAsState()

    val TAG = "NavigationHandler"


    val playSong: (Long, SongFilter) -> Unit = { songId, filter ->
        Log.d(TAG, "playSong: Playing song with ID $songId from filter: $filter")
        playerViewModel.playSongFromFilter(songId, filter)
        navController.navigate(NavScreen.PlayerScreen(songId))
    }
    val playSongExtra: (Long, SongFilter, Boolean, Int) -> Unit =
        { songId, filter, shuffle, repeatMode ->
            Log.d(TAG, "playSong: Playing song with ID $songId from filter: $filter")
            playerViewModel.playSongFromFilter(songId, filter, shuffle, repeatMode)
            navController.navigate(NavScreen.PlayerScreen(songId))
        }


    val playSongList: (Long, List<SongWithAlbumAndArtist>) -> Unit = { songId, songs ->
        Log.d(TAG, "playSongList: Playing song list with ID $songId from songs: $songs")
        playerViewModel.playSongFromList(songId, songs)
        navController.navigate(NavScreen.PlayerScreen(songId))
    }

    val onMenuClick: (Long) -> Unit = { songId ->
        Log.d(TAG, "onMenuClick: Opening song menu for song ID $songId")
        navController.navigate(NavScreen.SongMenu(songId))
    }

    val onAddToPlaylist: (Long) -> Unit = { songId ->
        Log.d(TAG, "onAddToPlaylist: Navigating to add song ID $songId to a playlist")
        navController.navigate(
            NavScreen.AddToPlaylist(songId)
        )
    }

    val onAlbumClick: (Long) -> Unit = { albumId ->
        Log.d(TAG, "onAlbumClick: Navigating to album with ID $albumId")
        navController.navigate(NavScreen.AlbumSongs(albumId))
    }
    val onArtistClick: (String) -> Unit = { artistName ->
        Log.d(TAG, "onArtistClick: Navigating to artist: $artistName")
        navController.navigate(NavScreen.ArtistSongs(artistName))
    }
    val onPlaylistClick: (Long) -> Unit = { playlistId ->
        Log.d(TAG, "onPlaylistClick: Navigating to playlist with ID $playlistId")
        navController.navigate(NavScreen.PlaylistSongs(playlistId))
    }

    val onBackClick: () -> Unit = {
        Log.d(
            TAG,
            "onBackClick: Popping back stack. Current screen: $currentScreen, Previous: $previousScreen"
        )
        navController.popBackStack()
        currentScreen = previousScreen ?: NavScreen.Home
    }


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
                        AnimatedVisibility(currentPlay.currentSong != null && currentScreen !is NavScreen.PlayerScreen) {
                            BottomPlayer(
                                modifier = modifier.clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp
                                    )
                                ),
                                playerUiState = currentPlay,
                                onClick = {
                                    navController.navigate(
                                        NavScreen.PlayerScreen(
                                            currentPlay.currentSong?.song?.id ?: 0L
                                        )
                                    )
                                },
                                onPlayPauseClick = {
                                    playerViewModel.onPlayPauseClicked()
                                },
                                onCloseCLick = {}
                            )
                        }
                        if (show) {
                            HorizontalDivider()
                        }
                        AnimatedVisibility(show) {

                            AppNavigationBar(
                                currentRoute = currentScreen,
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
                        bottom = max(0.dp, innerPadding.calculateBottomPadding() - WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()),
                        start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateRightPadding(LocalLayoutDirection.current)
                    )
                ) {
                    composable<NavScreen.Home> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = NavScreen.Home
                        }

                        HomeScreen(
                            viewModel = musicViewModel,
                            onCardClick = playSong,
                            onMenuClick = onMenuClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onPlaylistClick = onPlaylistClick,
                            onSearchClick = { navController.navigate(NavScreen.Search) }
                        )
                    }
                    composable<NavScreen.Search> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = NavScreen.Search
                        }
                        SearchScreen(
                            viewModel = searchViewModel,
                            onSongClick = playSongList,
                            onMenuClick = onMenuClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onPlaylistClick = onPlaylistClick,
                        )
//                    currentScreen = NavScreen.Search
                    }
                    composable<NavScreen.History> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = NavScreen.History
                        }
                        HistoryScreen(viewModel = historyViewModel, onSongClick = {})
                    }
                    composable<NavScreen.Setting> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = NavScreen.Setting
                        }
                        SettingsScreen(
                            modifier = modifier,
                            navController = navController
                        )
                    }
                    composable<NavScreen.AlbumSongs> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen =
                                NavScreen.AlbumSongs(it.arguments?.getLong("albumId") ?: 0L)
                        }
                        AlbumSongs(
                            onSongClick = playSongExtra,
                            onBackClick = onBackClick,
                            onMenuClick = onMenuClick
                        )
                    }
                    composable<NavScreen.ArtistSongs> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen =
                                NavScreen.ArtistSongs(it.arguments?.getString("artistName") ?: "")
                        }
                        ArtistSongs(
                            onSongClick = playSongExtra,
                            onBackClick = onBackClick,
                            onMenuClick = onMenuClick
                        )
                    }
                    composable<NavScreen.PlaylistSongs> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen =
                                NavScreen.PlaylistSongs(it.arguments?.getLong("playlistId") ?: 0L)
                        }
                        PlaylistSongs(
                            onSongClick = playSongExtra,
                            onBackClick = onBackClick,
                            onMenuClick = onMenuClick
                        )
                    }
                    composable<NavScreen.PlayerScreen> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = NavScreen.PlayerScreen()
                        }
//    )
                        PlayerWindow(
                            viewModel = playerViewModel,
                            onBackClick = onBackClick,
                            onAddToPlaylistClick = onAddToPlaylist
                        )
//                        PlayerScreen()
                    }
                    dialog<NavScreen.SongMenu> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen =
                                NavScreen.SongMenu(it.arguments?.getLong("songId") ?: 0L)
                        }
                        val songViewModel: SongViewModel = hiltViewModel()
//    )
                        SongMenu(
                            viewModel = songViewModel,
                            onDismissRequest = {
                                // To "just back", you simply pop the back stack.
                                navController.popBackStack()
                            },
                            onAddToPlaylist = onAddToPlaylist

                        )
//                        PlayerScreen()
                    }
                    dialog<NavScreen.AddToPlaylist> {
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen =
                                NavScreen.AddToPlaylist(it.arguments?.getLong("songId") ?: 0L)
                        }
                        val addToPlaylistViewModel: AddToPlaylistViewModel = hiltViewModel()
                        AddToPlaylistDialog(
                            viewModel = addToPlaylistViewModel,
                            onDismissRequest = {
                                // To "just back", you simply pop the back stack.
                                onBackClick()
                            },

                            )
                    }

                    // setting


                    composable< SettingScreen.GeneralSettings>{
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = SettingScreen.GeneralSettings
                        }
                        GeneralSettingScreen()
                    }
                    composable< SettingScreen.AudioSettings>{
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = SettingScreen.AudioSettings
                        }
                        AudioSettingScreen()
                    }
                    composable< SettingScreen.NotificationsSettings>{
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = SettingScreen.NotificationsSettings
                        }
                        NotificationSettingScreen()
                    }
                    composable< SettingScreen.AdvancedSettings>{
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = SettingScreen.AdvancedSettings
                        }
                        AdvanceSettingScreen()
                    }
                    composable< SettingScreen.LibrarySettings>{
                        LaunchedEffect(Unit) {
                            previousScreen = currentScreen
                            currentScreen = SettingScreen.LibrarySettings
                        }
                        LibrarySettingScreen()
                    }
                }
            }

        }
    }
}


