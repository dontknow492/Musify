package org.ghost.musify.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.ghost.musify.R
import org.ghost.musify.ui.components.MyBottomAppBar
import org.ghost.musify.ui.components.SongsLazyColumn
import org.ghost.musify.ui.models.SongFilter
import org.ghost.musify.ui.models.SongsCategory
import org.ghost.musify.ui.models.Tab
import org.ghost.musify.ui.navigation.NavScreen
import org.ghost.musify.ui.screens.tabWindow.AlbumScreen
import org.ghost.musify.ui.screens.tabWindow.ArtistScreen
import org.ghost.musify.ui.screens.tabWindow.PlaylistScreen
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.home.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigate: (NavScreen) -> Unit,
) {
    val onNavigationItemClick = onNavigate

    val onAlbumClick: (Long) -> Unit = {
        onNavigate(NavScreen.Songs.Album(it))
    }

    val onArtistClick: (String) -> Unit = {
        onNavigate(NavScreen.Songs.Artist(it))
    }

    val onPlaylistClick: (Long) -> Unit = {
        onNavigate(NavScreen.Songs.Playlist(it))
    }
    val onMenuClick: (Long) -> Unit = {
        onNavigate(NavScreen.Dialogs.SongMenu(it))
    }
    val onBottomPlayerClick: () -> Unit = {
        onNavigate(NavScreen.Player(-1L))
    }
    val onSearchClick: () -> Unit = {
        onNavigate(NavScreen.Main.Search)
    }

    val onSongClick: (Long) -> Unit = {songId ->
        playerViewModel.playSongFromFilter(
            songId,
            SongFilter(SongsCategory.AllSongs),
            false,
            0,
        )
        onNavigate(NavScreen.Player(songId))
    }


    val pageState = rememberPagerState(initialPage = 0) { 5 }
    val tabs = listOf(
        Tab(
            title = "Songs",
            icon = R.drawable.ic_music_black
        ),
        Tab(
            title = "Artists",
            icon = R.drawable.outline_album_24
        ),
        Tab(
            title = "Albums",
            icon = R.drawable.music_album_icon_2
        ),
        Tab(
            title = "Playlists",
            icon = R.drawable.playlist_placeholder
        ),
        Tab(
            title = "Liked",
            icon = R.drawable.rounded_favorite_24
        )
    )
    val scope = rememberCoroutineScope()

    var isBottomSheetVisible by remember { mutableStateOf(false) }


    val allSongs = viewModel.music.collectAsLazyPagingItems()
    val favoriteSongs = viewModel.favoriteSongs.collectAsLazyPagingItems()



    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopAppBar(
                onFilterClick = { isBottomSheetVisible = true },
                onSearchClick = onSearchClick
            )
        },
        bottomBar = {
            MyBottomAppBar(
                playerViewModel = playerViewModel,
                currentScreen = NavScreen.Main.Home,
                onPlayerClick = onBottomPlayerClick,
                onNavigationItemClick = onNavigationItemClick
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = {},
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                SongsTab(
                    selectedIndex = pageState.currentPage,
                    tabs = tabs,
                    onClick = {
                        scope.launch {
                            Log.d("HomeScreen", "Going to page: $it")
                            pageState.animateScrollToPage(it)
                        }

                    }
                )
                HorizontalPager(
                    pageState,
                    beyondViewportPageCount = 5
                ) { page ->
                    when (page) {
                        0 -> SongsLazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            allSongs,
                            item = {},
                            onSongClick = onSongClick,
                            onMenuClick = onMenuClick,
                        )

                        1 -> ArtistScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = hiltViewModel(),
                            onArtistClick = onArtistClick,
                        )

                        2 -> AlbumScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = hiltViewModel(),
                            onAlbumClick = onAlbumClick,
                        )

                        3 -> PlaylistScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = hiltViewModel(),
                            onPlaylistClick = onPlaylistClick,
                        )

                        4 -> SongsLazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            favoriteSongs,
                            item = {},
                            onSongClick = onSongClick,
                            onMenuClick = onMenuClick,
                        )
                    }
                }
            }
        }
    }
    AnimatedVisibility(isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isBottomSheetVisible = false },
        ) {

        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit = {},
    onSearchClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                "My Music",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        actions = {
            IconButton(
                onClick = onSearchClick
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
            IconButton(
                onClick = onFilterClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_filter_list_24),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsTab(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    tabs: List<Tab>,
    onClick: (Int) -> Unit = {},

    ) {
    val listState = rememberLazyListState()

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs, key = { index, tab -> tab.title }) { index, tab ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = {
                    onClick(index)
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                },
                leadingIcon = {
                    tab.icon?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = CircleShape
            )

        }

    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }
}