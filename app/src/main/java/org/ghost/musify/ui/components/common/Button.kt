package org.ghost.musify.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.ghost.musify.R

@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    onClick: () -> Unit,
    isPlaying: Boolean,
    iconSize: Dp = 60.dp,
){
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enable // Disable button when buffering
    ) {
        Icon(
            painter = if (isPlaying)
                painterResource(R.drawable.baseline_pause_24)
            else
                painterResource(R.drawable.baseline_play_arrow_24),
            contentDescription = "play/pause",
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun VolumeControlButton(
    volumeLevel: Float = 0.5f,
    onVolumeChange: (Float) -> Unit = {}
) {
    // 1. State to control the visibility of the popup.
    var showPopup by remember { mutableStateOf(false) }

    // State for the slider's value.

    // A Box is used to anchor the Popup to the Button.
    Box {
        // The button that triggers the popup.
        IconButton(onClick = { showPopup = true }) {
            when (volumeLevel) {
                0f -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_off_24),
                        contentDescription = "volume off"
                    )
                }

                in 0f..0.5f -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_down_24),
                        contentDescription = "volume medium"
                    )
                }

                else -> {
                    Icon(
                        painterResource(R.drawable.rounded_volume_up_24),
                        contentDescription = "Set Volume"
                    )
                }
            }
        }

        // The Popup is displayed conditionally based on the state.
        if (showPopup) {
            // 2. The Popup composable.
            Popup(
                // Position the popup right above the button.
                alignment = Alignment.TopCenter,
                // The lambda to execute when the user clicks outside the popup.
                onDismissRequest = { showPopup = false },
                // Optional: properties to control focus, etc.
                properties = PopupProperties(focusable = true)
            ) {
                // 3. The content of the popup.
                Card(
                    modifier = Modifier.padding(bottom = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Master Volume", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = volumeLevel,
                            onValueChange = onVolumeChange
                        )
                        Text(text = "${(volumeLevel * 100).toInt()}%")
                    }
                }
            }
        }
    }
}