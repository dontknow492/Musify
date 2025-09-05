package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.musify.ui.components.SettingsEditableListItem
import org.ghost.musify.ui.components.SettingsInputItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen

@Composable
@Preview(showSystemUi = true)
fun LibrarySettingScreen(modifier: Modifier = Modifier) {
    SettingChildScreen(
        modifier = modifier,
        title = "Library & Metadata",
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsEditableListItem(
                    title = "Music folders",
                    description = "folder to scan for musics",
                    items = listOf("Music", "Music2"),
                    onAddClick = {},
                    onItemDeleted = { },
                    searchQuery = "",
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Automatic scan",
                    description = "Enable or disable automatic scanning of musics onto device",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Download Album Art",
                    description = "Enable or disable automatic downloading of album art",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Prefer Embedded Art",
                    description = "Enable or disable using embedded album art",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Download on Wifi Only",
                    description = "Enable or disable downloading on Wi-Fi only",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Ignore Short Tracks",
                    description = "Enable or disable ignoring short tracks",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item {
                SettingsInputItem(
                    title = "Ignore Short Tracks Duration",
                    description = "Set the duration of short tracks in seconds",
                    value = "30",
                    onValueChange = {},
                    searchQuery = "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }


        }
    }
}