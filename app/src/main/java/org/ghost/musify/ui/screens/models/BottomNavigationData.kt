package org.ghost.musify.ui.screens.models

import androidx.compose.ui.graphics.vector.ImageVector
import org.ghost.musify.navigation.NavScreen

data class BottomNavigationData(
    val labelResId: Int,
    val icon: ImageVector,
    val route: NavScreen
)