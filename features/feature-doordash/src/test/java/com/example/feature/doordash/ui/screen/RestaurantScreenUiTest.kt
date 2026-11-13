package com.example.feature.doordash.ui.screen

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.doordash.FakeDoorDashAPIs
import com.example.feature.doordash.FakeLikedDao
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
import com.example.feature.doordash.restaurant
import com.example.feature.doordash.ui.viewmodel.RestaurantListsViewModel
import com.example.feature.doordash.usecase.GetNearByRestaurantsUseCase
import com.example.feature.doordash.usecase.LikeRestaurantUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Renders [RestaurantScreen] with Robolectric on top of the real ViewModel, fake API and in-memory DAO. */
@RunWith(AndroidJUnit4::class)
class RestaurantScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apis = FakeDoorDashAPIs(restaurants = listOf(restaurant(1), restaurant(2)))
    private val selected = mutableListOf<Long>()

    private val local = DoordashDbRepository(FakeLikedDao())

    /** The like IconButton inside the card for [name] (the icon has no content description). */
    private fun likeButton(name: String) = composeRule.onNode(
        hasClickAction() and hasAnyAncestor(hasClickAction() and hasText(name))
    )

    private fun likedStatus(id: Long) = runBlocking { local.getLikedStatusById(id) }

    @Test
    fun likeButtonTogglesTheStatusEachTap() {
        runBlocking { local.saveLikedStatus(restaurantId = 1, likedStatus = LikedStatus.UN_LIKED) }
        setScreen()
        waitFor(hasText("Restaurant 1"))

        likeButton("Restaurant 1").performClick()
        composeRule.waitUntil(5_000) { likedStatus(1) == LikedStatus.LIKED }

        // The second tap must send the *updated* status (LIKED) so it toggles back.
        likeButton("Restaurant 1").performClick()
        composeRule.waitUntil(5_000) { likedStatus(1) == LikedStatus.UN_LIKED }
    }

    private fun setScreen() {
        val viewModel = RestaurantListsViewModel(
            savedStateHandle = SavedStateHandle(),
            getRestaurantListUseCase = GetNearByRestaurantsUseCase(DoorDashRepository(apis), local),
            likeRestaurantUseCase = LikeRestaurantUseCase(local)
        )
        composeRule.setContent {
            RestaurantScreen(viewModel = viewModel, onRestaurantSelected = { selected += it })
        }
    }

    private fun waitFor(matcher: SemanticsMatcher) = composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun showsNearbyRestaurants() {
        setScreen()
        waitFor(hasText("Restaurant 1"))

        composeRule.onNodeWithText("Doordash").assertIsDisplayed()
        composeRule.onNodeWithText("Restaurant 1").assertIsDisplayed()
        composeRule.onNodeWithText("Restaurant 2").assertIsDisplayed()
    }

    @Test
    fun clickingRestaurantReportsItsId() {
        setScreen()
        waitFor(hasText("Restaurant 2"))

        composeRule.onNodeWithText("Restaurant 2").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { selected.isNotEmpty() }

        assertEquals(listOf(2L), selected)
    }

    @Test
    fun errorShowsRetryWhichReloads() {
        apis.error = IllegalStateException("offline")
        setScreen()
        waitFor(hasText("offline"))

        apis.error = null
        composeRule.onNodeWithText("Try Again").performClick()

        waitFor(hasText("Restaurant 1"))
    }
}
