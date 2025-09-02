package org.ghost.musify

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import org.ghost.musify.navigation.AppNavigation
import org.ghost.musify.navigation.NavScreen
import org.ghost.musify.ui.theme.MusifyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            navController = rememberNavController()
            MusifyTheme(
                dynamicColor = false
            ) {
//                val start = NavScreen.AlbumSongs(1312244804222984308L)
                val start = NavScreen.Home
                RequestAudioPermission {
                    AppNavigation(
                        navController = navController,
                        startDestination = start
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAudioPermission(
    // This is a composable lambda that will be displayed when permission is granted.
    // You can pass your main screen (e.g., SongListScreen()) here.
    content: @Composable () -> Unit
) {
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
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
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Request Permissions")
            }
        }
    }

    // 1. Determine the correct permission based on the Android version

}