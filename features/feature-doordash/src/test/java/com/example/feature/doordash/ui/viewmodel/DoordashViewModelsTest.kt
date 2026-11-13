package com.example.feature.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.domain.Kind
import com.example.feature.doordash.FakeDoorDashAPIs
import com.example.feature.doordash.FakeLikedDao
import com.example.feature.doordash.MainDispatcherRule
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
import com.example.feature.doordash.restaurant
import com.example.feature.doordash.usecase.GetNearByRestaurantsUseCase
import com.example.feature.doordash.usecase.GetRestaurantDetailUseCase
import com.example.feature.doordash.usecase.LikeRestaurantUseCase
import com.example.presentation.model.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * These ViewModels hop to Dispatchers.IO internally, so the tests wait for the expected state
 * with `first { }` instead of reading `value` straight away.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DoordashViewModelsTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val apis = FakeDoorDashAPIs(restaurants = listOf(restaurant(1), restaurant(2)))
    private val local = DoordashDbRepository(FakeLikedDao())
    private val remote = DoorDashRepository(apis)

    private fun TestScope.subscribe(flow: kotlinx.coroutines.flow.Flow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
    }

    private fun listViewModel() = RestaurantListsViewModel(
        savedStateHandle = SavedStateHandle(),
        getRestaurantListUseCase = GetNearByRestaurantsUseCase(remote, local),
        likeRestaurantUseCase = LikeRestaurantUseCase(local)
    )

    @Test
    fun `list loads nearby restaurants with liked status`() = runTest {
        local.saveLikedStatus(restaurantId = 1, likedStatus = LikedStatus.LIKED)
        val viewModel = listViewModel()
        subscribe(viewModel.uiState)

        val state = viewModel.uiState.first { it is UIState.Success }

        assertEquals(
            UIState.Success(listOf(restaurant(1) to LikedStatus.LIKED, restaurant(2) to LikedStatus.NO_REF)),
            state
        )
    }

    @Test
    fun `list shows an error when loading fails`() = runTest {
        apis.error = IllegalStateException("offline")
        val viewModel = listViewModel()
        subscribe(viewModel.uiState)

        assertEquals(
            UIState.Error(message = "offline", kind = Kind.UNEXPECTED),
            viewModel.uiState.first { it is UIState.Error }
        )
    }

    @Test
    fun `list starts in loading before the first result`() = runTest {
        val viewModel = listViewModel()

        assertEquals(UIState.Loading, viewModel.uiState.value)
    }

    // Regression: like results were dropped (the use case never emitted), so the list never updated.
    @Test
    fun `liking a restaurant updates only that row`() = runTest {
        local.saveLikedStatus(restaurantId = 1, likedStatus = LikedStatus.UN_LIKED)
        val viewModel = listViewModel()
        subscribe(viewModel.uiState)
        viewModel.uiState.first { it is UIState.Success }

        viewModel.likedRestaurant(restaurantId = 1, likedStatus = LikedStatus.UN_LIKED).join()

        assertEquals(
            UIState.Success(listOf(restaurant(1) to LikedStatus.LIKED, restaurant(2) to LikedStatus.NO_REF)),
            viewModel.uiState.first { state ->
                state is UIState.Success && state.data.first().second == LikedStatus.LIKED
            }
        )
        assertEquals(LikedStatus.LIKED, local.getLikedStatusById(1))
    }

    @Test
    fun `list never falls back to loading after success`() = runTest {
        repeat(50) {
            val viewModel = listViewModel()
            val states = mutableListOf<UIState<*>>()
            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.toList(states) }

            viewModel.uiState.first { it is UIState.Success }
            job.cancel()

            val firstSuccess = states.indexOfFirst { it is UIState.Success }
            assertEquals("states: $states", emptyList<UIState<*>>(), states.drop(firstSuccess).filterNot { it is UIState.Success })
        }
    }

    @Test
    fun `selecting a restaurant emits its id`() = runTest {
        val viewModel = listViewModel()

        viewModel.selectedRestaurant.test {
            viewModel.onSelectedRestaurant(restaurantId = 2)
            assertEquals(2L, awaitItem())
        }
    }

    @Test
    fun `detail loads and is cached in saved state`() = runTest {
        val detail = RestaurantDetailDataModel(id = 1, name = "One")
        apis.detail = detail
        val savedStateHandle = SavedStateHandle()
        val viewModel = RestaurantDetailViewModel(savedStateHandle, GetRestaurantDetailUseCase(remote, local), restaurantId = 1)
        subscribe(viewModel.uiState)

        assertEquals(UIState.Success(detail to LikedStatus.NO_REF), viewModel.uiState.first { it is UIState.Success })
        assertEquals(detail to LikedStatus.NO_REF, viewModel.detail.first { it != null })
    }

    // Regression: the restore path used to `send` to a rendezvous Channel inside onStart, before anything
    // was receiving, so it suspended forever and the screen stayed on UIState.Default.
    @Test
    fun `detail restored from saved state skips the network`() = runTest {
        val cached = RestaurantDetailDataModel(id = 1, name = "Cached") to LikedStatus.LIKED
        val savedStateHandle = SavedStateHandle(mapOf("RestaurantDetailViewModel.ARGUMENT_RESTAURANT" to cached))
        val viewModel = RestaurantDetailViewModel(savedStateHandle, GetRestaurantDetailUseCase(remote, local), restaurantId = 1)
        subscribe(viewModel.uiState)

        // Restoring is synchronous now, so the value is there without waiting.
        assertEquals(UIState.Success(cached), viewModel.uiState.value)
        assertTrue(apis.detailRequests.isEmpty())
    }

    @Test
    fun `detail shows an error and retry reloads it`() = runTest {
        apis.error = IllegalStateException("offline")
        val viewModel = RestaurantDetailViewModel(SavedStateHandle(), GetRestaurantDetailUseCase(remote, local), restaurantId = 1)
        subscribe(viewModel.uiState)

        assertEquals(
            UIState.Error(message = "offline", kind = Kind.UNEXPECTED),
            viewModel.uiState.first { it is UIState.Error }
        )

        apis.error = null
        viewModel.fetchRestaurantDetail()

        assertEquals(UIState.Success(apis.detail to LikedStatus.NO_REF), viewModel.uiState.first { it is UIState.Success })
        assertEquals(listOf(1L, 1L), apis.detailRequests)
    }

    // Regression: every emission used to be sent from its own `launch` on Dispatchers.IO, so Loading
    // could land after Success and leave the screen spinning. Repeat to give a race a chance to show.
    @Test
    fun `detail never falls back to loading after success`() = runTest {
        repeat(50) {
            val viewModel = RestaurantDetailViewModel(SavedStateHandle(), GetRestaurantDetailUseCase(remote, local), restaurantId = 1)
            val states = mutableListOf<UIState<*>>()
            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.toList(states) }

            viewModel.uiState.first { it is UIState.Success }
            job.cancel()

            val firstSuccess = states.indexOfFirst { it is UIState.Success }
            assertEquals("states: $states", emptyList<UIState<*>>(), states.drop(firstSuccess).filterNot { it is UIState.Success })
        }
    }
}
