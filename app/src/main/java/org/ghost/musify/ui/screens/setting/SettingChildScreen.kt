package org.ghost.musify.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ghost.musify.ui.components.SearchableAppBar
import org.ghost.musify.ui.components.SearchableTopAppBar
import org.ghost.musify.ui.components.SettingsHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingChildScreen(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title)},
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) {innerPadding ->
        val innerModifier = Modifier.padding(innerPadding)
        Box(
            modifier = innerModifier
        ){
            content()
        }
    }

}