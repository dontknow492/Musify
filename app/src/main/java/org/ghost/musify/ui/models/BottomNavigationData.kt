package org.ghost.musify.ui.models

import androidx.compose.ui.graphics.vector.ImageVector
import org.ghost.musify.ui.navigation.NavScreen

data class BottomNavigationData(
    val labelResId: Int,
    val icon: ImageVector,
    val route: NavScreen
)