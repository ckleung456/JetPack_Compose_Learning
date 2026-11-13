package com.example.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The real Material 3 search field (the pill-shaped input used by [androidx.compose.material3.SearchBar] /
 * [androidx.compose.material3.DockedSearchBar]), used here as a standalone, always-collapsed
 * search row - there's no full-screen/docked suggestions surface, since the filtered list is
 * already rendered inline by the caller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    placeholder: String
) {
    SearchBarDefaults.InputField(
        modifier = modifier.fillMaxWidth(),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onQueryChange,
        expanded = false,
        onExpandedChange = {},
        placeholder = { SmartAutoSizeText(modifier = Modifier.fillMaxWidth(), placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}

@Composable
fun SearchBarWithLoading(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    placeholder: String,
    isLoading: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { SmartAutoSizeText(modifier = modifier.fillMaxWidth(), placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                query.isNotEmpty() -> {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun AnimatedItem(
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 500)
        ) + expandVertically(
            animationSpec = tween(durationMillis = 500)
        ),
        exit = fadeOut()
    ) {
        content()
    }
}