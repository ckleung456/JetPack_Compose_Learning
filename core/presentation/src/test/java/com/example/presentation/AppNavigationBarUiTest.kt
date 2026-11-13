package com.example.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.presentation.navigation.model.BottomNavItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Robolectric tests for [AppNavigationBar]: the highlighted tab is exactly the one matching `selectedKey`. */
@RunWith(AndroidJUnit4::class)
class AppNavigationBarUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private object Countries : NavKey
    private object Doordash : NavKey
    private object Business : NavKey

    private val items = linkedMapOf<NavKey, BottomNavItem>(
        Countries to BottomNavItem(icon = Icons.Filled.Home, title = "Countries"),
        Doordash to BottomNavItem(icon = Icons.Filled.Star, title = "Doordash"),
        Business to BottomNavItem(icon = Icons.Filled.Search, title = "Business")
    )

    // Material 3 clears the icon's content description when a label is shown, so match tabs by label.
    private fun tab(title: String) = composeRule.onNode(hasText(title) and isSelectable())

    private fun assertOnlySelected(title: String) = items.values.forEach { item ->
        if (item.title == title) tab(item.title).assertIsSelected() else tab(item.title).assertIsNotSelected()
    }

    @Test
    fun highlightsOnlyTheSelectedKey() {
        composeRule.setContent { AppNavigationBar(items = items, selectedKey = Business, onSelectKey = {}) }

        assertOnlySelected("Business")
    }

    @Test
    fun highlightMovesWhenSelectedKeyChanges() {
        // Mirrors AppFeatureNavigation, which passes navigationState.topLevelRoute (a snapshot state).
        var selected: NavKey by mutableStateOf(Countries)
        composeRule.setContent {
            AppNavigationBar(items = items, selectedKey = selected, onSelectKey = { selected = it })
        }
        assertOnlySelected("Countries")

        tab("Doordash").performClick()
        assertOnlySelected("Doordash")

        tab("Business").performClick()
        assertOnlySelected("Business")

        selected = Countries // e.g. system back from a tab root
        assertOnlySelected("Countries")
    }

    @Test
    fun clickingATabReportsItsKey() {
        val clicked = mutableListOf<NavKey>()
        composeRule.setContent { AppNavigationBar(items = items, selectedKey = Countries, onSelectKey = { clicked += it }) }

        tab("Business").performClick()
        tab("Doordash").performClick()

        assertEquals(listOf(Business, Doordash), clicked)
    }
}
