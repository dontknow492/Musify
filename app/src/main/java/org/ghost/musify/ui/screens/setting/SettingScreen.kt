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
import androidx.navigation.NavHostController
import org.ghost.musify.ui.components.SettingsCategoryItem
import org.ghost.musify.ui.navigation.SettingScreen

data class SettingCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: SettingScreen // The navigation route for the detail screen
)

// master screen where main setting will display
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
//    navController: NavController
) {
    val categories = listOf(
        SettingCategory(
            "General",
            "Theme, color, language",
            Icons.Default.Home,
            SettingScreen.GeneralSettings
        ),
        SettingCategory(
            "Audio & Playback",
            "Equalizer, crossfade, focus",
            Icons.Default.Build,
            SettingScreen.AudioSettings
        ),
        SettingCategory(
            "Library & Metadata",
            "Folders, scanning, album art",
            Icons.Default.Info,
            SettingScreen.LibrarySettings
        ),
        SettingCategory(
            "Notifications & Widgets",
            "Appearance and behavior",
            Icons.Default.Notifications,
            SettingScreen.NotificationsSettings
        ),
        SettingCategory(
            "Advanced",
            "Exclusions, backup & restore",
            Icons.Default.Settings,
            SettingScreen.AdvancedSettings
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Settings") })
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