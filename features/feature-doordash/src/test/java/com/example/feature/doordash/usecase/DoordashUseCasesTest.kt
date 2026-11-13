package com.example.feature.doordash.usecase

import com.example.domain.UnexpectedError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.FakeDoorDashAPIs
import com.example.feature.doordash.FakeLikedDao
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.model.domain.usecase.GetNearByRestaurantInput
import com.example.feature.doordash.model.domain.usecase.GetRestaurantDetailInput
import com.example.feature.doordash.model.domain.usecase.LikeRestaurantUseCaseInput
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
import com.example.feature.doordash.module.network.DoorDashAPIs
import com.example.feature.doordash.restaurant
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DoordashUseCasesTest {

    private val apis = FakeDoorDashAPIs(restaurants = listOf(restaurant(1), restaurant(2)))
    private val dao = FakeLikedDao()
    private val remote = DoorDashRepository(apis)
    private val local = DoordashDbRepository(dao)

    @Test
    fun `nearby restaurants are paired with their stored liked status`() = runTest {
        local.saveLikedStatus(restaurantId = 2, likedStatus = LikedStatus.LIKED)

        val outputs = GetNearByRestaurantsUseCase(remote, local)(GetNearByRestaurantInput(lat = 1f, lng = 2f)).toList()

        assertEquals(
            UseCaseOutputWithStatus.Success(
                listOf(restaurant(1) to LikedStatus.NO_REF, restaurant(2) to LikedStatus.LIKED)
            ),
            outputs.last()
        )
        assertEquals(
            listOf(listOf<Number>(1f, 2f, DoorDashRepository.OFFSET, DoorDashRepository.LIMIT)),
            apis.nearByRequests
        )
    }

    @Test
    fun `nearby failure is reported`() = runTest {
        apis.error = IllegalStateException("offline")

        val outputs = GetNearByRestaurantsUseCase(remote, local)(GetNearByRestaurantInput(lat = 1f, lng = 2f)).toList()

        assertEquals(UnexpectedError("offline"), (outputs.last() as UseCaseOutputWithStatus.Failed).error)
    }

    @Test
    fun `restaurant detail is paired with its liked status`() = runTest {
        val detail = RestaurantDetailDataModel(id = 5, name = "Five")
        apis.detail = detail
        local.saveLikedStatus(restaurantId = 5, likedStatus = LikedStatus.UN_LIKED)

        val outputs = GetRestaurantDetailUseCase(remote, local)(GetRestaurantDetailInput(restaurantId = 5)).toList()

        assertEquals(UseCaseOutputWithStatus.Success(detail to LikedStatus.UN_LIKED), outputs.last())
        assertEquals(listOf(5L), apis.detailRequests)
    }

    @Test
    fun `restaurant detail without stored status is NO_REF`() = runTest {
        val outputs = GetRestaurantDetailUseCase(remote, local)(GetRestaurantDetailInput(restaurantId = 1)).toList()

        assertEquals(LikedStatus.NO_REF, (outputs.last() as UseCaseOutputWithStatus.Success).result.second)
    }

    @Test
    fun `liking toggles the stored status`() = runTest {
        val useCase = LikeRestaurantUseCase(local)

        // Regression: NO_REF used to map to NO_REF, so the first tap on a never-rated restaurant did nothing.
        useCase(LikeRestaurantUseCaseInput(restaurantId = 1, status = LikedStatus.NO_REF)).toList()
        assertEquals(LikedStatus.LIKED, local.getLikedStatusById(1))

        useCase(LikeRestaurantUseCaseInput(restaurantId = 1, status = LikedStatus.UN_LIKED)).toList()
        assertEquals(LikedStatus.LIKED, local.getLikedStatusById(1))

        useCase(LikeRestaurantUseCaseInput(restaurantId = 1, status = LikedStatus.LIKED)).toList()
        assertEquals(LikedStatus.UN_LIKED, local.getLikedStatusById(1))
    }

    // Regression: the use case is @ViewModelScoped and used to keep the request in a shared
    // `lateinit var input`. Overlapping requests overwrote it, so the slower one looked up the
    // other restaurant's liked status. It now keys the lookup on the returned detail.
    @Test
    fun `overlapping detail requests each get their own liked status`() = runTest {
        local.saveLikedStatus(restaurantId = 1, likedStatus = LikedStatus.LIKED)
        local.saveLikedStatus(restaurantId = 2, likedStatus = LikedStatus.UN_LIKED)
        // Restaurant 1 answers slowly, restaurant 2 quickly, so request 2 starts and finishes inside request 1.
        val slowApis = object : DoorDashAPIs by apis {
            override fun fetchRestaurantDetail(id: Long) = flow {
                delay(if (id == 1L) 100 else 10)
                emit(RestaurantDetailDataModel(id = id, name = "Restaurant $id"))
            }
        }
        val useCase = GetRestaurantDetailUseCase(DoorDashRepository(slowApis), local)

        val first = async { useCase(GetRestaurantDetailInput(restaurantId = 1)).toList().last() }
        delay(1) // let request 1 start before request 2
        val second = async { useCase(GetRestaurantDetailInput(restaurantId = 2)).toList().last() }

        assertEquals(LikedStatus.LIKED, (first.await() as UseCaseOutputWithStatus.Success).result.second)
        assertEquals(LikedStatus.UN_LIKED, (second.await() as UseCaseOutputWithStatus.Success).result.second)
    }

    // Regression: flowWork used to save without ever emitting, so callers only saw Progress and
    // the list never learned about the new status.
    @Test
    fun `liking emits progress then the new status`() = runTest {
        val useCase = LikeRestaurantUseCase(local)

        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success(LikedStatus.LIKED)),
            useCase(LikeRestaurantUseCaseInput(restaurantId = 1, status = LikedStatus.UN_LIKED)).toList()
        )
        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success(LikedStatus.UN_LIKED)),
            useCase(LikeRestaurantUseCaseInput(restaurantId = 1, status = LikedStatus.LIKED)).toList()
        )
    }
}
