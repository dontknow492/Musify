package org.ghost.musify.ui.screens.models

import androidx.annotation.DrawableRes

data class Tab(
    val title: String,
    @param:DrawableRes val icon: Int? = null, // drawable id
)
