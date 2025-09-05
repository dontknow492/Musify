package org.ghost.musify.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// Defines the distinct steps in the onboarding flow.
private enum class OnboardingStep {
    Welcome,
    Permissions,
    Finish
}

/**
 * The main container for the entire onboarding experience.
 *
 * This composable manages the state of the onboarding flow, including the current step
 * and progress. It displays the appropriate screen for each step.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onOnboardingComplete A callback invoked when the user finishes the last step,
 * signaling that the app should navigate to its main content.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onOnboardingComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.Welcome) }

    // Calculate progress based on the enum's ordinal value.
    val totalSteps = OnboardingStep.values().size
    val progressTarget = (currentStep.ordinal + 1) / totalSteps.toFloat()
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        label = "Onboarding Progress"
    )

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated progress bar at the top.
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
            )

            // AnimatedContent provides smooth transitions between screens.
            AnimatedContent(
                targetState = currentStep,
                label = "Onboarding Screen Transition",
                transitionSpec = {
                    // Define a slide-in and slide-out animation.
                    slideInHorizontally { fullWidth -> fullWidth } + fadeIn() togetherWith
                            slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
                }
            ) { step ->
                // The content of the screen changes based on the current step.
                when (step) {
                    OnboardingStep.Welcome -> WelcomeScreen(
                        onNext = { currentStep = OnboardingStep.Permissions }
                    )

                    OnboardingStep.Permissions -> PermissionScreen(
                        onNext = { currentStep = OnboardingStep.Finish }
                    )

                    OnboardingStep.Finish -> FinishScreen(
                        onFinish = onOnboardingComplete
                    )
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onOnboardingComplete = {})
}
