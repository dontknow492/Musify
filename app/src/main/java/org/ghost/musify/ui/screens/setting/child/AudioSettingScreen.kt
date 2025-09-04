package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction
import org.ghost.musify.ui.components.SettingsCollapsibleEnumItem
import org.ghost.musify.ui.components.SettingsInputItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen

@Composable
fun AudioSettingScreen(modifier: Modifier = Modifier) {
    SettingChildScreen(
        title = "Audio & Playback",
    ){
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item{
                SettingsSwitchItem(
                    title = "Crossfade",
                    description = "Enable or disable crossfade between songs",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
            item{
                SettingsInputItem(
                    title = "Crossfade Duration",
                    description = "Set the duration of crossfade in seconds",
                    value = "5",
                    onValueChange = {},
                    searchQuery = "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
            item{
                SettingsSwitchItem(
                    title = "Use Gapless Playback",
                    description = "Enable or disable gapless playback",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }

            item {
                SettingsCollapsibleEnumItem(
                    title = "Audio focus",
                    description = "Set audio focus",
                    options = AudioFocus.entries.toList(),
                    currentSelection = AudioFocus.PAUSE_ON_INTERRUPTION,
                    onSelectionChange = {},
                    searchQuery = ""
                )
            }
            item{
                SettingsCollapsibleEnumItem(
                    title = "Headset plug action",
                    description = "Set the action to take when the headset is plugged in",
                    options = HeadsetPlugAction.entries.toList(),
                    currentSelection = HeadsetPlugAction.RESUME_PLAYBACK,
                    onSelectionChange = {},
                )
            }
            item{
                SettingsSwitchItem(
                    title = "Bluetooth Autoplay",
                    description = "Set Bluetooth autoplay",
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }

        }
    }
}