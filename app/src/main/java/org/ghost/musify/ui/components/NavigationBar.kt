package org.ghost.musify.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ghost.musify.R
import org.ghost.musify.ui.models.BottomNavigationData
import org.ghost.musify.ui.navigation.NavScreen
import org.ghost.musify.ui.screens.BottomPlayer
import org.ghost.musify.viewModels.MainViewModel
import org.ghost.musify.viewModels.PlayerViewModel

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: NavScreen,
    onClick: (NavScreen) -> Unit
) {
    NavigationBar(
        modifier = modifier
    ) {
        val navigationItems = listOf(
            BottomNavigationData(R.string.home, Icons.Default.Home, NavScreen.Main.Home),
            BottomNavigationData(R.string.search, Icons.Default.Search, NavScreen.Main.Search),
            BottomNavigationData(
                R.string.history,
                Icons.Default.Refresh,
                NavScreen.Main.History
            ), // Suggestion: Icons.Default.History might fit better
            BottomNavigationData(R.string.settings, Icons.Default.Settings, NavScreen.Settings.Main)
        )

        NavigationBar(modifier = modifier) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    selected = item.route == currentRoute,
                    onClick = { onClick(item.route) },
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    label = { Text(text = stringResource(item.labelResId)) }
                )
            }
        }
    }

}

@Composable
fun MyBottomAppBar(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    currentScreen: NavScreen,
    onPlayerClick: () -> Unit = {},
    onNavigationItemClick: (NavScreen) -> Unit = {}

) {
    val uiState by playerViewModel.uiState.collectAsState()
//    BottomAppBar {
        Column{
            AnimatedVisibility(uiState.currentSong != null) {
                Column {
                    BottomPlayer(
                        modifier = modifier.clip(
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp
                            )
                        ),
                        playerUiState = uiState,
                        onClick = onPlayerClick,
                        onPlayPauseClick = {
                            playerViewModel.onPlayPauseClicked()
                        },
                        onCloseCLick = {

                        }
                    )
                    HorizontalDivider()
                }
            }
            AppNavigationBar(
                currentRoute = currentScreen,
                onClick = onNavigationItemClick
            )
//        }
    }
}

@Composable
fun PlayerBottomAppBar(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    onClick: () -> Unit,
){
    val uiState by playerViewModel.uiState.collectAsState()
    AnimatedVisibility(uiState.currentSong != null, modifier = modifier) {
        BottomAppBar {
            BottomPlayer(
                playerUiState = uiState,
                onClick = onClick,
                onPlayPauseClick = {
                    playerViewModel.onPlayPauseClicked()
                },
                onCloseCLick = { }
            )
        }
    }
}