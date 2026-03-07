package com.example.core.ui

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.BottomNavItem

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    items: Map<NavKey, BottomNavItem>,
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit
) {
    BottomAppBar(
        modifier = modifier
    ) {
        items.forEach { (topLevelDestination, data)->
            NavigationBarItem(
                selected = topLevelDestination == selectedKey,
                onClick = {
                    onSelectKey(topLevelDestination)
                },
                icon = {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = data.title
                    )
                },
                label = {
                    Text(text = data.title)
                }
            )
        }
    }
}