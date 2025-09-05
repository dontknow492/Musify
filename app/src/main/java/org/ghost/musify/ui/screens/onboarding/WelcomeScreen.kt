package org.ghost.musify.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ghost.musify.R

/**
 * The first screen a user sees, welcoming them to the app.
 * It features an illustration, a clear value proposition, and a call-to-action button.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onNext A callback lambda to be invoked when the user clicks the "Get Started" button,
 * which should navigate them to the next step of the onboarding or the main app.
 */
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onNext: () -> Unit
) {
    // The main Column arranges content vertically and centers it.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Provides consistent padding around the screen edges.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spacer at the top to push content down from the status bar.
        Spacer(modifier = Modifier.weight(0.5f))

        // 1. Illustration Section
        //
        AsyncImage(
            model = R.drawable.welcome, // Your welcome illustration resource.
            contentDescription = "Welcome Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f), // Takes up a significant portion of the vertical space.
            contentScale = ContentScale.Fit // Ensures the entire illustration is visible without distortion.
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Text Content Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f) // Gives text content flexible space.
        ) {
            Text(
                text = "Welcome to Musify",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your ultimate music companion. Discover new tracks, create playlists, and enjoy seamless playback.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Use a secondary color for body text.
            )
        }

        // 3. Action Button Section
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Adds horizontal padding to the button itself.
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                "Get Started",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Provides some space at the very bottom, below the button.
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    // Assuming you have a theme set up for your app, like MusifyTheme { ... }
    WelcomeScreen(onNext = {})
}
