package com.example.core.navigation.module

import androidx.navigation3.runtime.NavKey
import com.example.core.navigation.model.NavigationState

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey, resetAtFront: Boolean = false) {
        if (route in state.backStacks.keys) {
            if (resetAtFront) {
                state.backStacks[route]?.clear()
                state.backStacks[route]?.add(route)
            }
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Back stack for ${state.topLevelRoute} does not exist")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}