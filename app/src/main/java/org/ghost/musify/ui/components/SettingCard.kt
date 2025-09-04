package org.ghost.musify.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * Creates an AnnotatedString that highlights occurrences of a search query.
 *
 * @param text The full text to display.
 * @param query The text to search for and highlight.
 * @param highlightColor The color to use for the highlight.
 * @return An AnnotatedString with the query highlighted.
 */
@Composable
fun highlightSearchQuery(
    text: String,
    query: String,
    highlightColor: Color = MaterialTheme.colorScheme.primaryContainer
): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }

    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex == -1) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        append(text.substring(0, startIndex))
        withStyle(style = SpanStyle(background = highlightColor)) {
            append(text.substring(startIndex, startIndex + query.length))
        }
        append(text.substring(startIndex + query.length))
    }
}

/**
 * A header for a section of settings.
 */
@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}


@Preview
@Composable
fun SettingsHeaderPreview() {
    SettingsHeader(title = "General")
}

@Composable
fun SettingsCategoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


/**
 * A setting item that the user can click, usually to navigate to another screen or open a dialog.
 */
@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    searchQuery: String = "", // <-- New parameter
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Use the helper to highlight the title
            Text(
                text = highlightSearchQuery(text = title, query = searchQuery),
                style = MaterialTheme.typography.bodyLarge
            )
            // Use the helper to highlight the description
            Text(
                text = highlightSearchQuery(text = description, query = searchQuery),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Preview
@Composable
fun SettingsClickableItemPreview() {
    SettingsClickableItem(
        title = "Account",
        description = "Manage your account settings",
        onClick = {},
        searchQuery = "ccount"
    )
}

/**
 * A setting item with a switch to toggle a boolean preference.
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    searchQuery: String = "", // <-- New parameter
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Use the helper to highlight the title
            Text(
                text = highlightSearchQuery(text = title, query = searchQuery),
                style = MaterialTheme.typography.bodyLarge
            )
            // Use the helper to highlight the description
            Text(
                text = highlightSearchQuery(text = description, query = searchQuery),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview
@Composable
fun SettingsSwitchItemPreview() {
    SettingsSwitchItem(
        title = "Dark Mode",
        description = "Enable dark mode for the app",
        checked = true,
        onCheckedChange = {},
        searchQuery = "ark"
    )
}


/**
 * A settings item that allows for text input.
 *
 * @param title The main title of the setting.
 * @param description A brief description of what the setting does.
 * @param value The current value of the input field.
 * @param onValueChange The callback that is triggered when the value changes.
 * @param searchQuery Optional search query to highlight in the title and description.
 * @param keyboardOptions Keyboard options to customize the input type (e.g., number, text).
 */
@Composable
fun SettingsInputItem(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: @Composable () -> Unit = {},
    searchQuery: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp), // Increased vertical padding for better spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Use the helper to highlight the title
            Text(
                text = highlightSearchQuery(text = title, query = searchQuery),
                style = MaterialTheme.typography.bodyLarge
            )
            // Use the helper to highlight the description
            Text(
                text = highlightSearchQuery(text = description, query = searchQuery),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))

        // Use BasicTextField for a clean, unstyled input area
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .width(80.dp) // Give the input field a consistent width
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    innerTextField()
                }
            }
        )
    }
}


/**
 * A generic, collapsible settings item for selecting an enum value.
 *
 * @param T The type of the enum.
 * @param title The main title of the setting.
 * @param description A brief description of what the setting does.
 * @param options A list of all available enum options to choose from.
 * @param currentSelection The currently selected enum value.
 * @param onSelectionChange The callback that is triggered when a new option is selected.
 * @param searchQuery Optional search query to highlight in the title and description.
 */
@Composable
fun <T : Enum<T>> SettingsCollapsibleEnumItem(
    title: String,
    description: String,
    options: List<T>,
    currentSelection: T,
    onSelectionChange: (T) -> Unit,
    icon: @Composable () -> Unit = {},
    searchQuery: String = "",
    forceExpanded: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded || forceExpanded) 180f else 0f,
    )

    Column {
        // The main, always-visible part of the card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = highlightSearchQuery(text = title, query = searchQuery),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = highlightSearchQuery(text = description, query = searchQuery),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))

            // Shows the current selection and the dropdown arrow
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentSelection.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        // The collapsible part with the list of options
        AnimatedVisibility(
            visible = isExpanded || forceExpanded,
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (option == currentSelection),
                                onClick = {
                                    onSelectionChange(option)
                                    isExpanded = false // Collapse after selection
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentSelection),
                            onClick = null // The Row's selectable modifier handles the click
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


/**
 * A collapsible settings item for managing a dynamic list of strings.
 * Provides callbacks for adding and deleting items.
 *
 * @param title The main title of the setting.
 * @param description A brief description of what the setting does.
 * @param items The current list of string items.
 * @param onAddClick A callback that is triggered when the "Add" button is clicked.
 * @param onItemDeleted The callback that is triggered when an item should be deleted.
 * @param searchQuery Optional search query to highlight in the title and description.
 */
@Composable
fun SettingsEditableListItem(
    title: String,
    description: String,
    items: List<String>,
    onAddClick: () -> Unit,
    onItemDeleted: (String) -> Unit,
    icon: @Composable () -> Unit = {},
    searchQuery: String = "",
    forceExpanded: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded || forceExpanded) 180f else 0f,
    )

    Column {
        // The main, always-visible part of the card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = highlightSearchQuery(text = title, query = searchQuery),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = highlightSearchQuery(text = description, query = searchQuery),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))

            // Shows a summary and the dropdown arrow
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${items.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        // The collapsible part with the list of items and an "Add" button
        AnimatedVisibility(visible = isExpanded || forceExpanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                items.forEach { item ->
                    DeletableItemRow(text = item, onDelete = { onItemDeleted(item) })
                }
                TextButton(
                    onClick = onAddClick, // The onClick now calls the provided lambda
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Item")
                }
            }
        }
    }
}


// (The helper composables DeletableItemRow and highlightSearchQuery remain the same)
@Composable
private fun DeletableItemRow(text: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

