package org.ghost.musify.ui.screens.setting.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.musify.enums.Theme
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen

@Composable
@Preview(showSystemUi = true)
fun GeneralSettingScreen(modifier: Modifier = Modifier) {
    SettingChildScreen(
        title = "General",
    ){
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item{
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                ) {
                    Theme.entries.forEachIndexed {index, theme ->
                        SegmentedButton(
                            onClick = {},
                            selected = theme == Theme.LIGHT,
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
                    checked = true,
                    searchQuery = "",
                    onCheckedChange = {}
                )
            }
        }

    }
}