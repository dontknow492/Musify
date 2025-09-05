package org.ghost.musify.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.*
import org.ghost.musify.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview(showSystemUi = true)
fun PermissionScreen(modifier: Modifier = Modifier, onNext: () -> Unit = {}) {
    // 1. Define permissions based on Android version
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // 2. Remember the permission state
    val permissionState = rememberMultiplePermissionsState(permissionsToRequest)
    val allPermissionsGranted = permissionState.allPermissionsGranted
    val context = LocalContext.current

    // 3. Main layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Informational Header
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This app needs the following permissions to function correctly. Please grant them.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
//        Spacer(modifier = Modifier.height(24.dp))

        // Image Placeholder 🖼️
        AsyncImage(
            model = R.drawable.headphone_tilt, // Your welcome illustration resource.
            contentDescription = "Welcome Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f), // Takes up a significant portion of the vertical space.
            contentScale = ContentScale.Fit // Ensures the entire illustration is visible without distortion.
        )
        //

//        Spacer(modifier = Modifier.height(24.dp))

        // List of permissions
        permissionState.permissions.forEach { permState ->
            PermissionItem(permissionState = permState)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button 🚀
        val buttonText = if (allPermissionsGranted) {
            "Permissions Granted!"
        } else if (permissionState.shouldShowRationale) {
            "Please Grant Permissions"
        } else {
            "Grant Permissions"
        }

        Button(
            onClick = {
                if (!allPermissionsGranted) {
                    // If rationale is needed or permission is permanently denied,
                    // guide user to settings. Otherwise, just launch the request.
                    if (permissionState.shouldShowRationale || !permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    } else {
                        // Guide user to app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                }
            },
            enabled = !allPermissionsGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }

        AnimatedVisibility(allPermissionsGranted) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionItem(
    modifier: Modifier = Modifier,
    permissionState: PermissionState
) {
    val permissionData = remember(permissionState.permission) {
        when (permissionState.permission) {
            Manifest.permission.READ_MEDIA_AUDIO -> PermissionData("Read Audio", "To play and manage your music files.")
            Manifest.permission.READ_MEDIA_IMAGES -> PermissionData("Read Images", "To display your photos and videos.")
            Manifest.permission.POST_NOTIFICATIONS -> PermissionData("Send Notifications", "To keep you updated with important alerts.")
            Manifest.permission.READ_EXTERNAL_STORAGE -> PermissionData("Read Storage", "To access files on your device.")
            else -> PermissionData("Unknown Permission", "An unknown permission is required.")
        }
    }

    val isGranted = permissionState.status.isGranted
    val icon = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning
    val color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isGranted) "Granted" else "Denied",
                tint = color
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permissionData.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = permissionData.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class PermissionData(val title: String, val description: String)