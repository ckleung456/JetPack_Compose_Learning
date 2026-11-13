package com.example.presentation.navigation.module

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.presentation.navigation.model.NavigationState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bottom bar highlights `NavigationState.topLevelRoute`, so these tests pin down how [Navigator]
 * moves it: tab taps and back-from-root change it, pushing a detail screen does not.
 */
class NavigatorTest {

    private object Countries : NavKey
    private object Doordash : NavKey
    private object Business : NavKey
    private data class Detail(val id: String) : NavKey

    private val state = NavigationState(
        startRoute = Countries,
        topLevelRoute = mutableStateOf(Countries),
        backStacks = listOf(Countries, Doordash, Business).associateWith { NavBackStack(it) }
    )
    private val navigator = Navigator(state)

    @Test
    fun `starts on the start route`() {
        assertEquals(Countries, state.topLevelRoute)
        assertEquals(listOf(Countries), state.stacksInUse)
    }

    @Test
    fun `navigating to a tab makes it the top level route`() {
        navigator.navigate(Business)
        assertEquals(Business, state.topLevelRoute)

        navigator.navigate(Doordash)
        assertEquals(Doordash, state.topLevelRoute)
    }

    @Test
    fun `stacksInUse keeps the start route first so first() is never the current tab`() {
        navigator.navigate(Business)

        // This ordering is why `stacksInUse.first()` always highlighted Countries.
        assertEquals(listOf(Countries, Business), state.stacksInUse)
        assertEquals(state.topLevelRoute, state.stacksInUse.last())
    }

    @Test
    fun `opening a detail screen keeps the current tab`() {
        navigator.navigate(Business)
        navigator.navigate(Detail("abc"))

        assertEquals(Business, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Business, Detail("abc")), state.backStacks.getValue(Business).toList())
    }

    @Test
    fun `back from a detail screen pops it and stays on the tab`() {
        navigator.navigate(Business)
        navigator.navigate(Detail("abc"))

        navigator.goBack()

        assertEquals(Business, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Business), state.backStacks.getValue(Business).toList())
    }

    @Test
    fun `back from a tab root returns to the start route`() {
        navigator.navigate(Business)

        navigator.goBack()

        assertEquals(Countries, state.topLevelRoute)
        assertEquals(listOf(Countries), state.stacksInUse)
    }

    @Test
    fun `switching tabs keeps each tab's own back stack`() {
        navigator.navigate(Business)
        navigator.navigate(Detail("abc"))
        navigator.navigate(Doordash)
        navigator.navigate(Business)

        assertEquals(Business, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Business, Detail("abc")), state.backStacks.getValue(Business).toList())
    }

    @Test
    fun `resetAtFront clears the tab's stack`() {
        navigator.navigate(Business)
        navigator.navigate(Detail("abc"))
        navigator.navigate(Doordash)

        navigator.navigate(Business, resetAtFront = true)

        assertEquals(Business, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Business), state.backStacks.getValue(Business).toList())
    }
}
