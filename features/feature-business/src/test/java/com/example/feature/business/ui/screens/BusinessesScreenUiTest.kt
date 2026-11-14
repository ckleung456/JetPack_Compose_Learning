package com.example.feature.business.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import com.example.feature.business.ui.components.BusinessTestTags
import com.example.feature.business.ui.viewmodel.BusinessesViewModel
import com.example.feature.business.usecase.BuildBusinessSectionsUseCase
import com.example.feature.business.fakeBusinessLikedStore
import com.example.feature.business.usecase.GetCurrentAddressUseCase
import com.example.feature.business.usecase.ObserveLikedBusinessIdsUseCase
import com.example.feature.business.usecase.ToggleBusinessLikeUseCase
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
        val likedStore = fakeBusinessLikedStore()
        val viewModel = BusinessesViewModel(
            savedStateHandle = SavedStateHandle(),
            searchPizzaAndBeerUseCase = SearchPizzaAndBeerUseCase(repository),
            buildBusinessSectionsUseCase = BuildBusinessSectionsUseCase(),
            getCurrentAddressUseCase = GetCurrentAddressUseCase(FakeLocationAddressProvider()),
            observeLikedBusinessIdsUseCase = ObserveLikedBusinessIdsUseCase(likedStore),
            toggleBusinessLikeUseCase = ToggleBusinessLikeUseCase(likedStore)
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

    private val likedStore = fakeBusinessLikedStore()

    private fun setLikeScreen() {
        val repository = FakeBusinessRepository(
            pizza = listOf(business(id = "p1", name = "Tony's")),
            beer = listOf(business(id = "b1", name = "Brewery"))
        )
        val viewModel = BusinessesViewModel(
            savedStateHandle = SavedStateHandle(),
            searchPizzaAndBeerUseCase = SearchPizzaAndBeerUseCase(repository),
            buildBusinessSectionsUseCase = BuildBusinessSectionsUseCase(),
            getCurrentAddressUseCase = GetCurrentAddressUseCase(FakeLocationAddressProvider()),
            observeLikedBusinessIdsUseCase = ObserveLikedBusinessIdsUseCase(likedStore),
            toggleBusinessLikeUseCase = ToggleBusinessLikeUseCase(likedStore)
        )
        composeRule.setContent {
            BusinessesScreen(viewModel = viewModel, onBusinessSelected = {})
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasTestTag(BusinessTestTags.likeButton("p1"))).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun likeButton_togglesThroughTheViewModel() {
        setLikeScreen()
        val like = likeButton("p1")
        like.assertIsOff()

        like.performClick()
        like.assertIsOn()
        likeButton("b1").assertIsOff()

        likeButton("p1").performClick()
        likeButton("p1").assertIsOff()
    }

    // Regression: a like toggled on the detail screen wasn't shown when going back to the list.
    @Test
    fun likeToggledOnAnotherScreen_isShownInTheList() {
        setLikeScreen()
        likeButton("b1").assertIsOff()

        likedStore.toggleBusinessLiked("b1") // what the detail screen's ToggleBusinessLikeUseCase does
        composeRule.waitForIdle()

        likeButton("b1").assertIsOn()
    }

    /** The list is lazy, so scroll the row into view before looking up its like button. */
    private fun likeButton(id: String) = composeRule.run {
        onNodeWithTag(BusinessTestTags.BUSINESS_LIST).performScrollToNode(hasTestTag(BusinessTestTags.likeButton(id)))
        onNodeWithTag(BusinessTestTags.likeButton(id))
    }
}
