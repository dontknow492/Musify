package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ghost.musify.enums.NotificationStyle
import org.ghost.musify.ui.components.SettingsCollapsibleEnumItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen

@Composable
fun NotificationSettingScreen(modifier: Modifier = Modifier) {
    SettingChildScreen(
        modifier = modifier,
        title = "Notification",
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsCollapsibleEnumItem(
                    title = "Notification Style",
                    description = "Choose the style of notifications",
                    options = NotificationStyle.entries.toList(),
                    currentSelection = NotificationStyle.STANDARD,
                    onSelectionChange = {},
                    searchQuery = ""
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Use Colorized Notification",
                    description = "Enable or disable colorized notifications",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Show SeekBar in Notification",
                    description = "Enable or disable showing the seekbar in notifications",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }

        }
    }
}