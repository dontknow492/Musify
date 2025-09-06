package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.enums.StartScreen
import org.ghost.musify.enums.Theme
import org.ghost.musify.ui.components.SettingsCollapsibleEnumItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen
import org.ghost.musify.viewModels.SettingsViewModel

@Composable
fun GeneralSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.settingsState.collectAsStateWithLifecycle()
    SettingChildScreen(
        modifier = modifier,
        title = "General",
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                ) {
                    Theme.entries.forEachIndexed { index, theme ->
                        SegmentedButton(
                            onClick = { viewModel.setTheme(theme)},
                            selected = theme == uiState.theme,
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = Theme.entries.size
                            ),
                            label = { Text(theme.name, maxLines = 1) }
                        )
                    }
                }
            }
            item {
                SettingsSwitchItem(
                    title = "Use Material You",
                    description = "Enable or disable Material You colors",
                    checked = uiState.useMaterialYou,
                    searchQuery = "",
                    onCheckedChange = viewModel::setUseMaterialYou
                )
            }
            item {
                SettingsCollapsibleEnumItem(
                    title = "Start Screen",
                    description = "Set the screen to start on",
                    options = StartScreen.entries,
                    currentSelection = uiState.startScreen,
                    onSelectionChange = viewModel::setStartScreen,
                    searchQuery = ""
                )
            }
        }

    }
}