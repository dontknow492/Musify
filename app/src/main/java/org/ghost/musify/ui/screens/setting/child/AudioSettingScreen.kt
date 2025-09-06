package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.enums.AudioFocus
import org.ghost.musify.enums.HeadsetPlugAction
import org.ghost.musify.ui.components.SettingsCollapsibleEnumItem
import org.ghost.musify.ui.components.SettingsInputItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen
import org.ghost.musify.viewModels.SettingsViewModel

@Composable
fun AudioSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.settingsState.collectAsStateWithLifecycle()
    SettingChildScreen(
        modifier = modifier,
        title = "Audio & Playback",
        onBack  = onBackClick,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsSwitchItem(
                    title = "Crossfade",
                    description = "Enable or disable crossfade between songs",
                    checked = uiState.crossfadeEnabled,
                    searchQuery = "",
                    onCheckedChange = viewModel::setCrossfadeEnabled,
                    enabled = !uiState.useGaplessPlayback
                )
            }
            item {
                SettingsInputItem(
                    title = "Crossfade Duration",
                    description = "Set the duration of crossfade in seconds",
                    value = uiState.crossfadeDuration.toString(),
                    onValueChange = {
                        try{
                            viewModel.setCrossfadeDuration(it.toInt())
                        }catch(e: Exception) {
                            return@SettingsInputItem
                        }
                    },
                    searchQuery = "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    enabled = uiState.crossfadeEnabled && !uiState.useGaplessPlayback,
                    suffix = "sec"
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Use Gapless Playback",
                    description = "Enable or disable gapless playback",
                    checked = uiState.useGaplessPlayback,
                    searchQuery = "",
                    onCheckedChange = viewModel::setGaplessPlayback,
                    enabled = !uiState.crossfadeEnabled
                )
            }

            item {
                SettingsCollapsibleEnumItem(
                    title = "Audio focus",
                    description = "Set audio focus",
                    options = AudioFocus.entries.toList(),
                    currentSelection = uiState.audioFocusSetting,
                    onSelectionChange = viewModel::setAudioFocusSetting,
                    searchQuery = ""
                )
            }
            item {
                SettingsCollapsibleEnumItem(
                    title = "Headset plug action",
                    description = "Set the action to take when the headset is plugged in",
                    options = HeadsetPlugAction.entries.toList(),
                    currentSelection = uiState.headsetPlugAction,
                    onSelectionChange = viewModel::setHeadsetPlugAction,
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Bluetooth Autoplay",
                    description = "Set Bluetooth autoplay",
                    checked = uiState.bluetoothAutoplay,
                    searchQuery = "",
                    onCheckedChange = viewModel::setBluetoothAutoplay
                )
            }

        }
    }
}