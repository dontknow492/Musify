package org.ghost.musify.ui.models

import androidx.annotation.DrawableRes

data class Tab(
    val title: String,
    @param:DrawableRes val icon: Int? = null, // drawable id
)
