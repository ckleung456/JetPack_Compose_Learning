package com.example.feature.business.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.FakeLocationAddressProvider
import com.example.feature.business.business
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessListState
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.toBusinessUi
import com.example.feature.business.ui.viewmodel.BusinessesViewModel
import com.example.feature.business.usecase.BuildBusinessSectionsUseCase
import com.example.feature.business.usecase.GetCurrentAddressUseCase
import com.example.feature.business.usecase.SearchPizzaAndBeerUseCase
import com.example.presentation.model.UIState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Robolectric tests for the stateful [BusinessesScreen] wired to a real ViewModel with fake data sources. */
@RunWith(AndroidJUnit4::class)
class BusinessesScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun partialFailure_snackbarShowsResourceText() {
        val repository = FakeBusinessRepository(pizza = listOf(business(id = "p1", name = "Tony's"))).apply {
            beerError = IOException("boom")
        }
        val viewModel = BusinessesViewModel(
            savedStateHandle = SavedStateHandle(),
            searchPizzaAndBeerUseCase = SearchPizzaAndBeerUseCase(repository),
            buildBusinessSectionsUseCase = BuildBusinessSectionsUseCase(),
            getCurrentAddressUseCase = GetCurrentAddressUseCase(FakeLocationAddressProvider())
        )

        composeRule.setContent {
            BusinessesScreen(viewModel = viewModel, onBusinessSelected = {})
        }

        // Before the fix the screen ignored messageId and showed an empty snackbar.
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Couldn't load beer places."))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Couldn't load beer places.").assertExists()
    }

    @Test
    fun screenModifier_isAppliedOnlyToTheRoot() {
        val sections = persistentListOf(
            BusinessSectionUi(
                category = BusinessCategory.PIZZA,
                businesses = persistentListOf(
                    business(id = "p1").toBusinessUi(),
                    business(id = "p2").toBusinessUi()
                )
            )
        )
        composeRule.setContent {
            BusinessListScreen(
                modifier = Modifier.testTag(SCREEN_TAG),
                title = "Business",
                state = BusinessListState(content = UIState.Success(sections)),
                onAction = {}
            )
        }

        // Passing the parent modifier down to children duplicated it onto every item and divider.
        composeRule.onAllNodesWithTag(SCREEN_TAG).assertCountEquals(1)
    }

    private companion object {
        const val SCREEN_TAG = "businesses_screen"
    }
}
