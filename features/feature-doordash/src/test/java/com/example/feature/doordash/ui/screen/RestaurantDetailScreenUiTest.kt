package com.example.feature.doordash.ui.screen

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.doordash.FakeDoorDashAPIs
import com.example.feature.doordash.FakeLikedDao
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
import com.example.feature.doordash.ui.viewmodel.RestaurantDetailViewModel
import com.example.feature.doordash.usecase.GetRestaurantDetailUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Renders [RestaurantDetailScreen] with Robolectric on top of the real ViewModel and fakes. */
@RunWith(AndroidJUnit4::class)
class RestaurantDetailScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apis = FakeDoorDashAPIs(detail = RestaurantDetailDataModel(id = 1, name = "Fresh Detail"))
    private val local = DoordashDbRepository(FakeLikedDao())

    private fun setScreen(savedStateHandle: SavedStateHandle = SavedStateHandle()) {
        val viewModel = RestaurantDetailViewModel(savedStateHandle, GetRestaurantDetailUseCase(DoorDashRepository(apis), local), 1)
        composeRule.setContent { RestaurantDetailScreen(onBackClick = {}, viewModel = viewModel) }
    }

    private fun waitFor(matcher: SemanticsMatcher) = composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun loadsDetailFromTheNetwork() {
        setScreen()
        waitFor(hasText("Fresh Detail"))

        composeRule.onNodeWithText("Restaurant Detail").assertIsDisplayed()
        composeRule.onNodeWithText("Liked status : NO_REF").assertExists()
        assertEquals(listOf(1L), apis.detailRequests)
    }

    // Regression: after process death the screen used to stay blank because the ViewModel's restore path
    // suspended forever on an unbuffered Channel.
    @Test
    fun restoredDetailIsShownWithoutRefetching() {
        val cached = RestaurantDetailDataModel(id = 1, name = "Cached Detail") to LikedStatus.LIKED
        setScreen(SavedStateHandle(mapOf("RestaurantDetailViewModel.ARGUMENT_RESTAURANT" to cached)))

        waitFor(hasText("Cached Detail"))
        composeRule.onNodeWithText("Liked status : LIKED").assertExists()
        assertTrue(apis.detailRequests.isEmpty())
    }

    @Test
    fun errorShowsRetryWhichReloads() {
        apis.error = IllegalStateException("offline")
        setScreen()
        waitFor(hasText("offline"))

        apis.error = null
        composeRule.onNodeWithText("Try Again").performClick()

        waitFor(hasText("Fresh Detail"))
    }
}
