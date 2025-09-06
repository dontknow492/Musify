package org.ghost.musify.ui.screens.setting

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.ghost.musify.ui.components.MyBottomAppBar
import org.ghost.musify.ui.components.SettingsCategoryItem
import org.ghost.musify.ui.navigation.NavScreen
import org.ghost.musify.viewModels.PlayerViewModel
import org.ghost.musify.viewModels.SettingsViewModel

//import org.ghost.musify.ui.navigation.NavScreen.Settings

data class SettingCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: NavScreen.Settings // The navigation route for the detail screen
)

// master screen where main setting will display
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onNavigationItemClick: (NavScreen) -> Unit,
    onBottomPlayerClick: () -> Unit,
//    navController: NavController
) {
    val categories = listOf(
        SettingCategory(
            "General",
            "Theme, color, language",
            Icons.Default.Home,
            NavScreen.Settings.General
        ),
        SettingCategory(
            "Audio & Playback",
            "Equalizer, crossfade, focus",
            Icons.Default.Build,
            NavScreen.Settings.Audio
        ),
        SettingCategory(
            "Library & Metadata",
            "Folders, scanning, album art",
            Icons.Default.Info,
            NavScreen.Settings.Library
        ),
        SettingCategory(
            "Notifications & Widgets",
            "Appearance and behavior",
            Icons.Default.Notifications,
            NavScreen.Settings.Notifications
        ),
        SettingCategory(
            "Advanced",
            "Exclusions, backup & restore",
            Icons.Default.Settings,
            NavScreen.Settings.Advanced
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        bottomBar = {
            MyBottomAppBar(
                playerViewModel = playerViewModel,
                currentScreen = NavScreen.Settings.Main,
                onPlayerClick = onBottomPlayerClick,
                onNavigationItemClick = onNavigationItemClick
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(categories) { category ->
                SettingsCategoryItem(
                    icon = category.icon,
                    title = category.title,
                    subtitle = category.subtitle,
                    onClick = {
                        // Navigate to the specific detail screen when clicked
                        navController.navigate(category.route)
                    }
                )
            }
        }
    }
}