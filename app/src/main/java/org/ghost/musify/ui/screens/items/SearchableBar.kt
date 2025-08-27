package org.ghost.musify.ui.screens.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.musify.R

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableTopAppBar(
    modifier: Modifier = Modifier,
    search: String = "",
    title: String? = null,
    onBackClick: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        title = {
            if (!isSearchVisible) {
                Text(
                    title ?: "Songs",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(34.dp)
                )
            }
        },
        actions = {
            when (isSearchVisible) {
                true -> {
                    TextField(
                        value = search,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (search.isNotEmpty()) {
                                        onSearchChange("")
                                    } else {
                                        isSearchVisible = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 60.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent

                        )
                    )
                }

                false -> {
                    IconButton(
                        onClick = {
                            isSearchVisible = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

            }
            IconButton(
                onClick = onFilterClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_filter_list_24),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    )
}