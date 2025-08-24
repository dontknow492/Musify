package org.ghost.musify

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.ghost.musify.ui.theme.MusicRepository
import org.ghost.musify.ui.theme.MusifyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var _songs = MutableStateFlow<List<Song>>(emptyList())
            // A public StateFlow to expose the list to the UI, making it read-only.
            _songs

            val songAll = MusicRepository(this).getAllSongs()

            MusifyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        RequestAudioPermission {
                            Greeting(
                                name = "Android: ${songAll.size} songs found",
                                modifier = Modifier.padding(innerPadding)
                            )
                            songAll.forEach { song ->
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = song.duration.toString(),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = song.albumArtUri.toString(),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = song.id.toString(),
                                    style = MaterialTheme.typography.headlineMedium
                                )

                            }
                        }
                    }
                    // Main content goes here, e.g., SongListScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusifyTheme {
        Greeting("Android")
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
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Request Permissions")
            }
        }
    }

    // 1. Determine the correct permission based on the Android version

}