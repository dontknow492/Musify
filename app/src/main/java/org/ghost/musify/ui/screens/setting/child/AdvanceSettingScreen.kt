package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ghost.musify.ui.components.SettingsClickableItem
import org.ghost.musify.ui.components.SettingsEditableListItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen

@Composable
fun AdvanceSettingScreen(modifier: Modifier = Modifier) {
    SettingChildScreen(
        modifier = modifier,
        title = "General",
    ) {
        LazyColumn {
            item {
                SettingsEditableListItem(
                    title = "Excluded Folders",
                    description = "folder that are not scanned for musics.",
                    items = listOf("Music", "Music2"),
                    onAddClick = {},
                    onItemDeleted = { },
                    searchQuery = "",
                    forceExpanded = true
                )
            }
            item {
                SettingsClickableItem(
                    title = "Backup",
                    description = "Backup your library",
                    onClick = {},
                    searchQuery = ""
                )
            }
            item {
                SettingsClickableItem(
                    title = "Restore",
                    description = "Restore a backup",
                    onClick = {},
                    searchQuery = ""
                )
            }
        }
    }
}