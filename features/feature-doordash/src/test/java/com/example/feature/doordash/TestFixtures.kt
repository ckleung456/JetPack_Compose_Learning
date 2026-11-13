package com.example.feature.doordash

import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.module.domain.LikedDao
import com.example.feature.doordash.module.domain.LikedDb
import com.example.feature.doordash.module.network.DoorDashAPIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun restaurant(id: Long) = RestaurantDataModel(
    id = id,
    name = "Restaurant $id",
    description = null,
    coverImgUrl = null,
    status = "Open",
    deliveryFee = "0",
    statusType = "open"
)

/** In-memory [LikedDao] keyed by restaurant id. */
class FakeLikedDao : LikedDao {
    private val rows = MutableStateFlow<Map<Long?, LikedDb>>(emptyMap())
    val all: List<LikedDb> get() = rows.value.values.toList()

    override fun getAll(): Flow<List<LikedDb>> = rows.map { it.values.toList() }
    override fun getById(id: Long): LikedDb? = rows.value[id]
    override fun insert(likedDb: LikedDb) {
        rows.value = rows.value + (likedDb.id to likedDb)
    }
    override fun deleteAll(items: List<LikedDb>): Int {
        val ids = items.map { it.id }.toSet()
        val before = rows.value.size
        rows.value = rows.value.filterKeys { it !in ids }
        return before - rows.value.size
    }
}

class FakeDoorDashAPIs(
    var restaurants: List<RestaurantDataModel> = emptyList(),
    var detail: RestaurantDetailDataModel = RestaurantDetailDataModel(id = 1, name = "Detail")
) : DoorDashAPIs {
    var error: Throwable? = null
    val nearByRequests = mutableListOf<List<Number>>()
    val detailRequests = mutableListOf<Long>()

    override fun fetchRestaurantNearBy(lat: Float, lng: Float, offset: Int, limit: Int): Flow<List<RestaurantDataModel>> {
        nearByRequests += listOf(lat, lng, offset, limit)
        return emitOrThrow(restaurants)
    }

    override fun fetchRestaurantDetail(id: Long): Flow<RestaurantDetailDataModel> {
        detailRequests += id
        return emitOrThrow(detail)
    }

    private fun <T> emitOrThrow(value: T): Flow<T> {
        val failure = error
        return flow {
            if (failure != null) throw failure
            emit(value)
        }
    }
}
