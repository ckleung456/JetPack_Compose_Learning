package com.example.core.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarConfig(
    val title: String = "",
    val navigationIcon: ImageVector = Icons.Default.ArrowBackIosNew,
    val navigationIconEnabled: Boolean = true,
    val onNavigationClick: (() -> Unit)? = null,
    val actions: List<TopBarAction> = emptyList()
)

data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)