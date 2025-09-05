package org.ghost.musify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.ghost.musify.R
import org.ghost.musify.ui.models.BottomNavigationData
import org.ghost.musify.ui.navigation.NavScreen

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
            BottomNavigationData(R.string.home, Icons.Default.Home, NavScreen.Home),
            BottomNavigationData(R.string.search, Icons.Default.Search, NavScreen.Search),
            BottomNavigationData(
                R.string.history,
                Icons.Default.Refresh,
                NavScreen.History
            ), // Suggestion: Icons.Default.History might fit better
            BottomNavigationData(R.string.settings, Icons.Default.Settings, NavScreen.Setting)
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