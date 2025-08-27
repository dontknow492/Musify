package org.ghost.musify.ui.screens.permission

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAudioPermission(
    // This is a composable lambda that will be displayed when permission is granted.
    // You can pass your main screen (e.g., SongListScreen()) here.
    content: @Composable () -> Unit,
    permission: String = Manifest.permission.READ_MEDIA_AUDIO,
) {
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // On older versions, POST_NOTIFICATIONS is not needed
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissionsToRequest)
    if (permissionState.allPermissionsGranted) {
        // If permission is granted, show the main content
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // A list of permissions that have not been granted
            val revokedPermissions = permissionState.revokedPermissions

            // 4. Provide specific rationale for each denied permission
            // This provides a much better user experience
            revokedPermissions.forEach { permission ->
                val rationaleText = when (permission.permission) {
                    Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE ->
                        "To play music, the app needs to access your audio files. Please grant the permission."

                    Manifest.permission.POST_NOTIFICATIONS ->
                        "To show playback controls when the app is in the background, the app needs to send notifications."

                    else ->
                        "This permission is required for the app to function correctly."
                }
                Text(
                    text = rationaleText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // 5. The button to trigger the permission request
            Button(onClick = { }) {
                Text("Request Permissions")
            }
        }
    }

    // 1. Determine the correct permission based on the Android version

}