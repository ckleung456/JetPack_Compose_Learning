package com.example.myapplication

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.ui.components.BusinessTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke test of the real app on a device/emulator: launches [MainActivity] with the real Hilt
 * graph and drives the bottom navigation. Screen data comes from the real backends, so assertions only
 * check navigation chrome, never list contents.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun navItem(title: String) = composeRule.onNode(hasText(title) and isSelectable())

    private fun waitFor(matcher: SemanticsMatcher, count: Int = 1) = composeRule.waitUntil(timeoutMillis = 10_000) {
        composeRule.onAllNodes(matcher).fetchSemanticsNodes().size == count
    }

    /** A screen is showing when its title is in the top bar as well as in the bottom-bar label. */
    private fun waitForScreen(title: String) = waitFor(hasText(title), count = 2)

    @Test
    fun launchesOnCountriesWithAllTabs() {
        assertEquals(Lifecycle.State.RESUMED, composeRule.activityRule.scenario.state)

        waitForScreen("Countries")
        navItem("Countries").assertExists()
        navItem("Doordash").assertExists()
        navItem("Business").assertExists()
    }

    @Test
    fun bottomNavigationSwitchesScreens() {
        navItem("Doordash").performClick()
        waitForScreen("Doordash")

        navItem("Business").performClick()
        waitForScreen("Business")
        waitFor(hasTestTag(BusinessTestTags.SEARCH_FIELD))

        navItem("Countries").performClick()
        waitForScreen("Countries")
    }

    @Test
    fun systemBackFromAnotherTabReturnsToCountries() {
        navItem("Business").performClick()
        waitForScreen("Business")

        // Espresso drives the system back press; Compose sees it through the activity's back dispatcher.
        Espresso.pressBack()

        waitForScreen("Countries")
    }

    // Regression tests: AppFeatureNavigation used to hardcode selectedKey = CountryRoute.Countries,
    // so the Countries tab stayed highlighted whichever screen was shown.

    /** Exactly one tab is highlighted, and it's [title]. */
    private fun assertOnlySelected(title: String) {
        tabs.forEach { tab ->
            if (tab == title) navItem(tab).assertIsSelected() else navItem(tab).assertIsNotSelected()
        }
    }

    @Test
    fun selectedTabFollowsTheCurrentScreen() {
        waitForScreen("Countries")
        assertOnlySelected("Countries")

        listOf("Doordash", "Business", "Countries").forEach { tab ->
            navItem(tab).performClick()
            waitForScreen(tab)
            assertOnlySelected(tab)
        }
    }

    @Test
    fun selectedTabFollowsSystemBack() {
        navItem("Business").performClick()
        waitForScreen("Business")
        assertOnlySelected("Business")

        Espresso.pressBack()

        waitForScreen("Countries")
        assertOnlySelected("Countries")
    }

    private val tabs = listOf("Countries", "Doordash", "Business")
}
