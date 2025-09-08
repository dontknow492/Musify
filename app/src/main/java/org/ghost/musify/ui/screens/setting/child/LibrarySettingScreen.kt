package org.ghost.musify.ui.screens.setting.child

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.ui.components.SettingsEditableListItem
import org.ghost.musify.ui.components.SettingsInputItem
import org.ghost.musify.ui.components.SettingsSwitchItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen
import org.ghost.musify.viewModels.SettingsViewModel

@Composable
fun LibrarySettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                // 2. Take persistent permissions to access the folder later
                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)

                uri.path?.let{path ->

                    viewModel.addMusicFolder(path.split(":")[1])
                }
            }
        }
    )

    SettingChildScreen(
        modifier = modifier,
        title = "Library & Metadata",
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsEditableListItem(
                    title = "Music folders",
                    description = "folder to scan for musics",
                    items = uiState.musicFolders.toList(),
                    onAddClick = {folderPickerLauncher.launch(null)},
                    onItemDeleted = {
                        viewModel.removeMusicFolder(it)
                    },
                    searchQuery = "",
                )
            }
            item {
                SettingsInputItem(
                    title = "Artist name separator",
                    description = "character to separate artist names",
                    value = uiState.artistNameSeparator,
                    onValueChange = viewModel::setArtistNameSeparator,
                    searchQuery = "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    )
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Automatic scan",
                    description = "Enable or disable automatic scanning of musics onto device",
                    checked = uiState.automaticScanning,
                    searchQuery = "",
                    onCheckedChange = viewModel::setAutomaticScanning
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Download Album Art",
                    description = "Enable or disable automatic downloading of album art",
                    checked = uiState.downloadAlbumArt,
                    searchQuery = "",
                    onCheckedChange = viewModel::setDownloadAlbumArt
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Download on Wifi Only",
                    description = "Enable or disable downloading on Wi-Fi only",
                    checked = uiState.downloadOnWifiOnly,
                    searchQuery = "",
                    onCheckedChange = viewModel::setDownloadOnWifiOnly,
                    enabled = uiState.downloadAlbumArt
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Prefer Embedded Art",
                    description = "Enable or disable using embedded album art",
                    checked = uiState.preferEmbeddedArt,
                    searchQuery = "",
                    onCheckedChange = viewModel::setPreferEmbeddedArt
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Ignore Short Tracks",
                    description = "Enable or disable ignoring short tracks",
                    checked = uiState.ignoreShortTracks,
                    searchQuery = "",
                    onCheckedChange = viewModel::setIgnoreShortTracks
                )
            }
            item {
                SettingsInputItem(
                    title = "Ignore Short Tracks Duration",
                    description = "Set the duration of short tracks in seconds",
                    value = uiState.ignoreShortTracksDuration.toString(),
                    onValueChange = {
                        try{
                            viewModel.setIgnoreShortTracksDuration(it.toInt())
                        }catch (e: NumberFormatException){
                            return@SettingsInputItem
                        }
                    },
                    searchQuery = "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    enabled = uiState.ignoreShortTracks,
                    suffix = "sec"
                )
            }


        }
    }
}