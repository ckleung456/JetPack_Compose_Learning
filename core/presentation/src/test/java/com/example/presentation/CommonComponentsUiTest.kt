package com.example.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.domain.Kind
import com.example.presentation.model.UIState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Robolectric Compose tests for the shared building blocks in core:presentation. */
@RunWith(AndroidJUnit4::class)
class CommonComponentsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun errorView_showsMessageAndRetries() {
        var retries = 0
        composeRule.setContent { ErrorView(message = "No connection", onRetry = { retries++ }) }

        composeRule.onNodeWithText("Oops!").assertIsDisplayed()
        composeRule.onNodeWithText("No connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun uiStatefulContent_rendersTheSlotForEachState() {
        var state: UIState<String> by mutableStateOf(UIState.Loading)
        composeRule.setContent {
            UIStatefulContent(
                state = state,
                loadingContent = { Text("loading") },
                errorContent = { message, kind -> Text("error $message $kind") },
                successContent = { data -> Text("data $data") }
            )
        }

        composeRule.onNodeWithText("loading").assertIsDisplayed()

        state = UIState.Error(message = "boom", kind = Kind.NETWORK)
        composeRule.onNodeWithText("error boom NETWORK").assertIsDisplayed()

        state = UIState.Success("hello")
        composeRule.onNodeWithText("data hello").assertIsDisplayed()

        state = UIState.Default
        composeRule.onNodeWithText("data hello").assertDoesNotExist()
    }

    @Test
    fun searchBar_reportsTypingAndShowsClearOnlyWithText() {
        var query by mutableStateOf("")
        var cleared = 0
        composeRule.setContent {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onClearClick = { cleared++; query = "" },
                placeholder = "Search countries"
            )
        }

        composeRule.onNodeWithContentDescription("Clear").assertDoesNotExist()

        composeRule.onNode(hasSetTextAction()).performTextInput("fra")
        assertEquals("fra", query)

        composeRule.onNodeWithContentDescription("Clear").performClick()
        assertEquals(1, cleared)
        assertEquals("", query)
        composeRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun appTopBar_showsTitleAndBackOnlyWhenHandled() {
        var backs = 0
        var withBack by mutableStateOf(false)
        composeRule.setContent {
            AppTopBar(title = "Countries", onBackClick = if (withBack) ({ backs++ }) else null)
        }

        composeRule.onNodeWithText("Countries").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").assertDoesNotExist()

        withBack = true
        composeRule.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backs)
    }
}
