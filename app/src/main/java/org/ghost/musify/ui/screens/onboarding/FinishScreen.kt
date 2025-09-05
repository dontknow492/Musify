package org.ghost.musify.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ghost.musify.R

/**
 * A composable screen that signifies the end of the onboarding process.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onFinish A callback lambda to be invoked when the user clicks the "Start Listening" button.
 * This should typically handle navigation to the main part of the app.
 */
@Composable
fun FinishScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Visual Element (Placeholder)
        // TODO: Replace this Box with an actual Image or an animation (like Lottie)
        // for a more engaging experience.
        //
        AsyncImage(
            model = R.drawable.headphone_normal, // Your welcome illustration resource.
            contentDescription = "Welcome Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f), // Takes up a significant portion of the vertical space.
            contentScale = ContentScale.Fit // Ensures the entire illustration is visible without distortion.
        )


        // 2. Header Text
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Body Text
        Text(
            text = "Your personalized music experience is ready. Let's dive into a world of sound.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant // A slightly muted color for hierarchy
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Call-to-Action Button
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Start Listening",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
private fun FinishScreenPreview() {
    // Assuming you have a theme set up for your app
    // MusifyTheme {
    FinishScreen(onFinish = {})
    // }
}
