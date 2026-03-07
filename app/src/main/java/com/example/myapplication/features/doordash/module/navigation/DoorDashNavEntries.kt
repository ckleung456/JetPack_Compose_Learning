package com.example.myapplication.features.doordash.module.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.module.Navigator
import com.example.core.navigation.model.Route

@Composable
fun EntryProviderScope<NavKey>.DoorDashNavEntries(
    navigator: Navigator
) {
    entry<Route.DoorDash> {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Doordash")
        }
    }
}