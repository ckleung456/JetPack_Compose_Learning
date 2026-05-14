package com.example.core.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.ui.model.TopBarAction
import com.example.core.ui.model.TopBarConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TopBarStateManager {
    companion object {
        val LocalTopBarStateManager = compositionLocalOf { TopBarStateManager() }
    }

    private val _config = MutableStateFlow(
        TopBarConfig()
    )
    val config: StateFlow<TopBarConfig> = _config.asStateFlow()

    fun updateConfig(
        title: String? = null,
        navigationIcon: ImageVector? = null,
        navigationIconEnabled: Boolean? = null,
        onNavigationClick: (() -> Unit)? = null,
        actions: List<TopBarAction>? = null
    ) {
        _config.update {
            it.copy(
                title = title ?: it.title,
                navigationIcon = navigationIcon ?: it.navigationIcon,
                navigationIconEnabled = navigationIconEnabled ?: it.navigationIconEnabled,
                onNavigationClick = onNavigationClick ?: it.onNavigationClick,
                actions = actions ?: it.actions
            )
        }
    }

    fun resetToDefault(
        title: String = "",
    ) {
        _config.update { TopBarConfig(title = title) }
    }
}