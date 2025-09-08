package org.ghost.musify.ui.screens.setting.child

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ghost.musify.ui.components.SettingsClickableItem
import org.ghost.musify.ui.components.SettingsEditableListItem
import org.ghost.musify.ui.screens.setting.SettingChildScreen
import org.ghost.musify.viewModels.SettingsViewModel

@Composable
fun AdvanceSettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {

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
                    viewModel.addExcludedFolder(path.split(":")[1])
                }
            }
        }
    )

    val backupFolderPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocumentTree(),
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                // 2. Take persistent permissions to access the folder later

                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)

                viewModel.backupSettings(it)
            }
        }
    )

    val restoreFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // 2. Take persistent permissions to access the folder later
                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)
                viewModel.restoreSettings(it)
            }
        }
    )

    val uiState by viewModel.settingsState.collectAsStateWithLifecycle()

    SettingChildScreen(
        modifier = modifier,
        title = "General",
        onBack = onBackClick,
    ) {
        LazyColumn {
            item {
                SettingsEditableListItem(
                    title = "Excluded Folders",
                    description = "folder that are not scanned for musics.",
                    items = uiState.excludedFolders.toList(),
                    onAddClick = { folderPickerLauncher.launch(null) },
                    onItemDeleted = {
                        viewModel.removeExcludedFolder(it)
                    },
                    searchQuery = "",
                    forceExpanded = true
                )
            }
            item {
                SettingsClickableItem(
                    title = "Backup",
                    description = "Backup your library",
                    onClick = { backupFolderPickerLauncher.launch("musify_settings_backup.pb") },
                    searchQuery = ""
                )
            }
            item {
                SettingsClickableItem(
                    title = "Restore",
                    description = "Restore a backup",
                    onClick = { restoreFolderPickerLauncher.launch(arrayOf("application/octet-stream")) },
                    searchQuery = ""
                )
            }
        }
    }
}