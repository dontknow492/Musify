package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.enums.NotificationStyle
import org.ghost.musify.ui.components.SettingsCollapsibleEnumItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen
import org.ghost.musify.viewModels.SettingsViewModel

@Composable
fun NotificationSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.settingsState.collectAsStateWithLifecycle()
    SettingChildScreen(
        modifier = modifier,
        title = "Notification",
        onBack = onBackClick,
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
                    checked = uiState.useColorizedNotification,
                    searchQuery = "",
                    onCheckedChange = viewModel::setUseColorizedNotification
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Show SeekBar in Notification",
                    description = "Enable or disable showing the seekbar in notifications",
                    checked = uiState.showSeekBarInNotification,
                    searchQuery = "",
                    onCheckedChange = viewModel::setShowSeekBarInNotification
                )
            }

        }
    }
}